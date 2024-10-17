package com.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
@SecurityScheme(name = AppConstants.SECURITY_CONTEXT_PARAM, scheme = "bearer",
        type = SecuritySchemeType.HTTP,bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
@OpenAPIDefinition(info = @io.swagger.v3.oas.annotations.info.Info(title = "API Documentation", version = "v1"))
public class SwaggerConfig {

    @Bean
    public OpenAPI shopOpenAPI() {
        return new OpenAPI().info(new Info().title("White label multi vendor subscription app")
                .description("API documentation for the subscription model services").version("1.0")
                .contact(new Contact().name("Swamy Kunta").url("https://github.com/s713278/ECommerceApp/tree/master")
                        .email("swamy.kunta@gmail.com"))
                .license(new License().name("License").url("/")))
                .externalDocs(new ExternalDocumentation().description("Subscription Model API Documentation")
                        .url("http://localhost:8080/api/swagger-ui/index.html"));
    }
}
