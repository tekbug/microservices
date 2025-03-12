package com.athena.v2.libraries.dtos.requests;

import lombok.Builder;

import java.time.Instant;

@Builder
public record EventMetadata(
        String eventId,
        String eventType,
        String eventVersion,
        String correlationId,
        String publisher,
        Instant timestamp
) {}
