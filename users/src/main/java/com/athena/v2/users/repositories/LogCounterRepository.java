package com.athena.v2.users.repositories;

import com.athena.v2.users.models.LogCounter;
import com.athena.v2.users.models.LogCounterId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LogCounterRepository extends JpaRepository<LogCounter, LogCounterId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    LogCounter findByIdEndpointAndIdLogType(@Param("endpoint") String endpoint, @Param("logType") String logType);
}
