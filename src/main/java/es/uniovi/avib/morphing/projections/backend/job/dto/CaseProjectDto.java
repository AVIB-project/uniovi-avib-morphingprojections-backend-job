package es.uniovi.avib.morphing.projections.backend.job.dto;

import java.util.Date;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CaseProjectDto {
	private String caseId;
	private String projectId;
	private String organizationId;
	private String imageId;
	private String name;
	private String description;	
	private String type;
	private String createdBy;
	private Date createdDate;
	private String updatedBy;
	private Date updatedDate;	
}
	