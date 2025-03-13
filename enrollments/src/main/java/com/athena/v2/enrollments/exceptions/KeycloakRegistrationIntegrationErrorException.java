package com.athena.v2.enrollments.exceptions;

public class KeycloakRegistrationIntegrationErrorException extends RuntimeException {
    public KeycloakRegistrationIntegrationErrorException(String message) {
        super(message);
    }
}