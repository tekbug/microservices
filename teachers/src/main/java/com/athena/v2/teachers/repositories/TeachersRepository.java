package com.athena.v2.teachers.repositories;

import com.athena.v2.teachers.models.Teachers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeachersRepository extends JpaRepository<Teachers, Long> {
}
