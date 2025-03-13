package com.athena.v2.notifications.exceptions;

public class NotificationParsingErrorException extends RuntimeException {
    public NotificationParsingErrorException(String message) {
        super(message);
    }
}
