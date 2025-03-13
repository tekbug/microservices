package com.athena.v2.gateway.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${baseUrl}")
    private String baseUrl;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveClientRegistrationRepository repository) {
        return http
              .csrf(ServerHttpSecurity.CsrfSpec::disable)
              .authorizeExchange(exchange -> exchange
                      .anyExchange().authenticated())
              .oauth2Client(Customizer.withDefaults())
              .oauth2ResourceServer(OAuth2Spec -> OAuth2Spec.jwt(Customizer.withDefaults()))
              .exceptionHandling(exceptionHandlingSpec ->
                    exceptionHandlingSpec.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
              .logout(logoutSpec -> logoutSpec.logoutSuccessHandler(oidcLogoutSuccessHandler(repository)))
            .build();
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRepository) {
        var oidcClient = new OidcClientInitiatedServerLogoutSuccessHandler(clientRepository);
        oidcClient.setPostLogoutRedirectUri(baseUrl + "/logout");
        return oidcClient;
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        return ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
    }

    @Bean
    WebFilter csrfWebFilter() {
        return (exchange, chain) -> {
            exchange.getResponse().beforeCommit(() -> Mono.defer(() -> {
                Mono<CsrfToken> token = exchange.getAttribute(CsrfToken.class.getName());
                return token != null ? token.then() : Mono.empty();
            }));
            return chain.filter(exchange);
        };
    }

}
