package com.athena.v2.enrollments.configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.user.url}")
    private String userServiceUrl;

    @Value("${services.student.url}")
    private String studentServiceUrl;

    @Value("${services.course.url}")
    private String courseServiceUrl;

    @Bean
    @Qualifier("userServiceWebClient")
    public WebClient userServiceWebClient() {
        return WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    @Bean
    @Qualifier("studentServiceWebClient")
    public WebClient studentServiceWebClient() {
        return WebClient.builder()
                .baseUrl(studentServiceUrl)
                .build();
    }

    @Bean
    @Qualifier("courseServiceWebClient")
    public WebClient courseServiceWebClient() {
        return WebClient.builder()
                .baseUrl(courseServiceUrl)
                .build();
    }
}
