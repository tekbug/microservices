package com.athena.v2.courses.repositories;

import com.athena.v2.courses.models.CoursePerformanceLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursesPerformanceLogsRepository extends JpaRepository<CoursePerformanceLogs, Long> {
}
