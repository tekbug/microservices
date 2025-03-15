package com.athena.v2.enrollments.services;

import com.athena.v2.enrollments.exceptions.EnrollmentAlreadyExistsException;
import com.athena.v2.enrollments.exceptions.EnrollmentNotFoundException;
import com.athena.v2.enrollments.exceptions.UnauthorizedAccessException;
import com.athena.v2.enrollments.models.Enrollments;
import com.athena.v2.enrollments.models.Events;
import com.athena.v2.enrollments.repositories.EnrollmentsRepository;
import com.athena.v2.enrollments.repositories.EventsRepository;
import com.athena.v2.enrollments.utils.ObjectMappers;
import com.athena.v2.libraries.dtos.requests.EnrollmentRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.responses.CourseRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.EnrollmentRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.EnrollmentWithDetailsResponseDTO;
import com.athena.v2.libraries.dtos.responses.StudentRegistrationResponseDTO;
import com.athena.v2.libraries.enums.EnrollmentStatus;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnrollmentsService {

    private final EnrollmentsRepository enrollmentsRepository;
    private final EmailService emailService;
    private final WebClient userServiceWebClient;
    private final WebClient studentServiceWebClient;
    private final WebClient courseServiceWebClient;
    private final RabbitTemplate rabbitTemplate;
    private final EventsRepository eventsRepository;
    private final ObjectMappers objectMappers;

    public EnrollmentsService(
            EnrollmentsRepository enrollmentsRepository,
            EmailService emailService,
            @Qualifier("userServiceWebClient") WebClient userServiceWebClient,
            @Qualifier("studentServiceWebClient") WebClient studentServiceWebClient,
            @Qualifier("courseServiceWebClient") WebClient courseServiceWebClient,
            RabbitTemplate rabbitTemplate,
            EventsRepository eventsRepository,
            ObjectMappers objectMappers) {
        this.enrollmentsRepository = enrollmentsRepository;
        this.emailService = emailService;
        this.userServiceWebClient = userServiceWebClient;
        this.studentServiceWebClient = studentServiceWebClient;
        this.courseServiceWebClient = courseServiceWebClient;
        this.rabbitTemplate = rabbitTemplate;
        this.eventsRepository = eventsRepository;
        this.objectMappers = objectMappers;
    }

    @Transactional
    public void createEnrollment(EnrollmentRegistrationRequestDTO enrollmentRequestDTO) {

        if (validateEnrollment(enrollmentRequestDTO)) {
            throw new EnrollmentAlreadyExistsException("ENROLLMENT ALREADY EXISTS FOR THE STUDENT AND COURSE");
        }


        boolean studentExists = Boolean.TRUE.equals(userServiceWebClient.post()
                .uri("api/v2/users/exists")
                .bodyValue(Map.of("userId", enrollmentRequestDTO.studentId()))
                .headers(headers -> headers.setBearerAuth(extractToken()))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(Boolean.class);
                    } else {
                        log.error("Student existence check failed with status: {}", response.statusCode());
                        return Mono.just(Boolean.FALSE);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error checking student existence", e);
                    return Mono.just(Boolean.FALSE);
                })
                .block());

        if (!studentExists) {
            throw new IllegalArgumentException("STUDENT DOES NOT EXIST");
        }

        // Verify course exists and has capacity
        CourseRegistrationResponseDTO course = courseServiceWebClient.get()
                .uri("api/v2/courses/get-course/" + enrollmentRequestDTO.courseId())
                .headers(headers -> headers.setBearerAuth(extractToken()))
                .retrieve()
                .bodyToMono(CourseRegistrationResponseDTO.class)
                .block();

        if (course == null) {
            throw new IllegalArgumentException("COURSE DOES NOT EXIST");
        }

        if (course.currentEnrollment() >= course.maxCapacity()) {
            throw new IllegalStateException("COURSE HAS REACHED MAXIMUM CAPACITY");
        }

        // Create enrollment
        Enrollments enrollment = objectMappers.mapEnrollmentToDatabase(enrollmentRequestDTO);
        enrollmentsRepository.saveAndFlush(enrollment);

        // Increment course enrollment count
        courseServiceWebClient.post()
                .uri("api/v2/courses/increment-enrollment/" + enrollmentRequestDTO.courseId())
                .headers(headers -> headers.setBearerAuth(extractToken()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Send notification emails
        String studentEmail = getEmailFromUserService(enrollmentRequestDTO.studentId());
        if (studentEmail != null) {
            emailService.sendEmailToCourseStakeholders(
                    studentEmail,
                    "COURSE ENROLLMENT CONFIRMATION",
                    "You have been enrolled in course: " + course.courseTitle()
            );
        }

        // Create and publish event
        Events event = createEventForPublication(enrollment);
        rabbitTemplate.convertAndSend("enrollment-exchange", "enrollment.created", event);

        log.info("Enrollment created: {}", enrollment);
    }

    public EnrollmentRegistrationResponseDTO getEnrollmentById(String enrollmentId) {
        Enrollments enrollment = getEnrollmentByIdOrThrow(enrollmentId);
        return objectMappers.mapEnrollmentFromDatabase(enrollment);
    }

    public List<EnrollmentRegistrationResponseDTO> getEnrollmentsByStudent(String studentId) {
        List<Enrollments> enrollments = enrollmentsRepository.findByStudentId(studentId);
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }
        return objectMappers.mapEnrollmentsFromDatabase(enrollments);
    }

    public List<EnrollmentRegistrationResponseDTO> getEnrollmentsByCourse(String courseId) {
        List<Enrollments> enrollments = enrollmentsRepository.findByCourseId(courseId);
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }
        return objectMappers.mapEnrollmentsFromDatabase(enrollments);
    }

    public List<EnrollmentRegistrationResponseDTO> getAllEnrollments() {
        List<Enrollments> enrollments = enrollmentsRepository.findAll();
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }
        return objectMappers.mapEnrollmentsFromDatabase(enrollments);
    }

    public List<EnrollmentWithDetailsResponseDTO> getEnrollmentsWithDetails() {
        List<Enrollments> enrollments = enrollmentsRepository.findAll();

        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }

        // Get unique student and course IDs
        Set<String> studentIds = enrollments.stream()
                .map(Enrollments::getStudentId)
                .collect(Collectors.toSet());

        Set<String> courseIds = enrollments.stream()
                .map(Enrollments::getCourseId)
                .collect(Collectors.toSet());

        // Get student details
        Map<String, StudentRegistrationResponseDTO> studentMap = new HashMap<>();
        for (String studentId : studentIds) {
            try {
                StudentRegistrationResponseDTO student = studentServiceWebClient.get()
                        .uri("api/v2/students/get-student/" + studentId)
                        .headers(headers -> headers.setBearerAuth(extractToken()))
                        .retrieve()
                        .bodyToMono(StudentRegistrationResponseDTO.class)
                        .block();

                if (student != null) {
                    studentMap.put(studentId, student);
                }
            } catch (Exception e) {
                log.error("Failed to retrieve student with ID {}: {}", studentId, e.getMessage());
            }
        }

        // Get course details
        Map<String, CourseRegistrationResponseDTO> courseMap = new HashMap<>();
        for (String courseId : courseIds) {
            try {
                CourseRegistrationResponseDTO course = courseServiceWebClient.get()
                        .uri("api/v2/courses/get-course/" + courseId)
                        .headers(headers -> headers.setBearerAuth(extractToken()))
                        .retrieve()
                        .bodyToMono(CourseRegistrationResponseDTO.class)
                        .block();

                if (course != null) {
                    courseMap.put(courseId, course);
                }
            } catch (Exception e) {
                log.error("Failed to retrieve course with ID {}: {}", courseId, e.getMessage());
            }
        }

        // Combine into response DTOs
        return enrollments.stream()
                .map(enrollment -> {
                    EnrollmentRegistrationResponseDTO enrollmentDTO = objectMappers.mapEnrollmentFromDatabase(enrollment);
                    StudentRegistrationResponseDTO studentDTO = studentMap.get(enrollment.getStudentId());
                    CourseRegistrationResponseDTO courseDTO = courseMap.get(enrollment.getCourseId());

                    return EnrollmentWithDetailsResponseDTO.builder()
                            .enrollment(enrollmentDTO)
                            .student(studentDTO)
                            .course(courseDTO)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateEnrollment(String enrollmentId, EnrollmentRegistrationRequestDTO enrollmentRequestDTO) {
        Enrollments enrollment = getEnrollmentByIdOrThrow(enrollmentId);

        // Cannot change the student or course
        if (!enrollment.getStudentId().equals(enrollmentRequestDTO.studentId()) ||
                !enrollment.getCourseId().equals(enrollmentRequestDTO.courseId())) {
            throw new IllegalArgumentException("STUDENT AND COURSE CANNOT BE CHANGED");
        }

        // Check if status changed from ENROLLED to something else
        boolean wasEnrolled = enrollment.getStatus() == EnrollmentStatus.ENROLLED;
        boolean isNoLongerEnrolled = enrollmentRequestDTO.status() != EnrollmentStatus.ENROLLED;

        // Update enrollment fields
        enrollment.setStatus(enrollmentRequestDTO.status());

        enrollmentsRepository.saveAndFlush(enrollment);

        // If student has dropped/withdrawn, decrement course enrollment
        if (wasEnrolled && isNoLongerEnrolled) {
            courseServiceWebClient.post()
                    .uri("api/v2/courses/decrement-enrollment/" + enrollment.getCourseId())
                    .headers(headers -> headers.setBearerAuth(extractToken()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }

        // Send notification
        String studentEmail = getEmailFromUserService(enrollment.getStudentId());
        if (studentEmail != null) {
            String statusMessage = "Your enrollment status has been updated to: " + enrollment.getStatus();
            emailService.sendEmailToCourseStakeholders(
                    studentEmail,
                    "ENROLLMENT STATUS UPDATE",
                    statusMessage
            );
        }

        // Create and publish event
        Events event = createEventForPublication(enrollment);
        rabbitTemplate.convertAndSend("enrollment-exchange", "enrollment.updated", event);

        log.info("Enrollment updated: {}", enrollment);
    }

    @Transactional
    public void dropEnrollment(String enrollmentId) {
        Enrollments enrollment = getEnrollmentByIdOrThrow(enrollmentId);

        // Only drop if currently enrolled
        if (enrollment.getStatus() == EnrollmentStatus.ENROLLED) {
            enrollment.setStatus(EnrollmentStatus.DROPPED);
            enrollmentsRepository.saveAndFlush(enrollment);

            // Decrement course enrollment
            courseServiceWebClient.post()
                    .uri("api/v2/courses/decrement-enrollment/" + enrollment.getCourseId())
                    .headers(headers -> headers.setBearerAuth(extractToken()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Send notification
            String studentEmail = getEmailFromUserService(enrollment.getStudentId());
            if (studentEmail != null) {
                emailService.sendEmailToCourseStakeholders(
                        studentEmail,
                        "COURSE DROPPED",
                        "You have dropped the course"
                );
            }

            // Create and publish event
            Events event = createEventForPublication(enrollment);
            rabbitTemplate.convertAndSend("enrollment-exchange", "enrollment.dropped", event);

            log.info("Enrollment dropped: {}", enrollment);
        } else {
            log.warn("Cannot drop enrollment that is not in ENROLLED status");
        }
    }

    // Helper methods
    private String getEmailFromUserService(String userId) {
        try {
            return userServiceWebClient.get()
                    .uri("api/v2/users/get-email/" + userId)
                    .headers(headers -> headers.setBearerAuth(extractToken()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Error retrieving email for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    private Events createEventForPublication(Enrollments enrollment) {
        Events event = new Events();
        event.setEventId(enrollment.getEnrollmentId() + "-" + UUID.randomUUID().toString().substring(0, 8));
        event.setEventType("enrollment-exchange-events");
        event.setEntityId(enrollment.getEnrollmentId());
        eventsRepository.saveAndFlush(event);
        return event;
    }

    private Enrollments getEnrollmentByIdOrThrow(String enrollmentId) {
        return enrollmentsRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException("ENROLLMENT NOT FOUND WITH ID: " + enrollmentId));
    }

    private boolean validateEnrollment(EnrollmentRegistrationRequestDTO requestDTO) {
        return enrollmentsRepository.existsByStudentIdAndCourseId(requestDTO.studentId(), requestDTO.courseId());
    }

    private static String extractToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnauthorizedAccessException("TOKEN CANNOT BE FOUND");
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt.getTokenValue();
        } else {
            throw new UnauthorizedAccessException("AUTHENTICATION IS NOT JWT TYPE");
        }
    }
}
