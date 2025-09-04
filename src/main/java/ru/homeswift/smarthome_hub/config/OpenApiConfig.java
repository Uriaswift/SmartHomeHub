package ru.homeswift.smarthome_hub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        final String BASIC = "basicAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("SmartHomeHub API")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BASIC))
                .schemaRequirement(BASIC, new SecurityScheme()
                        .name(BASIC)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic"));
    }
}