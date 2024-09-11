package es.uniovi.avib.morphing.projections.backend.job.configuration;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;

@Configuration
public class KubeClientConfig {
	@Autowired
	Environment env;
	
	@Value("classpath:certs/ca.crt")
	Resource caCertData;
	
	@Value("classpath:certs/client.crt")
	Resource clientCertData;
	
	@Value("classpath:certs/client.key")
	Resource clientKeyData;
	
	@Value("${kube.url:https://172.23.0.2:8443}")
	String kubeUrl;
	
	@Bean
	public KubernetesClient getKubernetesClient() throws IOException {
		// the client will be configured using the service account called flow-sa (with de permissions)
		// attached when deploy the microservice in kubernetes (check the flow-deployment.yaml file)
		if (Arrays.asList(env.getActiveProfiles()).contains("avib") )
			return new KubernetesClientBuilder().build();
		else
			return new KubernetesClientBuilder()
					.withConfig(new ConfigBuilder()
					        .withMasterUrl(kubeUrl)
					        .withOauthToken("sha256~secret")
					        .withNamespace("default")
					        .withCaCertFile(caCertData.getFile().getAbsolutePath())
					        .withClientCertFile(clientCertData.getFile().getAbsolutePath())
					        .withClientKeyFile(clientKeyData.getFile().getAbsolutePath())
					        .withClientKeyAlgo("RSA")
				        .build())
				    .build();
	}
}
