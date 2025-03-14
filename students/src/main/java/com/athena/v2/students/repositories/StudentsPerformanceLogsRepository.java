package com.athena.v2.students.repositories;

import com.athena.v2.students.models.StudentsPerformanceLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentsPerformanceLogsRepository extends JpaRepository<StudentsPerformanceLogs, Long> {
}
