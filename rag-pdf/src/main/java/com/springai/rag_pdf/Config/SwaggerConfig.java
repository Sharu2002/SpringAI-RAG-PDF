package com.springai.rag_pdf.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Your API Title")
                        .description("Your API Description")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your.email@company.com")
                                .url("Your Website"))
                        .license(new License()
                                .name("License Name")
                                .url("License URL")));
    }
}