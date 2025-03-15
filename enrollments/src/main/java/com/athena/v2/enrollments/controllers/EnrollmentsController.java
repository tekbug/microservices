package com.athena.v2.enrollments.controllers;

import com.athena.v2.enrollments.services.EnrollmentsService;
import com.athena.v2.libraries.dtos.requests.EnrollmentRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.responses.EnrollmentRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.EnrollmentWithDetailsResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v2/enrollments")
@RequiredArgsConstructor
@EnableMethodSecurity
public class EnrollmentsController {

    private final EnrollmentsService enrollmentsService;

    @PostMapping("/create-enrollment")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR')")
    public ResponseEntity<String> createEnrollment(@Valid @RequestBody EnrollmentRegistrationRequestDTO requestDTO) {
        enrollmentsService.createEnrollment(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Enrollment has been successfully created");
    }

    @GetMapping("/get-enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'STUDENT', 'TEACHER')")
    public ResponseEntity<EnrollmentRegistrationResponseDTO> getEnrollment(@PathVariable String enrollmentId) {
        return ResponseEntity.status(HttpStatus.OK).body(enrollmentsService.getEnrollmentById(enrollmentId));
    }

    @GetMapping("/get-by-student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'STUDENT', 'TEACHER')")
    public ResponseEntity<List<EnrollmentRegistrationResponseDTO>> getEnrollmentsByStudent(@PathVariable String studentId) {
        return ResponseEntity.status(HttpStatus.OK).body(enrollmentsService.getEnrollmentsByStudent(studentId));
    }

    @GetMapping("/get-by-course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER')")
    public ResponseEntity<List<EnrollmentRegistrationResponseDTO>> getEnrollmentsByCourse(@PathVariable String courseId) {
        return ResponseEntity.status(HttpStatus.OK).body(enrollmentsService.getEnrollmentsByCourse(courseId));
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER')")
    public ResponseEntity<List<EnrollmentRegistrationResponseDTO>> getAllEnrollments() {
        return ResponseEntity.status(HttpStatus.OK).body(enrollmentsService.getAllEnrollments());
    }

    @GetMapping("/get-with-details")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR')")
    public ResponseEntity<List<EnrollmentWithDetailsResponseDTO>> getEnrollmentsWithDetails() {
        return ResponseEntity.status(HttpStatus.OK).body(enrollmentsService.getEnrollmentsWithDetails());
    }

    @PutMapping("/update-enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'TEACHER')")
    public ResponseEntity<String> updateEnrollment(
            @PathVariable String enrollmentId,
            @RequestBody EnrollmentRegistrationRequestDTO requestDTO) {
        enrollmentsService.updateEnrollment(enrollmentId, requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body("Enrollment has been updated");
    }

    @PutMapping("/drop-enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR')")
    public ResponseEntity<String> dropEnrollment(@PathVariable String enrollmentId) {
        enrollmentsService.dropEnrollment(enrollmentId);
        return ResponseEntity.status(HttpStatus.OK).body("Enrollment has been dropped");
    }
}
