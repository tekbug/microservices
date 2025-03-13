package com.athena.v2.enrollments.repositories;

import com.athena.v2.users.models.UsersActivityLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersActivityLogsRepository extends JpaRepository<UsersActivityLogs, Long> {
}
