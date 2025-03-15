package com.athena.v2.assignments.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GradeSubmissionDTO(
        @NotNull(message = "Score is required")
        @Min(value = 0, message = "Score must be at least 0")
        @Max(value = 100, message = "Score cannot exceed 100")
        Integer score,

        String feedback
) {}

