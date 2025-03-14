package com.athena.v2.libraries.dtos.requests;

import lombok.Builder;
import lombok.NonNull;

import java.time.Instant;

@Builder
public record QualificationRegistrationRequestDTO(
        @NonNull String qualificationType,
        @NonNull String fieldOfStudy,
        Instant yearObtained,
        @NonNull String institution,
        @NonNull String documentsLink,
        Boolean isVerified
) {
}
