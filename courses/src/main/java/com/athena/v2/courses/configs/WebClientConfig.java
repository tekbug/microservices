package com.athena.v2.courses.configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.user.url}")
    private String userServiceUrl;

    @Value("${services.teacher.url}")
    private String teacherServiceUrl;

    @Bean
    @Qualifier("userServiceWebClient")
    public WebClient userServiceWebClient() {
        return WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    @Bean
    @Qualifier("teacherServiceWebClient")
    public WebClient teacherServiceWebClient() {
        return WebClient.builder()
                .baseUrl(teacherServiceUrl)
                .build();
    }
}
