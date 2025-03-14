package com.athena.v2.libraries.dtos.responses;

import lombok.Builder;
import lombok.NonNull;

import java.time.Instant;

@Builder
public record QualificationRegistrationResponseDTO(
        @NonNull String qualificationType,
        @NonNull String fieldOfStudy,
        Instant yearObtained,
        @NonNull String institution,
        @NonNull String documentsLink,
        Boolean isVerified
) {
}
