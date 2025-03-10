package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.AttendanceStatus;
import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;

@Builder
public record AttendanceDetailsResponseDTO(
        @NonNull String userId,
        @NonNull String fullName,
        @NonNull AttendanceStatus status,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime,
        int attendanceDurationMinutes,
        boolean withinGracePeriod
        ) {
}
