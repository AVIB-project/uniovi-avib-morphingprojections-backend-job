package es.uniovi.avib.morphing.projections.backend.job.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.uniovi.avib.morphing.projections.backend.job.domain.Job;
import es.uniovi.avib.morphing.projections.backend.job.dto.JobLogDto;
import es.uniovi.avib.morphing.projections.backend.job.dto.JobSubmitConverterDto;
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
	
	@RequestMapping(method = { RequestMethod.GET }, produces = "application/json", value = "/cases/{caseId}")
	public ResponseEntity<List<Job>> findJobsByCaseId(@PathVariable String caseId) {
		List<Job> jobs = (List<Job>) jobService.findJobsByCaseId(caseId);
					
		log.debug("findByCaseId: found {} jobs", jobs.size());
		
		return new ResponseEntity<List<Job>>(jobs, HttpStatus.OK);			
	}
	
	@RequestMapping(method = { RequestMethod.GET }, produces = "application/json", value = "/{jobName}/getJobLogs")
    public JobLogDto getJobLogs(@PathVariable String jobName) {
		log.debug("getJobLogs: get logs from name {}", jobName);
		
        return jobService.getJobLogs(jobName);
    }	
		
	@RequestMapping(method = { RequestMethod.POST }, produces = "application/json", value = "/submitJob")	
	public ResponseEntity<Object> submitJob(@RequestBody JobSubmitDto jobSubmitDto) {
		log.debug("submitJob job for case Id with id {}", jobSubmitDto.getCaseId());
		
		Object resultFlow = jobService.submitJob(jobSubmitDto);
			
		return new ResponseEntity<Object>(resultFlow, HttpStatus.OK);			
	}
	
	@RequestMapping(method = { RequestMethod.POST }, produces = "application/json", value = "/submitConverterJob")	
	public ResponseEntity<Object> submitConverterJob(@RequestBody JobSubmitConverterDto jobSubmitConverterDto) {
		log.debug("submitConverterJob job for case Id with id {}", jobSubmitConverterDto.getCaseId());
								
		Object resultFlow = jobService.submitConverterJob(jobSubmitConverterDto);
			
		return new ResponseEntity<Object>(resultFlow, HttpStatus.OK);			
	}	
}
