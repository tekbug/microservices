package com.athena.v2.users.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@Configuration
public class MemoryManagementConfig {

    @Bean
    public MemoryMXBean memoryMXBean() {
        return ManagementFactory.getMemoryMXBean();
    }
}
