package com.athena.v2.gateway.configs;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

  @Bean
  public KeyResolver keyResolver() {
    return exchange -> Mono.just("rate_limiter");
  }
}
