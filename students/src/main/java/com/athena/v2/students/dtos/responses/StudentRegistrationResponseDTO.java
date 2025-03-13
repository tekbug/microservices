package com.athena.v2.students.dtos.responses;

import lombok.Builder;

@Builder
public record StudentRegistrationResponseDTO(String userId, String email, String department, String batch) {
}
