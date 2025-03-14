package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.dtos.requests.QualificationRegistrationRequestDTO;
import com.athena.v2.libraries.enums.EmploymentStatus;
import lombok.Builder;
import lombok.NonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record TeacherRegistrationResponseDTO(
        @NonNull String userId,
        @NonNull String email,
        @NonNull EmploymentStatus employmentStatus,
        LocalDateTime hiredDate,
        @NonNull List<String> specializations,
        Instant officeHours,
        @NonNull List<QualificationRegistrationResponseDTO> qualifications
) {
}
