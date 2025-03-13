package com.athena.v2.courses.exceptions;

public class KeycloakRegistrationIntegrationErrorException extends RuntimeException {
    public KeycloakRegistrationIntegrationErrorException(String message) {
        super(message);
    }
}