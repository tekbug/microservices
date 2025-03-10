package com.athena.v2.libraries.dtos.requests;

import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;

@Builder
public record AttendanceCheckRequestDTO(
        @NonNull String classroomId,
        @NonNull String studentId,
        LocalDateTime checkInTime,
        @NonNull String deviceInfo,
        @NonNull String ipAddress
) {}
