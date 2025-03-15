package com.athena.v2.enrollments.models;

import com.athena.v2.libraries.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Data
@Table
public class Enrollments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String enrollmentId;
    private String studentId;
    private String courseId;

    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    @CreationTimestamp
    private LocalDateTime enrolledAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}


