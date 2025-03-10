package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.AttendanceStatus;
import com.athena.v2.libraries.enums.SessionStatus;
import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record AttendanceStatusResponseDTO(
        @NonNull String classroomId,
        @NonNull String courseId,
        @NonNull LocalDateTime sessionStartTime,
        @NonNull LocalDateTime sessionEndTime,
        @NonNull SessionStatus sessionStatus,
        int totalEnrolled,
        int presentCount,
        int absentCount,
        int lateCount,
        @NonNull List<StudentAttendanceStatusResponseDTO> studentStatuses,
        @NonNull Map<AttendanceStatus, Integer> statusDistribution,
        @NonNull LocalDateTime lastUpdated

) {}
