package com.athena.v2.users.repositories;

import com.athena.v2.users.models.ActiveSessions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionsRepository extends JpaRepository<ActiveSessions, Long> {
}
