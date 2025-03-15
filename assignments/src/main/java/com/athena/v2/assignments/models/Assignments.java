package com.athena.v2.assignments.models;

import com.athena.v2.libraries.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table
public class Assignments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assignmentId;
    private String title;
    private String description;
    private String courseId;
    private Integer totalPoints;
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
