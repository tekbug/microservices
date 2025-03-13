package com.athena.v2.libraries.dtos.responses;

import lombok.Builder;
import lombok.NonNull;

import java.time.Instant;

@Builder
public record NotificationResponseDTO(
        @NonNull String userId,
        @NonNull String eventId,
        @NonNull String eventType,
        @NonNull String correlationId,
        @NonNull String publisher,
        @NonNull Instant timestamp,
        @NonNull String payloadJson,
        boolean isSuccessful,
        @NonNull String errorMessage
) {}
