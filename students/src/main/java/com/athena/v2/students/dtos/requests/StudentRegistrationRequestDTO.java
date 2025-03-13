package com.athena.v2.students.dtos.requests;

import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public record StudentRegistrationRequestDTO(
        @NonNull String userId,
        @NonNull String department,
        @NonNull String batch,
        @NonNull List<String> enrollments,
        @NonNull List<GuardianRequestDTO> guardians,
        @NonNull List<String> academicInformation
        )
    {}
