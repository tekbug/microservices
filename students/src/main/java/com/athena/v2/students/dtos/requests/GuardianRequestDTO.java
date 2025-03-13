package com.athena.v2.students.dtos.requests;

import lombok.NonNull;

import java.util.List;

public record GuardianRequestDTO(
        @NonNull String name,
        @NonNull String relationship,
        @NonNull List<String> phoneNumber,
        @NonNull String email
        ) {}