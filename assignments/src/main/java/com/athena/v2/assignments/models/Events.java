package com.athena.v2.assignments.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;
    private String eventType;
    private String entityId;
}
