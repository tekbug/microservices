package com.athena.v2.libraries.dtos.responses;

import lombok.Builder;

@Builder
public record TeacherWithUserResponseDTO(
        UserResponseDTO userResponseDTO,
        TeacherRegistrationResponseDTO teacherDTO
) {
}
