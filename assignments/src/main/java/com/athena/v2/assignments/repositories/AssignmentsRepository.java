package com.athena.v2.assignments.repositories;

import com.athena.v2.assignments.models.Assignments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentsRepository extends JpaRepository<Assignments, Long> {
    Optional<Assignments> findByAssignmentId(String assignmentId);
    boolean existsByAssignmentId(String assignmentId);
    List<Assignments> findByCourseId(String courseId);
    List<Assignments> findByDueDateBefore(LocalDateTime dateTime);
    List<Assignments> findByDueDateAfter(LocalDateTime dateTime);
}