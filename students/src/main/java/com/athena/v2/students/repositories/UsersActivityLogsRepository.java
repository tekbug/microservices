package com.athena.v2.students.repositories;

import com.athena.v2.students.models.StudentAnalyticsLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersActivityLogsRepository extends JpaRepository<StudentAnalyticsLogs, Long> {
}
