package es.uniovi.avib.morphing.projections.backend.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class UnioviAvibMorphingprojectionsBackendJobApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnioviAvibMorphingprojectionsBackendJobApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
	    return new RestTemplate();
	}
}
