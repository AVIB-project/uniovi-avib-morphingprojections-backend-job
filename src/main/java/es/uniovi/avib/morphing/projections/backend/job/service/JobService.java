package es.uniovi.avib.morphing.projections.backend.job.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobList;
import io.fabric8.kubernetes.client.KubernetesClientException;

import org.bson.types.ObjectId;

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
	final String JOB_NAMESPACE = "default";
	final String JOB_SECRET_PULL_REQUEST = "acr-avib-secret";
	final int JOB_TTL_AFTER_FINISHED = 3600;
	
	private final KubeClientConfig kubeConfig;
	private final OrganizationConfig organizationConfig;
	
	private final RestTemplate restTemplate;
	private final JobRepository jobRepository;
	
	private final String JOB_RUNNING_STATE = "Running";
	
	public List<es.uniovi.avib.morphing.projections.backend.job.domain.Job> findJobsByCaseId(String caseId) {
		log.debug("getJobsByCaseId: found job with caseId: {}", caseId);
		
		return jobRepository.findByCaseId(new ObjectId(caseId));
	}
	
	@SuppressWarnings("finally")
 	public Object submitJob(JobSubmitDto jobSubmitDto) {
		log.debug("submitJob for caseId with id {}", jobSubmitDto.getCaseId());
		
		Map<String, String> result = new HashMap<>();
		
		// get case from Id
		String urlCase = "http://" + organizationConfig.getHost() + ":" + organizationConfig.getPort() + "/cases/" + jobSubmitDto.getCaseId();
		
		ResponseEntity<CaseProjectDto> caseProjectDto = restTemplate.getForEntity(urlCase, CaseProjectDto.class);
		
		// get image from id
		String urlImage = "http://" + organizationConfig.getHost() + ":" + organizationConfig.getPort() + "/images/" + caseProjectDto.getBody().getImageId();
		
		ResponseEntity<ImageDto> imageDto = restTemplate.getForEntity(urlImage, ImageDto.class);
		
		// get job image
		String image = imageDto.getBody().getImage() + ":" +imageDto.getBody().getVersion();
		
		// get job environment
		String environment = imageDto.getBody().getEnvironment();
				
		// get job command
		String command = imageDto.getBody().getCommand();
		
		// get job submit parameters if exist and substitute
		if (jobSubmitDto.getParameters() != null) {
			for (String parameter : jobSubmitDto.getParameters().keySet()) {
				command = command.replace("${" + parameter + "}", jobSubmitDto.getParameters().get(parameter));
			}
		}		
						
		try {    
    		// STEP01: save job scheduled
			String guid = UUID.randomUUID().toString();
			
    		es.uniovi.avib.morphing.projections.backend.job.domain.Job job = new es.uniovi.avib.morphing.projections.backend.job.domain.Job();
    		
    		job.setCaseId(new ObjectId(caseProjectDto.getBody().getCaseId()));
    		job.setName("job-"+ guid);
    		job.setImage(imageDto.getBody().getImage());
    		job.setState(JOB_RUNNING_STATE);
    		job.setVersion(imageDto.getBody().getVersion());
    		
    		jobRepository.save(job);
    		
    		// STEP02: deploy job in Kubernetes 
    		final Job jobSchedule = new JobBuilder()
  	              .withApiVersion("batch/v1")
  	              	.withNewMetadata()
  	              		.withName("job-"+ guid)
  	              		.withNamespace(JOB_NAMESPACE)
  	              	.endMetadata()
  	              	.withNewSpec()
  	              		.withTtlSecondsAfterFinished(JOB_TTL_AFTER_FINISHED)  	              		
  	              		.withNewTemplate()  	              			
  	              			.withNewMetadata()
  	              				.withName("pod-"+ guid)
  	              			.endMetadata()
  	              			.withNewSpec()
  	              				.addNewContainer()
  	              					.withName("container-" + guid)
  	              					.withImage(image)
  	              					.addNewEnv()
	              						.withName("ARG_PYTHON_PROFILES_ACTIVE")
	              						.withValue(environment)
	              					.endEnv()  	              					
  	              					//.withArgs("/bin/sh", "-c", "for i in $(seq 120); do echo \"Welcome $i times\"; sleep 1; done")
	              					//.withArgs("/bin/sh", "-c", "python src/morphingprojections_job_projection/service.py --case-id 65cdc989fa8c8fdbcefac01e --space primal,dual")
	              					.withArgs("/bin/sh", "-c", command)
  	              				.endContainer()
  	              				.withRestartPolicy("Never")
  	              				.addNewImagePullSecret()
  	              					.withName(JOB_SECRET_PULL_REQUEST)  	              					
  	              				.endImagePullSecret()
  	              			.endSpec()
  	              		.endTemplate()
  	              	.endSpec()
  	              .build();  
    		
    		Job jobsResult = kubeConfig.getKubernetesClient().batch().v1().jobs()
				.inNamespace(JOB_NAMESPACE)
				.resource(jobSchedule)
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
