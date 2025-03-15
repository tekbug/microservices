package com.athena.v2.libraries.dtos.responses;

import lombok.Builder;

@Builder
public record CoursePrerequisiteResponseDTO(
        String prerequisiteCourseId,
        String prerequisiteCourseName,
        Integer minimumGrade
) {
}

