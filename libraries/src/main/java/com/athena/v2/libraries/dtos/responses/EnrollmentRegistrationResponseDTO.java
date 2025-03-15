package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.EnrollmentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EnrollmentRegistrationResponseDTO(
        String enrollmentId,
        String studentId,
        String courseId,
        EnrollmentStatus status,
        LocalDateTime enrolledAt,
        LocalDateTime updatedAt
) {
}
