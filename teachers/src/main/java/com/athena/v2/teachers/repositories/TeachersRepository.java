package com.athena.v2.teachers.repositories;

import com.athena.v2.teachers.models.Teachers;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeachersRepository extends JpaRepository<Teachers, Long> {
    Optional<Teachers> findTeachersByUserId(String userId);
    boolean existsTeachersByUserId(@NonNull String userId);
}
