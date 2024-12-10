package es.uniovi.avib.morphing.projections.backend.job.dto;

import java.util.HashMap;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobSubmitConverterDto {
	private String organizationId;
	private String projectId;
	private String caseId;
	private HashMap<String, String> parameters;
}
