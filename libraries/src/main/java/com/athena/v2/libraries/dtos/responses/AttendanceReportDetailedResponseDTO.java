package com.athena.v2.libraries.dtos.responses;

import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record AttendanceReportDetailedResponseDTO(
        @NonNull String courseId,
        @NonNull String courseName,
        @NonNull LocalDateTime reportGeneratedAt,
        @NonNull LocalDateTime periodStart,
        @NonNull LocalDateTime periodEnd,
        @NonNull ReportSummaryResponseDTO summary,
        @NonNull List<SessionAttendanceResponseDTO> sessions,
        @NonNull Map<String, List<AttendanceTrendResponseDTO>> trends
) {
}
