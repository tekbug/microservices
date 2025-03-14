package com.athena.v2.libraries.dtos.requests;

import com.athena.v2.libraries.enums.StudentStatus;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public record StudentRegistrationRequestDTO(
        @NonNull String userId,
        @NonNull String email,
        @NonNull String department,
        @NonNull String batch,
        @NonNull List<GuardianRequestDTO> guardians,
        @NonNull StudentStatus status
        ) {
}
