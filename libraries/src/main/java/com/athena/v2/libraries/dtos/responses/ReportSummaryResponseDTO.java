package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.AttendanceStatus;
import lombok.Builder;
import lombok.NonNull;

import java.util.Map;

@Builder
public record ReportSummaryResponseDTO(
        int totalSessions,
        int totalStudents,
        double averageAttendanceRate,
        @NonNull Map<AttendanceStatus, Integer> statusDistribution,
        @NonNull Map<String, Double> metrics
) {}
