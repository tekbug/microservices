package com.athena.v2.users.exceptions;

public class KeycloakSessionRetrievalException extends RuntimeException {
    public KeycloakSessionRetrievalException(String message) {
        super(message);
    }
}
