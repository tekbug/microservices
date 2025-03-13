package com.athena.v2.courses.resolvers;

import com.athena.v2.libraries.dtos.requests.UserIdRequestDTO;
import com.athena.v2.users.annotations.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrentUserResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (
                parameter.getParameterType().equals(UserIdRequestDTO.class) &&
                        parameter.hasParameterAnnotation(CurrentUser.class)
        );
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken token)) {
            log.error("JWT Authentication is required but not found in the instance. Check the log here: {}", authentication);
            throw new AuthenticationCredentialsNotFoundException("NOT FOUND: No JWT Authentication found");
        }

        try {
            var jwt = token.getToken();
            var claims = jwt.getClaims();

            String userId = Optional.ofNullable(claims.get("sub"))
                    .map(Object::toString)
                    .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("NOT FOUND: User ID is not found in the JWT token"));


            String username = Optional.ofNullable(claims.get("preferred_username"))
                    .map(Object::toString)
                    .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("NOT FOUND: Username is not found in the JWT token"));

            var roles = extractRoles(claims);

            return UserIdRequestDTO.builder()
                    .userId(userId)
                    .username(username)
                    .roles(roles)
                    .build();
        } catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("NOT FOUND: JWT token is not found in the security context");
        }
    }

    private List<String> extractRoles(Map<String, Object> claims) {
        try {
            Object roles = claims.get("roles");
            if (roles instanceof List<?> rolesList) {
                return rolesList.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
            return List.of();
        } catch (ClassCastException e) {
            log.warn("JWT token contains invalid claims. Returning empty list", e);
            return List.of();
        }
    }
}
