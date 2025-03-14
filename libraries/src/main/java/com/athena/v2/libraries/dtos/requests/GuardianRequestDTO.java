package com.athena.v2.libraries.dtos.requests;

import lombok.NonNull;

import java.util.List;

public record GuardianRequestDTO(
        @NonNull String name,
        @NonNull String relationship,
        @NonNull List<String> phoneNumber,
        @NonNull String email
        ) {}