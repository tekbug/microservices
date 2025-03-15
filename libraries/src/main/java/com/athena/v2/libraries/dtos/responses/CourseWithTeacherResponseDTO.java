package com.athena.v2.libraries.dtos.responses;

import lombok.Builder;

@Builder
public record CourseWithTeacherResponseDTO(
        CourseRegistrationResponseDTO courseDTO,
        TeacherRegistrationResponseDTO teacherDTO
) {
}
