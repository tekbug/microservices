package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.EnrollmentStatus;
import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;

@Builder
public record EnrolledStudentsResponseDTO(
        @NonNull String studentId,
        @NonNull String courseId,
        @NonNull String fullName,
        @NonNull String email,
        @NonNull EnrollmentStatus status,
        @NonNull LocalDateTime enrollmentDate,
        @NonNull LocalDateTime lastActivityDate
) {
}
