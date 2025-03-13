package com.athena.v2.students.repositories;

import com.athena.v2.students.models.Students;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentsRepository extends JpaRepository<Students, Long> {
    Optional<Students> findStudentsByUserId(String userId);

    boolean existsStudentsByUserIdAndEmail(String userId, @Email String email);
}
