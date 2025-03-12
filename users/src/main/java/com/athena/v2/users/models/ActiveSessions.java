package com.athena.v2.users.models;

import com.athena.v2.users.enums.StatusForUsers;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveSessions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;
    private String userId;
    private Long lastAccess;
    private Long start;

    @Enumerated(EnumType.STRING)
    private StatusForUsers statusForUsers = StatusForUsers.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
