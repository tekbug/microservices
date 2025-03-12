package com.athena.v2.users.models;

import com.athena.v2.users.enums.ActionType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Data
@Table
public class UsersActivityLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String activityId;
    private String userId;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @ElementCollection
    @CollectionTable
    private List<String> actionDetails;

    private String apiEndpoint;
    private String ipAddress;
    private Boolean success;
    private String sessionId = "BACKEND_SESSIONS"; // for future use in the browser, functionality gets implemented
    private Float responseTime;

    @CreationTimestamp
    private Instant createdAt;

}
