package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.AttendanceStatus;
import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record AttendanceReportResponseDTO(
        @NonNull String classroomId,
        @NonNull String courseId,
        @NonNull LocalDateTime sessionDate,
        int totalStudents,
        int presentStudents,
        int absentStudents,
        int lateStudents,
        @NonNull List<AttendanceDetailsResponseDTO> attendanceDetails,
        @NonNull Map<AttendanceStatus, Integer> statusDistribution,
        @NonNull LocalDateTime lastUpdated
        ) {}
