package com.athena.v2.assignments.configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.course.url}")
    private String courseServiceUrl;

    @Bean
    @Qualifier("courseServiceWebClient")
    public WebClient courseServiceWebClient() {
        return WebClient.builder()
                .baseUrl(courseServiceUrl)
                .build();
    }
}
