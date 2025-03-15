package com.athena.v2.libraries.dtos.requests;

import com.athena.v2.libraries.enums.CourseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Builder
public record CourseRegistrationRequestDTO(
        @NotNull String courseId,
        @NotNull String courseTitle,
        String courseDescription,
        @NotNull String department,
        @NotNull Integer creditHours,
        String teacherId,
        @NotNull Integer maxCapacity,
        Integer currentEnrollment,
        @NotNull Set<DayOfWeek> scheduleDays,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        List<CoursePrerequisiteRequestDTO> prerequisites,
        @NotNull CourseStatus status
) {
}
