package com.athena.v2.courses.models;

import com.athena.v2.users.enums.ActionType;
import com.athena.v2.users.enums.OperationType;
import com.athena.v2.users.enums.StatusForUsers;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table
public class UsersPerformanceLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String performanceId;
    private String userId;
    private String methodName;
    private String serviceClass;


    @Enumerated(EnumType.STRING)
    private ActionType action;

    private Float responseTime;
    private Long usedMemory;

    private Boolean thresholdExceeded;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    private StatusForUsers statusForUsers = StatusForUsers.ACTIVE;

    private Boolean isSucceeded;
    private Boolean isFailed;
    private String errorMessage;

    private String ipAddress;

    @CreationTimestamp
    private LocalDateTime timestamp;

}
