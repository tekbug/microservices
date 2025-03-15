package com.athena.v2.assignments.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SubmissionRequestDTO(
        @NotBlank(message = "Assignment ID cannot be blank")
        String assignmentId,

        @NotBlank(message = "Student ID cannot be blank")
        String studentId,

        @NotBlank(message = "Submission link cannot be blank")
        @Pattern(regexp = "^(https?|ftp)://.*$", message = "Must be a valid URL")
        String submissionLink,

        String submissionComment
) {}

