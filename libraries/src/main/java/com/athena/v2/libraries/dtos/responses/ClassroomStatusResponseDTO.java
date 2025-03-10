package com.athena.v2.libraries.dtos.responses;

import com.athena.v2.libraries.enums.ClassroomStatus;
import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ClassroomStatusResponseDTO(
        @NonNull String classroomId,
        @NonNull String courseId,
        @NonNull ClassroomStatus status,
        @NonNull List<ActiveStudentsResponseDTO> activeStudentsList,
        int totalEnrolledStudents,
        int currentlyActiveStudents,
        LocalDateTime sessionStartTime,
        LocalDateTime lastUpdated
        ) {
}
