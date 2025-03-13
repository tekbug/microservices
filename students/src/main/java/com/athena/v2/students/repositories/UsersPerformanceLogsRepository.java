package com.athena.v2.students.repositories;

import com.athena.v2.users.models.UsersPerformanceLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersPerformanceLogsRepository extends JpaRepository<UsersPerformanceLogs, Long> {
}
