package com.athena.v2.libraries.exceptions;

public class PageCannotBeNegativeException extends RuntimeException {
    public PageCannotBeNegativeException(String message) {
        super(message);
    }
}
