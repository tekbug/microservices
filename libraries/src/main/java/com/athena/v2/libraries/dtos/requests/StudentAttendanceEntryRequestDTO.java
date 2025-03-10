package com.athena.v2.libraries.dtos.requests;

import com.athena.v2.libraries.enums.AttendanceStatus;
import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;

@Builder
public record StudentAttendanceEntryRequestDTO(
        @NonNull String studentId,
        @NonNull String fullName,
        @NonNull AttendanceStatus status,
        LocalDateTime checkInTime
) {}
