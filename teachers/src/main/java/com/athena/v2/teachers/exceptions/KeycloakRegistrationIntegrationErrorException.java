package com.athena.v2.teachers.exceptions;

public class KeycloakRegistrationIntegrationErrorException extends RuntimeException {
    public KeycloakRegistrationIntegrationErrorException(String message) {
        super(message);
    }
}