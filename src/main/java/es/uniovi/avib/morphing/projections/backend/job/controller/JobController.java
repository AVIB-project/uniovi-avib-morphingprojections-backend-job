package es.uniovi.avib.morphing.projections.backend.job.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.uniovi.avib.morphing.projections.backend.job.dto.JobSubmitDto;
import es.uniovi.avib.morphing.projections.backend.job.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(maxAge = 3600)
@RestController
@RequiredArgsConstructor
@RequestMapping("jobs")
public class JobController {
	private final JobService jobService;
	
	@RequestMapping(method = { RequestMethod.POST }, produces = "application/json", value = "/submitJob")	
	public ResponseEntity<Object> submitJob(@RequestBody JobSubmitDto jobSubmitDto) {
		log.debug("submitJob job for case Id with id {}", jobSubmitDto.getCaseId());
		
		Object resultFlow = jobService.submitJob(jobSubmitDto);
			
		return new ResponseEntity<Object>(resultFlow, HttpStatus.OK);			
	}
	
	@RequestMapping(method = { RequestMethod.GET }, produces = "application/json", value = "value = /{jobName}/getJobLogs")
    public Map<String, String> getJobLogs(@PathVariable String jobName) {
        return jobService.getJobLogs(jobName);
    }	
}
