package es.uniovi.avib.morphing.projections.backend.job.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class OrganizationConfig {
    @Value("${organization.host:localhost}")
    String host;

    @Value("${organization.port:8082}")
    String port;
}
