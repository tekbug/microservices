package com.athena.v2.libraries.dtos.requests;

import com.athena.v2.libraries.enums.EmploymentStatus;
import lombok.Builder;
import lombok.NonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record TeacherRegistrationRequestDTO(
        @NonNull String userId,
        @NonNull EmploymentStatus employmentStatus,
        LocalDateTime hiredDate,
        @NonNull List<String> specializations,
        Instant officeHours,
        @NonNull List<QualificationRegistrationRequestDTO> qualifications
        ) {
}
