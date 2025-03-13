package com.athena.v2.students.dtos.responses;

import lombok.Builder;
import lombok.NonNull;

import java.util.List;

@Builder
public record StudentGradeReportDTO(
        @NonNull String courseId,
        @NonNull String grade,
        @NonNull List<String> gradesOfAssessment
        ) {

}