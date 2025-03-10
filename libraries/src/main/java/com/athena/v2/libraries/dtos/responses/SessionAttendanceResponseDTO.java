package com.athena.v2.libraries.dtos.responses;

import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record SessionAttendanceResponseDTO(
        @NonNull String sessionId,
        @NonNull LocalDateTime sessionDate,
        int totalStudents,
        int presentStudents,
        int absentStudents,
        int lateStudents,
        @NonNull List<AttendanceDetailsResponseDTO> attendanceDetails
) {}
