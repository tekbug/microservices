package com.athena.v2.users.exceptions;

public class InvalidUserStatusException extends RuntimeException {
    public InvalidUserStatusException(String message) {
        super(message);
    }
}
