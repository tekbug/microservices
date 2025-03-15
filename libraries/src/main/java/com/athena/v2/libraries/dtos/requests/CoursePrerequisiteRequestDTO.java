package com.athena.v2.libraries.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CoursePrerequisiteRequestDTO(
        @NotNull String prerequisiteCourseId,
        @NotNull String prerequisiteCourseName,
        Integer minimumGrade
) {}
