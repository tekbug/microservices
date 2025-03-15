package com.athena.v2.assignments.controllers;

import com.athena.v2.assignments.dtos.requests.GradeSubmissionDTO;
import com.athena.v2.assignments.dtos.requests.SubmissionRequestDTO;
import com.athena.v2.assignments.dtos.responses.SubmissionResponseDTO;
import com.athena.v2.assignments.services.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v2/submissions")
@RequiredArgsConstructor
public class SubmissionsController {

    private final SubmissionService submissionService;

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionResponseDTO> submitAssignment(
            @Valid @RequestBody SubmissionRequestDTO request,
            Authentication authentication) {

        if (!request.studentId().equals(authentication.getName()) && !hasAdminRole(authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        SubmissionResponseDTO response = submissionService.submitAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{submissionId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMINISTRATOR')")
    public ResponseEntity<SubmissionResponseDTO> getSubmission(@PathVariable String submissionId) {
        SubmissionResponseDTO submission = submissionService.getSubmissionById(submissionId);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/by-assignment/{assignmentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMINISTRATOR')")
    public ResponseEntity<List<SubmissionResponseDTO>> getSubmissionsByAssignment(
            @PathVariable String assignmentId) {
        List<SubmissionResponseDTO> submissions = submissionService.getSubmissionsByAssignment(assignmentId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/by-student/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMINISTRATOR')")
    public ResponseEntity<List<SubmissionResponseDTO>> getSubmissionsByStudent(
            @PathVariable String studentId,
            Authentication authentication) {

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))
                && !studentId.equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<SubmissionResponseDTO> submissions = submissionService.getSubmissionsByStudent(studentId);
        return ResponseEntity.ok(submissions);
    }

    @PostMapping("/grade/{submissionId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMINISTRATOR')")
    public ResponseEntity<SubmissionResponseDTO> gradeSubmission(
            @PathVariable String submissionId,
            @Valid @RequestBody GradeSubmissionDTO gradeRequest) {

        SubmissionResponseDTO submission = submissionService.gradeSubmission(submissionId, gradeRequest);
        return ResponseEntity.ok(submission);
    }

    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));
    }
}
