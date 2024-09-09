package es.uniovi.avib.morphing.projections.backend.job.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

@Configuration
public class KubeClientConfig {
	@Bean
	public KubernetesClient getKubernetesClient() {
		// the client will be configured using the service account called flow-sa (with de permissions)
		// attached when deploy the microservice in kubernetes (check the flow-deployment.yaml file)
		return new KubernetesClientBuilder().build();
	}
}
