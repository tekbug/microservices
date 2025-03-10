package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.AttendanceStatus;
import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;

@Builder
public record ActiveStudentsResponseDTO(
        @NonNull String studentId,
        @NonNull String fullName,
        LocalDateTime joinTime,
        @NonNull String deviceInfo,
        @NonNull AttendanceStatus status
        ) {}
