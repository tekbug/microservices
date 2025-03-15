package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.CourseStatus;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Builder
public record CourseRegistrationResponseDTO(
        String courseId,
        String courseTitle,
        String courseDescription,
        String department,
        Integer creditHours,
        String teacherId,
        Integer maxCapacity,
        Integer currentEnrollment,
        Set<DayOfWeek> scheduleDays,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<CoursePrerequisiteResponseDTO> prerequisites,
        CourseStatus status
) {
}