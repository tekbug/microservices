package com.athena.v2.teachers.repositories;


import com.athena.v2.teachers.models.TeacherActivityLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeachersActivityLogsRepository extends JpaRepository<TeacherActivityLogs, Long> {
}
