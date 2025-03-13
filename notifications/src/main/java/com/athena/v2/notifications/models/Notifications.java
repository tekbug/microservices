package com.athena.v2.notifications.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class Notifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;
    private String userId;
    private String eventType;
    private String correlationId;

    @Column(columnDefinition = "TIMESTAMP")
    private Instant timestamp;

    private String publisher;

    @Column(columnDefinition = "TEXT")
    private String payloadJson;

    private boolean isSuccessful;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    private Instant createdAt;

}
