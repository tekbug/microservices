package com.athena.v2.libraries.dtos.requests;

import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record NotificationRequestDTO(
        @NonNull String eventId,
        @NonNull String eventType,
        @NonNull String sourceService,
        @NonNull String message,
        @NonNull Map<String, Object> payload,
        @NonNull LocalDateTime timestamp

) {}
