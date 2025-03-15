package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.AssignmentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AssignmentResponseDTO(
        String assignmentId,
        String title,
        String description,
        String courseId,
        Integer totalPoints,
        LocalDateTime dueDate,
        AssignmentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
