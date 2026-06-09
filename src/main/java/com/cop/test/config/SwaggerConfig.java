package com.cop.test.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cop API - Customer Transaction API")
                        .version("1.0.0")
                        .description("RESTful API for managing customer accounts and fund transfers. " +
                                "This API allows creating customer accounts, transferring funds between accounts, " +
                                "and checking account balances.")
                        .contact(new Contact()
                                .name("Corp Test Team")
                                .email("support@corptest.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ));
    }
}

