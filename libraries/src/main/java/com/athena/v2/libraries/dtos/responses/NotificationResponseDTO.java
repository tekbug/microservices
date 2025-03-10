package com.athena.v2.libraries.dtos.responses;

import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record NotificationResponseDTO(
        @NonNull String id,
        @NonNull String eventType,
        @NonNull String sourceService,
        @NonNull String message,
        boolean isRead,
        LocalDateTime sentAt,
        Map<String, Object> payload
) {}
