package com.athena.v2.enrollments.exceptions;

public class EnrollmentAlreadyExistsException extends RuntimeException {
    public EnrollmentAlreadyExistsException(String message) {
        super(message);
    }
}
