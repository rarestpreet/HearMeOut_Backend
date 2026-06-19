package com.project.hearmeout_backend.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("HearMeOut APIs")
                                .description("by Arpit")
                )
                .servers(
                        List.of(new Server().url("https://hearmeout-backend-kbw4.onrender.com/api").description("production stage"),
                                new Server().url("http://localhost:8080/api").description("development stage"))
                )
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )
                .tags(
                        List.of(new Tag().name("Home display APIs"),
                                new Tag().name("Account authentication APIs"),
                                new Tag().name("Profile showcase APIs"),
                                new Tag().name("Post CRUD APIs"),
                                new Tag().name("Comment CRUD APIs"),
                                new Tag().name("Vote toggle APIs"),
                                new Tag().name("Tag CRUD APIS"))
                );
    }
}
