package es.uniovi.avib.morphing.projections.backend.job.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobLogDto {
	private String log;
	private String state;
	private String error;
}
