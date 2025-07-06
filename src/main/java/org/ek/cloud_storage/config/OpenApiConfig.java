package org.ek.cloud_storage.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cloud Storage API")
                        .description("Spring Boot Cloud Storage API with Session Authentication")
                        .version("1.0.0"));
    }
}
