package es.uniovi.avib.morphing.projections.backend.job.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobSubmitDto {
	private String caseId;
	private List<String> parameters;
}
