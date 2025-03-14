package com.athena.v2.teachers.models;

import com.athena.v2.teachers.enums.EmploymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table
public class Teachers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private EmploymentStatus employmentStatus;
    private LocalDateTime hiringDate;

    @ElementCollection
    @CollectionTable
    private List<String> specializations;

    private Instant officeHours;
}
