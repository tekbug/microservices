package com.athena.v2.libraries.dtos.requests;

import lombok.Builder;

import java.time.Instant;


@Builder
public record PayloadObject(
        String userId,
        String email,
        String firstName,
        String lastName,
        String userRole,
        Instant createdAt
) {}
