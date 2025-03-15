package com.athena.v2.assignments.controllers;

import com.athena.v2.libraries.dtos.responses.AssignmentWithCourseDTO;
import com.athena.v2.assignments.services.AssignmentsService;
import com.athena.v2.libraries.dtos.requests.AssignmentRequestDTO;
import com.athena.v2.libraries.dtos.responses.AssignmentResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v2/assignments")
@RequiredArgsConstructor
@EnableMethodSecurity
public class AssignmentsController {

    private final AssignmentsService assignmentsService;

    @PostMapping("/create-assignment")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER')")
    public ResponseEntity<String> createAssignment(@Valid @RequestBody AssignmentRequestDTO requestDTO) {
        assignmentsService.createAssignment(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Assignment has been successfully created");
    }

    @GetMapping("/get-assignment/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER', 'STUDENT')")
    public ResponseEntity<AssignmentResponseDTO> getAssignment(@PathVariable String assignmentId) {
        return ResponseEntity.status(HttpStatus.OK).body(assignmentsService.getAssignmentById(assignmentId));
    }

    @GetMapping("/get-by-course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<AssignmentResponseDTO>> getAssignmentsByCourse(@PathVariable String courseId) {
        return ResponseEntity.status(HttpStatus.OK).body(assignmentsService.getAssignmentsByCourse(courseId));
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<List<AssignmentResponseDTO>> getAllAssignments() {
        return ResponseEntity.status(HttpStatus.OK).body(assignmentsService.getAllAssignments());
    }

    @GetMapping("/get-upcoming")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER', 'STUDENT')")
    public ResponseEntity<List<AssignmentResponseDTO>> getUpcomingAssignments() {
        return ResponseEntity.status(HttpStatus.OK).body(assignmentsService.getUpcomingAssignments());
    }

    @GetMapping("/get-past-due")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER')")
    public ResponseEntity<List<AssignmentResponseDTO>> getPastDueAssignments() {
        return ResponseEntity.status(HttpStatus.OK).body(assignmentsService.getPastDueAssignments());
    }

    @GetMapping("/get-with-details")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER')")
    public ResponseEntity<List<AssignmentWithCourseDTO>> getAssignmentsWithCourseDetails() {
        return ResponseEntity.status(HttpStatus.OK).body(assignmentsService.getAssignmentsWithCourseDetails());
    }

    @PutMapping("/update-assignment/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER')")
    public ResponseEntity<String> updateAssignment(
            @PathVariable String assignmentId,
            @RequestBody AssignmentRequestDTO requestDTO) {
        assignmentsService.updateAssignment(assignmentId, requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body("Assignment has been updated");
    }

    @PutMapping("/publish-assignment/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER')")
    public ResponseEntity<String> publishAssignment(@PathVariable String assignmentId) {
        assignmentsService.publishAssignment(assignmentId);
        return ResponseEntity.status(HttpStatus.OK).body("Assignment has been published");
    }

    @PutMapping("/close-assignment/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER')")
    public ResponseEntity<String> closeAssignment(@PathVariable String assignmentId) {
        assignmentsService.closeAssignment(assignmentId);
        return ResponseEntity.status(HttpStatus.OK).body("Assignment has been closed");
    }

    @DeleteMapping("/delete-assignment/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER')")
    public ResponseEntity<String> deleteAssignment(@PathVariable String assignmentId) {
        assignmentsService.deleteAssignment(assignmentId);
        return ResponseEntity.status(HttpStatus.OK).body("Assignment has been archived");
    }
}