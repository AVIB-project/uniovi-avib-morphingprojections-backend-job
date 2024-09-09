package es.uniovi.avib.morphing.projections.backend.job.domain;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "job")
public class Job {
	@Id	
	private String jobId;
	
	@NotNull(message = "Case Id may not be null")
	@Field("case_id")
	private ObjectId caseId;

	@NotNull(message = "Job State not be null")
	@Field("state")
	private String state;

	@NotNull(message = "Job Image not be null")
	@Field("image")
	private String image;

	@NotNull(message = "Job Image version not be null")
	@Field("version")
	private String version;
	
	@Field("job_creation")
	private String jobCreation;	
	
	@NotNull(message = "Creation by may not be null")
	@Field("creation_by")
	@CreatedBy	
	private String creationBy;	
	
	@NotNull(message = "Creation Date may not be null")
	@Field("creation_date")
	@CreatedDate
	private Date creationDate;	
		
	@Field("updated_by")
	@LastModifiedBy		
	private String updatedBy;
	
	@Field("updated_date")
	@LastModifiedDate	
	private Date updatedDate;	
}
