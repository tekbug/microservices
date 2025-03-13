package com.athena.v2.courses.exceptions;

import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ErrorResponseRecord(
        @NonNull LocalDateTime timestamp,
        int status,
        @NonNull String error,
        @NonNull List<String> message,
        @NonNull String path
        ) {
    public ErrorResponseRecord {
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("Invalid status value");
        }
    }
}
