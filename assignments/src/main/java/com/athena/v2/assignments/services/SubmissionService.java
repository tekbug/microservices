package com.athena.v2.assignments.services;

import com.athena.v2.assignments.dtos.requests.GradeSubmissionDTO;
import com.athena.v2.assignments.dtos.requests.SubmissionRequestDTO;
import com.athena.v2.assignments.dtos.responses.SubmissionResponseDTO;
import com.athena.v2.assignments.exceptions.AssignmentNotFoundException;
import com.athena.v2.assignments.exceptions.SubmissionNotFoundException;
import com.athena.v2.assignments.exceptions.UnauthorizedAccessException;
import com.athena.v2.assignments.models.Assignments;
import com.athena.v2.assignments.models.Submission;
import com.athena.v2.assignments.repositories.AssignmentsRepository;
import com.athena.v2.assignments.repositories.SubmissionsRepository;
import com.athena.v2.libraries.enums.AssignmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionsRepository submissionRepository;
    private final AssignmentsRepository assignmentsRepository;

    @Transactional
    public SubmissionResponseDTO submitAssignment(SubmissionRequestDTO request) {
        // Verify assignment exists and is published
        Assignments assignment = assignmentsRepository.findByAssignmentId(request.assignmentId())
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found with ID: " + request.assignmentId()));

        if (assignment.getStatus() != AssignmentStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot submit to an assignment that is not published");
        }

        // Check if submission would be late
        boolean isLate = LocalDateTime.now().isAfter(assignment.getDueDate());

        // Check if student has already submitted this assignment
        Submission existingSubmission = submissionRepository
                .findByAssignmentIdAndStudentId(request.assignmentId(), request.studentId())
                .orElse(null);

        if (existingSubmission != null) {
            // Update existing submission
            existingSubmission.setSubmissionLink(request.submissionLink());
            existingSubmission.setSubmissionComment(request.submissionComment());
            existingSubmission.setLate(isLate);

            Submission updated = submissionRepository.save(existingSubmission);
            log.info("Updated submission for assignment {} by student {}",
                    request.assignmentId(), request.studentId());
            return mapToSubmissionResponseDTO(updated);
        } else {
            // Create new submission
            Submission submission = new Submission();
            submission.setSubmissionId("SUB-" + UUID.randomUUID().toString().substring(0, 8));
            submission.setAssignmentId(request.assignmentId());
            submission.setStudentId(request.studentId());
            submission.setSubmissionLink(request.submissionLink());
            submission.setSubmissionComment(request.submissionComment());
            submission.setLate(isLate);

            Submission saved = submissionRepository.save(submission);
            log.info("Created new submission for assignment {} by student {}",
                    request.assignmentId(), request.studentId());
            return mapToSubmissionResponseDTO(saved);
        }
    }

    public SubmissionResponseDTO getSubmissionById(String submissionId) {
        Submission submission = submissionRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new SubmissionNotFoundException("Submission not found with ID: " + submissionId));

        // Check permissions - students can only view their own submissions
        String currentUserId = getCurrentUserId();
        if (isStudentRole() && !submission.getStudentId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("You don't have permission to access this submission");
        }

        return mapToSubmissionResponseDTO(submission);
    }

    public List<SubmissionResponseDTO> getSubmissionsByAssignment(String assignmentId) {
        // Verify the assignment exists
        assignmentsRepository.findByAssignmentId(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found with ID: " + assignmentId));

        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        return submissions.stream()
                .map(this::mapToSubmissionResponseDTO)
                .collect(Collectors.toList());
    }

    public List<SubmissionResponseDTO> getSubmissionsByStudent(String studentId) {
        // Students can only view their own submissions
        String currentUserId = getCurrentUserId();
        if (isStudentRole() && !studentId.equals(currentUserId)) {
            throw new UnauthorizedAccessException("You can only view your own submissions");
        }

        List<Submission> submissions = submissionRepository.findByStudentId(studentId);
        return submissions.stream()
                .map(this::mapToSubmissionResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubmissionResponseDTO gradeSubmission(String submissionId, GradeSubmissionDTO gradeRequest) {
        Submission submission = submissionRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new SubmissionNotFoundException("Submission not found with ID: " + submissionId));

        submission.setScore(gradeRequest.score());
        submission.setFeedback(gradeRequest.feedback());

        Submission updated = submissionRepository.save(submission);
        log.info("Graded submission {} with score {}", submissionId, gradeRequest.score());
        return mapToSubmissionResponseDTO(updated);
    }

    private SubmissionResponseDTO mapToSubmissionResponseDTO(Submission submission) {
        return new SubmissionResponseDTO(
                submission.getSubmissionId(),
                submission.getAssignmentId(),
                submission.getStudentId(),
                submission.getSubmissionLink(),
                submission.getSubmissionComment(),
                submission.getScore(),
                submission.getFeedback(),
                submission.isLate(),
                submission.getSubmittedAt(),
                submission.getUpdatedAt()
        );
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthorizedAccessException("Authentication information not found");
        }
        return authentication.getName();
    }

    private boolean isStudentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
    }
}
