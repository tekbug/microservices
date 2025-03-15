package com.athena.v2.enrollments.repositories;


import com.athena.v2.enrollments.models.EnrollmentActivityLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentsActivityLogsRepository extends JpaRepository<EnrollmentActivityLogs, Long> {
}
