package com.athena.v2.assignments.utils;

import com.athena.v2.assignments.models.Assignments;
import com.athena.v2.libraries.dtos.requests.AssignmentRequestDTO;
import com.athena.v2.libraries.dtos.responses.AssignmentResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ObjectMappers {

    public Assignments mapAssignmentToDatabase(AssignmentRequestDTO dto) {
        Assignments assignment = new Assignments();
        assignment.setAssignmentId(dto.assignmentId() != null ?
                dto.assignmentId() : "ASG-" + UUID.randomUUID().toString().substring(0, 8));
        assignment.setTitle(dto.title());
        assignment.setDescription(dto.description());
        assignment.setCourseId(dto.courseId());
        assignment.setTotalPoints(dto.totalPoints());
        assignment.setDueDate(dto.dueDate());
        assignment.setStatus(dto.status());
        return assignment;
    }

    public AssignmentResponseDTO mapAssignmentFromDatabase(Assignments assignment) {
        return AssignmentResponseDTO.builder()
                .assignmentId(assignment.getAssignmentId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .courseId(assignment.getCourseId())
                .totalPoints(assignment.getTotalPoints())
                .dueDate(assignment.getDueDate())
                .status(assignment.getStatus())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

    public List<AssignmentResponseDTO> mapAssignmentsFromDatabase(List<Assignments> assignments) {
        return assignments.stream()
                .map(this::mapAssignmentFromDatabase)
                .collect(Collectors.toList());
    }
}

