package com.athena.v2.enrollments.repositories;

import com.athena.v2.enrollments.models.Enrollments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentsRepository extends JpaRepository<Enrollments, Long> {
    Optional<Enrollments> findByEnrollmentId(String enrollmentId);
    boolean existsByEnrollmentId(String enrollmentId);
    List<Enrollments> findByStudentId(String studentId);
    List<Enrollments> findByCourseId(String courseId);
    Optional<Enrollments> findByStudentIdAndCourseId(String studentId, String courseId);
    boolean existsByStudentIdAndCourseId(String studentId, String courseId);
}
