package es.uniovi.avib.morphing.projections.backend.job.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenAPIConfig {
	@Bean
	public OpenAPI myOpenAPI() {
		Server devServer = new Server();
		devServer.setUrl("http://localhost:8082");
		devServer.setDescription("Server URL in Development environment");

		Server prodServer = new Server();
		prodServer.setUrl("http://localhost:8082");
		prodServer.setDescription("Server URL in Production environment");

		Contact contact = new Contact();
		contact.setEmail("salinasmiguel@uniovi.es");
		contact.setName("Miguel Salinas Ganeco");
		contact.setUrl("http://isa.uniovi.es/GSDPI/");

		License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");
		
		Info info = new Info().title("Jobs API")
		.version("1.0.0")
		.contact(contact)
		.description("This API exposes endpoints to manage jobs.")
		    .license(mitLicense);
		
		return new OpenAPI().info(info).servers(List.of(devServer, prodServer));
	}
}
