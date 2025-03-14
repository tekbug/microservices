package com.athena.v2.students.dtos.responses;

import com.athena.v2.libraries.dtos.responses.StudentRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.UserResponseDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record StudentWithUserResponseDTO(
        UserResponseDTO userResponseDTO,
        StudentRegistrationResponseDTO studentDTO
) {}
