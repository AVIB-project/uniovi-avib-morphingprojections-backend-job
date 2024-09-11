package es.uniovi.avib.morphing.projections.backend.job.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobList;
import io.fabric8.kubernetes.client.KubernetesClientException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import es.uniovi.avib.morphing.projections.backend.job.configuration.KubeClientConfig;
import es.uniovi.avib.morphing.projections.backend.job.configuration.OrganizationConfig;
import es.uniovi.avib.morphing.projections.backend.job.dto.CaseProjectDto;
import es.uniovi.avib.morphing.projections.backend.job.dto.ImageDto;
import es.uniovi.avib.morphing.projections.backend.job.dto.JobLogDto;
import es.uniovi.avib.morphing.projections.backend.job.dto.JobSubmitDto;
import es.uniovi.avib.morphing.projections.backend.job.repository.JobRepository;

@Slf4j
@AllArgsConstructor
@Service
public class JobService {	
	final int TTL_AFTER_FINISHED = 10;
	final String JOB_NAMESPACE = "default";

	private final KubeClientConfig kubeConfig;
	private final OrganizationConfig organizationConfig;
	
	private final RestTemplate restTemplate;
	private final JobRepository jobRepository;
	
	public List<es.uniovi.avib.morphing.projections.backend.job.domain.Job> findJobsByCaseId(String caseId) {
		log.debug("getJobsByCaseId: found job with caseId: {}", caseId);
		
		return jobRepository.findByCaseId(new ObjectId(caseId));
	}
	
	@SuppressWarnings("finally")
 	public Object submitJob(JobSubmitDto jobSubmitDto) {
		log.debug("submitJob for caseId with id {}", jobSubmitDto.getCaseId());
		
		// get case from Id
		String urlCase = "http://" + organizationConfig.getHost() + ":" + organizationConfig.getPort() + "/cases/" + jobSubmitDto.getCaseId();
		
		ResponseEntity<CaseProjectDto> caseProjectDto = restTemplate.getForEntity(urlCase, CaseProjectDto.class);
		
		// get image from id
		String urlImage = "http://" + organizationConfig.getHost() + ":" + organizationConfig.getPort() + "/images/" + caseProjectDto.getBody().getImageId();
		
		ResponseEntity<ImageDto> imageDto = restTemplate.getForEntity(urlImage, ImageDto.class);
		String image = imageDto.getBody().getImage() + ":" +imageDto.getBody().getVersion();
		
		// create job from image 
		Map<String, String> result = new HashMap<>();
		
		String guid = UUID.randomUUID().toString();
		
		try {    		
    		final Job jobSchedule = new JobBuilder()
  	              .withApiVersion("batch/v1")
  	              	.withNewMetadata()
  	              		.withName("job-"+ guid)
  	              		.withNamespace(JOB_NAMESPACE)
  	              	.endMetadata()
  	              	.withNewSpec()
  	              		.withTtlSecondsAfterFinished(TTL_AFTER_FINISHED)
  	              		.withNewTemplate()
  	              			.withNewMetadata()
  	              				.withName("pod-"+ guid)
  	              			.endMetadata()
  	              			.withNewSpec()
  	              				.addNewContainer()
  	              					.withName("container-" + guid)
  	              					.withImage(image)
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
				.resource(jobSchedule)
			.create();   		
    		
    		// save job scheduled
    		es.uniovi.avib.morphing.projections.backend.job.domain.Job job = new es.uniovi.avib.morphing.projections.backend.job.domain.Job();
    		
    		job.setCaseId(new ObjectId(caseProjectDto.getBody().getCaseId()));
    		job.setName("job-"+ guid);
    		job.setImage(imageDto.getBody().getImage());
    		job.setVersion(imageDto.getBody().getVersion());
    		
    		jobRepository.save(job);
    		
            result.put("message", "Job with name " + jobsResult.getMetadata().getName() + " created in default namespace.");
        } catch (KubernetesClientException exception) {
        	log.error(exception.getStackTrace().toString());
            
            result.put("error", exception.getMessage());           
        } finally {
            return result;
		}		
	}
	
	@SuppressWarnings("finally")
	public JobLogDto getJobLogs(String jobName) {
		JobLogDto jobLogDto = null;

  		String jobLogs = "";
  		String jobState = "";
  		
        try {    		
			// get jobs in default namespace
        	JobList jobList = kubeConfig.getKubernetesClient().batch().v1().jobs().inNamespace(JOB_NAMESPACE).list(); 		

        	for (Job job : jobList.getItems()) {
        		// get job by name
        	    if (job.getMetadata().getName().equals(jobName)) {
        	    	// get pods from job
        			PodList podList = kubeConfig.getKubernetesClient().pods()
        					.inNamespace(JOB_NAMESPACE)
        					.withLabel("job-name", jobName)
        				.list();
       			 
        			if (job.getStatus().getActive() != null && job.getStatus().getActive() == 1)
        				jobState = "Running";
        			else {
        				if (job.getStatus().getSucceeded() != null && job.getStatus().getSucceeded() == 1)
        					jobState = "Succeeded";
            			else
            				jobState = "Failed";        				
        			}
        			     			
        			// get job log
        			jobLogs = kubeConfig.getKubernetesClient().pods()
        		    		.inNamespace(JOB_NAMESPACE)
        		    		.withName(podList.getItems().get(0).getMetadata().getName())
        		    		.getLog(true);
        	    }
        	}
        	
        	jobLogDto = JobLogDto.builder()
        		.log(jobLogs)
        		.state(jobState)
        		.error(null)
        	.build();
        } catch (KubernetesClientException exception) {
        	log.error(exception.getStackTrace().toString());
            
        	jobLogDto = JobLogDto.builder()
        		.log(null)
    			.state(jobState)
    			.error(exception.getMessage())
    		.build();
        } finally {
            return jobLogDto;
        }
	}
}
