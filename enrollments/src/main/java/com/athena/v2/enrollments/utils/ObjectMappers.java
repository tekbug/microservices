package com.athena.v2.enrollments.utils;

import com.athena.v2.enrollments.models.Enrollments;
import com.athena.v2.libraries.dtos.requests.EnrollmentRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.responses.EnrollmentRegistrationResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ObjectMappers {

    public Enrollments mapEnrollmentToDatabase(EnrollmentRegistrationRequestDTO dto) {
        Enrollments enrollment = new Enrollments();
        enrollment.setEnrollmentId(dto.enrollmentId());
        enrollment.setStudentId(dto.studentId());
        enrollment.setCourseId(dto.courseId());
        enrollment.setStatus(dto.status());
        return enrollment;
    }

    public EnrollmentRegistrationResponseDTO mapEnrollmentFromDatabase(Enrollments enrollment) {
        return EnrollmentRegistrationResponseDTO.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .studentId(enrollment.getStudentId())
                .courseId(enrollment.getCourseId())
                .status(enrollment.getStatus())
                .enrolledAt(enrollment.getEnrolledAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }

    public List<EnrollmentRegistrationResponseDTO> mapEnrollmentsFromDatabase(List<Enrollments> enrollments) {
        return enrollments.stream()
                .map(this::mapEnrollmentFromDatabase)
                .collect(Collectors.toList());
    }
}
