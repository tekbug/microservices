package com.athena.v2.libraries.dtos.requests;

import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AttendanceFormRequestDTO(
        @NonNull String classroomId,
        @NonNull String courseId,
        @NonNull List<StudentAttendanceEntryRequestDTO> studentEntries,
        LocalDateTime sessionStartTime,
        LocalDateTime sessionEndTime,
        int gracePeriodMinutes
        ) {
}
