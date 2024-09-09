package es.uniovi.avib.morphing.projections.backend.job.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import es.uniovi.avib.morphing.projections.backend.job.configuration.KubeClientConfig;
import es.uniovi.avib.morphing.projections.backend.job.dto.JobSubmitDto;
import es.uniovi.avib.morphing.projections.backend.job.repository.JobRepository;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobList;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class JobService {	
	final int TTL_AFTER_FINISHED = 10;
	final String JOB_NAMESPACE = "default";

	private final KubeClientConfig kubeConfig;
	private final JobRepository jobRepository;
	
	@SuppressWarnings("finally")
	public Object submitJob(JobSubmitDto jobSubmitDto) {
		log.debug("submitJob for caseId with id {}", jobSubmitDto.getCaseId());
		
		Map<String, String> result = new HashMap<>();
		
		try {
    		// create job    		
    		final Job job = new JobBuilder()
  	              .withApiVersion("batch/v1")
  	              	.withNewMetadata()
  	              		.withName("sample-job")
  	              		.withNamespace(JOB_NAMESPACE)
  	              	.endMetadata()
  	              	.withNewSpec()
  	              		.withTtlSecondsAfterFinished(TTL_AFTER_FINISHED)
  	              		.withNewTemplate()
  	              			.withNewMetadata()
  	              				.withName("sample-pod")
  	              			.endMetadata()
  	              			.withNewSpec()
  	              				.addNewContainer()
  	              					.withName("sample-container")
  	              					.withImage("busybox")
  	              					.withArgs("/bin/sh", "-c", "for i in $(seq 120); do echo \"Welcome $i times\"; sleep 1; done")
  	              				.endContainer()
  	              				.withRestartPolicy("Never")
  	              			.endSpec()
  	              		.endTemplate()
  	              	.endSpec()
  	              .build();  
    		
			// execute the job
    		Job jobsResult = kubeConfig.getKubernetesClient().batch().v1().jobs()
				.inNamespace(JOB_NAMESPACE)
				.resource(job)
			.create();   		

            result.put("message", "Job with name " + jobsResult.getMetadata().getName() + " created in default namespace.");
        } catch (KubernetesClientException exception) {
        	log.error(exception.getStackTrace().toString());
            
            result.put("error", exception.getMessage());           
        } finally {
            return result;
		}		
	}
	
	@SuppressWarnings("finally")
	public Map<String, String> getJobLogs(String jobName) {
		Map<String, String> result = new HashMap<>();
        
        try {    		
			// get jobs in default namespace
        	JobList jobList = kubeConfig.getKubernetesClient().batch().v1().jobs().inNamespace(JOB_NAMESPACE).list(); 		

      		String jobLogs = "";
        	for (Job job : jobList.getItems()) {
        		// get job by name
        	    if (job.getMetadata().getName().equals(jobName)) {
        	    	// get job pods
        			PodList podList = kubeConfig.getKubernetesClient().pods()
        					.inNamespace(JOB_NAMESPACE)
        					.withLabel("job-name", jobName)
        				.list();
       			 
        			// get job log
        			jobLogs = kubeConfig.getKubernetesClient().pods()
        		    		.inNamespace(JOB_NAMESPACE)
        		    		.withName(podList.getItems().get(0).getMetadata().getName())
        		    		.getLog(true);
        	    }
        	}
        	
            result.put("log", jobLogs);
        } catch (KubernetesClientException exception) {
        	log.error(exception.getStackTrace().toString());
            
            result.put("error", exception.getMessage());
        } finally {
            return result;
        }
	}	
}
