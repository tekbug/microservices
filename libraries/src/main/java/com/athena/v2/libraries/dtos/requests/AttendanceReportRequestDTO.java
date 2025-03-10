package com.athena.v2.libraries.dtos.requests;

import com.athena.v2.libraries.enums.ReportFormat;
import com.athena.v2.libraries.enums.ReportGrouping;
import com.athena.v2.libraries.enums.ReportMetrics;
import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AttendanceReportRequestDTO(
        @NonNull String courseId,
        List<String> studentIds,
        LocalDateTime startDate,
        LocalDateTime endDate,
        ReportFormat format,
        ReportGrouping groupBy,
        boolean includeInactiveStudents,
        List<ReportMetrics> metrics
) {
    public AttendanceReportRequestDTO {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate cannot be greater than endDate");
        }
    }

    public String generateFileName() {
        return String.format("attendance_report_%s_%s_%s.%s",
                courseId,
                startDate.toLocalDate(),
                format.toString().toLowerCase(),
                format.getFileName()
        );
    }
}
