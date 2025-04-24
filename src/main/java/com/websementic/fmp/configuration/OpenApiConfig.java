package com.websementic.fmp.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@io.swagger.v3.oas.annotations.security.SecurityScheme(
        name = "basicAuth",
        scheme = "basic",
        description = "Basic Authentication",
        type = SecuritySchemeType.HTTP)
@io.swagger.v3.oas.annotations.security.SecurityScheme(
        name = "jwt",
        scheme = "bearer",
        description = "Bearer Authentication",
        type = SecuritySchemeType.HTTP)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .paths(new Paths()
                .addPathItem("/login", new PathItem()
                        .post(new Operation()
                                .summary("User Login")
                                .description("Authenticate a user and retrieve an access token")
                                .addTagsItem("Authentication")
                                .operationId("login")
                                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                                .responses(new ApiResponses()
                                        .addApiResponse("200", new ApiResponse()
                                                .content(new Content().addMediaType("application/json",
                                                        new MediaType().schema(new Schema<>()
                                                                .type("object")
                                                                .addProperty("access_token", new Schema<String>().type("string"))
                                                        )
                                                ))
                                                .description("Successfully logged in and received access token"))
                                        .addApiResponse("401", new ApiResponse()
                                                .description("Invalid credentials")))))
                .addPathItem("/logout", new PathItem()
                        .post(new Operation()
                                .summary("User Logout")
                                .description("Logout the logged in user")
                                .addTagsItem("Authentication")
                                .operationId("logout")
                                .responses(new ApiResponses()
                                        .addApiResponse("200", new ApiResponse()
                                                .description("Successfully logged out"))))));
        openAPI.setSpecVersion(SpecVersion.V31);
        return openAPI;
    }
}