package com.athena.v2.courses.models;

import com.athena.v2.libraries.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table
public class Courses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String courseId;
    private String courseTitle;
    private String courseDescription;
    private String department;
    private Integer creditHours;
    private String teacherId;
    private Integer maxCapacity;
    private Integer currentEnrollment;

    @ElementCollection
    @CollectionTable
    private Set<DayOfWeek> scheduleDays;

    private LocalTime startTime;
    private LocalTime endTime;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn
    private List<CoursePrerequisite> prerequisites = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

