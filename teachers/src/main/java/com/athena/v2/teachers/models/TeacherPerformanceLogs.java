package com.athena.v2.teachers.models;

import com.athena.v2.teachers.enums.ActionType;
import com.athena.v2.teachers.enums.OperationType;
import com.athena.v2.teachers.enums.StatusForStudents;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table
public class TeacherPerformanceLogs {

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

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    private StatusForStudents statusForStudents = StatusForStudents.ACTIVE;

    private Boolean isSucceeded;
    private Boolean isFailed;
    private String errorMessage;

    private String ipAddress;

    @CreationTimestamp
    private LocalDateTime timestamp;

}
