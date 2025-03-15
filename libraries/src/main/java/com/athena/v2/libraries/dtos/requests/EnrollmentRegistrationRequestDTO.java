package com.athena.v2.libraries.dtos.requests;

import com.athena.v2.libraries.enums.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record EnrollmentRegistrationRequestDTO(
        @NotNull String enrollmentId,
        @NotNull String studentId,
        @NotNull String courseId,
        @NotNull EnrollmentStatus status
) {
}

