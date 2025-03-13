package com.athena.v2.teachers.services;

import com.athena.v2.libraries.dtos.responses.UserResponseDTO;
import com.athena.v2.users.exceptions.KeycloakRegistrationIntegrationErrorException;
import com.athena.v2.users.exceptions.KeycloakSessionRetrievalException;
import com.athena.v2.users.exceptions.UnauthorizedAccessException;
import com.athena.v2.users.models.ActiveSessions;
import com.athena.v2.users.repositories.SessionsRepository;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    @Value("${keycloak.realm}")
    private String realm;

    private final Keycloak keycloak;
    private final SessionsRepository sessionsRepository;

    @Transactional
    public void registerUser(UserResponseDTO registrationDTO) {

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setUsername(registrationDTO.userId());
        userRepresentation.setEmail(registrationDTO.email());
        userRepresentation.setFirstName(registrationDTO.firstName());
        userRepresentation.setLastName(registrationDTO.lastName());
        userRepresentation.setAttributes(Collections.singletonMap("username", List.of(registrationDTO.username())));

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        Response response = null;
        String userId;

        try {
            response = usersResource.create(userRepresentation);

            if (response.getStatus() < 200 || response.getStatus() >= 300) {
                String errorDetails = response.readEntity(String.class);
                log.error("Error returned with {} and status of: {}", errorDetails, response.getStatus());
                throw new KeycloakRegistrationIntegrationErrorException("Error returned with " + errorDetails);
            }
            userId = CreatedResponseUtil.getCreatedId(response);
            log.info("User has been successfully created, with ID: {}", userId);
        } finally {
            if (response != null) {
                response.close();
            }
        }

        if (userId != null) {
            try {
                CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
                credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
                credentialRepresentation.setValue("password");
                credentialRepresentation.setId(userId);
                credentialRepresentation.setTemporary(true);

                UserResource userResource = realmResource.users().get(userId);
                userResource.resetPassword(credentialRepresentation);

                List<RoleRepresentation> addRoles = new ArrayList<>();
                switch (registrationDTO.userRoles()) {
                    case STUDENT -> addRoles.add(realmResource.roles().get("STUDENT").toRepresentation());
                    case TEACHER -> addRoles.add(realmResource.roles().get("TEACHER").toRepresentation());
                    case ADMINISTRATOR -> addRoles.add(realmResource.roles().get("ADMINISTRATOR").toRepresentation());
                    case DEAN -> addRoles.add(realmResource.roles().get("DEAN").toRepresentation());
                    case DEPARTMENT_HEAD -> addRoles.add(realmResource.roles().get("DEPARTMENT_HEAD").toRepresentation());
                    case PROGRAM_COORDINATOR -> addRoles.add(realmResource.roles().get("PROGRAM_COORDINATOR").toRepresentation());
                }
                userResource.roles().realmLevel().add(addRoles);
                log.info("User has been successfully created, with roles: {}", addRoles);
            } catch (Exception e) {
                log.error("Error occurred while creating user with id: {} roles", userId, e);

                try {
                    realmResource.users().get(userId).remove();
                } catch (Exception cleanUpException) {
                    log.error("Error occurred while removing user with id: {}", userId, cleanUpException);
                }
                throw new KeycloakRegistrationIntegrationErrorException("Error occurred while removing user with id: " + userId);
            }
        }
    }

    @Transactional
    public List<String> syncSessionFromKeycloak() {
        String userId = extractUserIdFromKeycloakToken();
        log.debug("syncSessionFromKeycloak for user with a user id of: {}", userId);

        try {
            List<UserSessionRepresentation> sessions = getActiveSessionForUser(userId);
            List<String> sessionIds = new ArrayList<>();

            for (UserSessionRepresentation session : sessions) {
                ActiveSessions logSession = ActiveSessions.builder()
                        .sessionId(session.getId())
                        .userId(session.getUsername())
                        .start(session.getStart())
                        .lastAccess(session.getLastAccess())
                        .build();
                log.info("Session has been successfully created, with session ID: {} for the current user with ID: {}", logSession.getSessionId(), logSession.getUserId());
                sessionsRepository.saveAndFlush(logSession);
            }
            return sessionIds;
        } catch (Exception e) {
            log.error("Error occurred while creating session for user with id: {}", userId, e);
            throw new KeycloakSessionRetrievalException("Error occurred while creating session for user with id: " + userId);
        }
    }

    private String extractUserIdFromKeycloakToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        if(authentication instanceof JwtAuthenticationToken) {
            Jwt token = ((JwtAuthenticationToken) authentication).getToken();
            String userId = token.getClaimAsString("sub");
            log.info("Keycloak token userId as a sub component: {}", userId);
            assert userId != null;
            assert !userId.isEmpty();
            return userId;
        } else {
            throw new UnauthorizedAccessException("TOKEN CANNOT BE FOUND");
        }
    }

    private List<UserSessionRepresentation> getActiveSessionForUser(String userId) {
        RealmResource realmResource = keycloak.realm(realm);
        UserResource userResource = realmResource.users().get(userId);

        try {
            List<UserSessionRepresentation> sessions = userResource.getUserSessions();
            log.debug("Retrieved {} active sessions for user: {}", sessions.size(), userId);
            return sessions;
        } catch (Exception e) {
            log.error("Error retrieving user sessions from Keycloak for user: {}", userId, e);
            throw new KeycloakSessionRetrievalException("Error retrieving sessions from Keycloak: " + e.getMessage());
        }
    }
}
