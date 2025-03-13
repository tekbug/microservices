package com.athena.v2.students.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v2/students")
@RequiredArgsConstructor
public class StudentAssignmentController {

    @PostMapping("assignments/{assignmentId}/submit")
    public ResponseEntity<Object> submitAssignmentById(@PathVariable("assignmentId") String assignmentId) {
        return null;
    }

    @GetMapping("assignments")
    public ResponseEntity<Object> getAllStudentAssignmentById() {
        return null;
    }

    @GetMapping("assignments/pending")
    public ResponseEntity<Object> getAllPendingAssignmentsById() {
        return null;
    }

    @GetMapping("/assignments/completed")
    public ResponseEntity<Object> getAllCompletedAssignmentById() {
        return null;
    }

    @GetMapping("submissions")
    public ResponseEntity<Object> getAllSubmittedAsssignmentsById() {
        return null;
    }
    
}