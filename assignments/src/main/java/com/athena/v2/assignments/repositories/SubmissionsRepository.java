package com.athena.v2.assignments.repositories;

import com.athena.v2.assignments.models.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionsRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findBySubmissionId(String submissionId);
    List<Submission> findByAssignmentId(String assignmentId);
    List<Submission> findByStudentId(String studentId);
    Optional<Submission> findByAssignmentIdAndStudentId(String assignmentId, String studentId);
    boolean existsByAssignmentIdAndStudentId(String assignmentId, String studentId);
}
