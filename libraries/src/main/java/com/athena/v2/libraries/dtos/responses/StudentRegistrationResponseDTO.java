package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.StudentStatus;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public record StudentRegistrationResponseDTO(
        @NonNull String userId,
        @NonNull String email,
        @NonNull String department,
        @NonNull String batch,
        @NonNull List<GuardianResponseDTO> guardians,
        @NonNull StudentStatus status
) {}
