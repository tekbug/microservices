package com.athena.v2.libraries.dtos.requests;

import com.athena.v2.libraries.enums.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AssignmentRequestDTO(
        String assignmentId,
        @NotNull String title,
        String description,
        @NotNull String courseId,
        @NotNull Integer totalPoints,
        @NotNull LocalDateTime dueDate,
        @NotNull AssignmentStatus status
) {
}
