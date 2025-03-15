package com.athena.v2.courses.repositories;


import com.athena.v2.courses.models.CourseActivityLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursesActivityLogsRepository extends JpaRepository<CourseActivityLogs, Long> {
}
