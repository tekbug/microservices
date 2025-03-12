package com.athena.v2.users.exceptions;

public class KeycloakRegistrationIntegrationErrorException extends RuntimeException {
    public KeycloakRegistrationIntegrationErrorException(String message) {
        super(message);
    }
}