package com.athena.v2.libraries.dtos.responses;

import lombok.NonNull;

import java.util.List;

public record GuardianResponseDTO(
        @NonNull String name,
        @NonNull String relationship,
        @NonNull List<String> phoneNumber,
        @NonNull String email
) {
}
