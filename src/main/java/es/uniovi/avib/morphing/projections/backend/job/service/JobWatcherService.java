package es.uniovi.avib.morphing.projections.backend.job.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import es.uniovi.avib.morphing.projections.backend.job.configuration.KubeClientConfig;
import es.uniovi.avib.morphing.projections.backend.job.repository.JobRepository;

@Slf4j
@AllArgsConstructor
@Service
public class JobWatcherService {
	private final KubeClientConfig kubeConfig;
	
	private final JobRepository jobRepository;
	
	private final String JOB_RUNNING_STATE = "Running";
	private final String JOB_SUCCEEDED_STATE = "Succeeded";
	private final String JOB_FAILED_STATE = "Failed";
	
	private String getJobState(Job job) {
		if (job.getStatus().getActive() != null && job.getStatus().getActive() == 1) {
			return JOB_RUNNING_STATE;			
		} else {
			if (job.getStatus().getSucceeded() != null && job.getStatus().getSucceeded() == 1) {
				return JOB_SUCCEEDED_STATE;	
			} else {
				return JOB_FAILED_STATE;  
			}
		}
	}
	
	@Async
	@EventListener(ApplicationStartedEvent.class)
	public void performStateChecks() throws IOException {
		kubeConfig.getKubernetesClient().batch().v1().jobs()
				.inNamespace("default")
				.watch(new Watcher<>() {
					@Override
					public void eventReceived(Action action, Job job) {
			            switch (action.name()) {
			                case "ADDED":
			                    log.info("{}/{} got added", job.getMetadata().getNamespace(), job.getMetadata().getName());					                    	
							      
			                    break;
			                case "DELETED":
			                	log.info("{}/{} got deleted", job.getMetadata().getNamespace(), job.getMetadata().getName());
			                    
			                	break;
			                case "MODIFIED":
			                	log.info("{}/{} got modified", job.getMetadata().getNamespace(), job.getMetadata().getName());
			                	
			                	// get unique Job
			                	List<es.uniovi.avib.morphing.projections.backend.job.domain.Job> jobsScheduled = jobRepository.findByName(job.getMetadata().getName());
			                	
			                	// set last state job
			                	if (jobsScheduled.size() > 0) {
			                		es.uniovi.avib.morphing.projections.backend.job.domain.Job jobScheduled = jobsScheduled.get(0);
			                		
			                		String jobState = getJobState(job);
			                		
			                		// set last state and datetime
			                		jobScheduled.setState(getJobState(job));
			                		
			                		if (jobState.equals(JOB_RUNNING_STATE)) {
			                			if (jobScheduled.getJobCreationDate() == null) {
			                				jobScheduled.setJobCreationDate(new Date());
			                			}
			                		}
			                		else {
			                			jobScheduled.setJobFinalizeDate(new Date());
			                		}
			                					                					                				                		
			                		jobRepository.save(jobScheduled);
			                	}
			                	
			                    break;
			                default:
			                	log.error("Unrecognized event: {}", action.name());
			            }
					}
	
					@Override
					public void onClose() {
			            log.info("Watch closed");
					}
					
					@Override
					public void onClose(WatcherException cause) {
			            log.info("Watched closed due to exception ", cause);
					}			    	    		  		    	    	  
			});	    
	}
}
