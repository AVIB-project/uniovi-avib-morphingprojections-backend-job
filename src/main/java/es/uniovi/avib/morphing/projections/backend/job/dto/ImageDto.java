package es.uniovi.avib.morphing.projections.backend.job.dto;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageDto {
	private String imageId;
				
	private String organizationId;

	private String name;
	
	private String description;
		
	private String image;
	
	private String version;
	
	private String environment;
	
	private String command;
	
	private List<String> parameters;
	
	private String creationBy;	
	
	private Date creationDate;	
		
	private String updatedBy;
	
	private Date updatedDate;	
}
