package com.athena.v2.assignments.dtos.responses;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SubmissionResponseDTO(
        String submissionId,
        String assignmentId,
        String studentId,
        String submissionLink,
        String submissionComment,
        Integer score,
        String feedback,
        boolean isLate,
        LocalDateTime submittedAt,
        LocalDateTime updatedAt
) {}
