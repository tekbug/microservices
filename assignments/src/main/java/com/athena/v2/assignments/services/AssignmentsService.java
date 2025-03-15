package com.athena.v2.assignments.services;

import com.athena.v2.assignments.models.Assignments;
import com.athena.v2.libraries.dtos.responses.AssignmentWithCourseDTO;
import com.athena.v2.assignments.exceptions.AssignmentAlreadyExistsException;
import com.athena.v2.assignments.exceptions.AssignmentNotFoundException;
import com.athena.v2.assignments.exceptions.UnauthorizedAccessException;
import com.athena.v2.assignments.models.Events;
import com.athena.v2.assignments.repositories.AssignmentsRepository;
import com.athena.v2.assignments.repositories.EventsRepository;
import com.athena.v2.assignments.utils.ObjectMappers;
import com.athena.v2.libraries.dtos.requests.AssignmentRequestDTO;
import com.athena.v2.libraries.dtos.responses.AssignmentResponseDTO;
import com.athena.v2.libraries.dtos.responses.CourseRegistrationResponseDTO;
import com.athena.v2.libraries.enums.AssignmentStatus;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AssignmentsService {

    private final AssignmentsRepository assignmentsRepository;
    private final WebClient courseServiceWebClient;
    private final RabbitTemplate rabbitTemplate;
    private final EventsRepository eventsRepository;
    private final ObjectMappers objectMappers;

    public AssignmentsService(
            AssignmentsRepository assignmentsRepository,
            @Qualifier("courseServiceWebClient") WebClient courseServiceWebClient,
            RabbitTemplate rabbitTemplate,
            EventsRepository eventsRepository,
            ObjectMappers objectMappers) {
        this.assignmentsRepository = assignmentsRepository;
        this.courseServiceWebClient = courseServiceWebClient;
        this.rabbitTemplate = rabbitTemplate;
        this.eventsRepository = eventsRepository;
        this.objectMappers = objectMappers;
    }

    @Transactional
    public void createAssignment(AssignmentRequestDTO assignmentRequestDTO) {
        // Validate if assignment already exists
        if (assignmentRequestDTO.assignmentId() != null &&
                assignmentsRepository.existsByAssignmentId(assignmentRequestDTO.assignmentId())) {
            throw new AssignmentAlreadyExistsException("ASSIGNMENT ALREADY EXISTS WITH THIS ID");
        }

        // Verify course exists
        CourseRegistrationResponseDTO course = courseServiceWebClient.get()
                .uri("api/v2/courses/get-course/" + assignmentRequestDTO.courseId())
                .headers(headers -> headers.setBearerAuth(extractToken()))
                .retrieve()
                .bodyToMono(CourseRegistrationResponseDTO.class)
                .block();

        if (course == null) {
            throw new IllegalArgumentException("COURSE DOES NOT EXIST");
        }

        // Create and save assignment
        Assignments assignment = objectMappers.mapAssignmentToDatabase(assignmentRequestDTO);
        assignmentsRepository.saveAndFlush(assignment);

        // Publish event
        Events event = createEventForPublication(assignment);
        rabbitTemplate.convertAndSend("assignment-exchange", "assignment.created", event);

        log.info("Assignment created: {}", assignment);
    }

    public AssignmentResponseDTO getAssignmentById(String assignmentId) {
        Assignments assignment = getAssignmentByIdOrThrow(assignmentId);
        return objectMappers.mapAssignmentFromDatabase(assignment);
    }

    public List<AssignmentResponseDTO> getAssignmentsByCourse(String courseId) {
        List<Assignments> assignments = assignmentsRepository.findByCourseId(courseId);
        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }
        return objectMappers.mapAssignmentsFromDatabase(assignments);
    }

    public List<AssignmentResponseDTO> getAllAssignments() {
        List<Assignments> assignments = assignmentsRepository.findAll();
        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }
        return objectMappers.mapAssignmentsFromDatabase(assignments);
    }

    public List<AssignmentResponseDTO> getUpcomingAssignments() {
        List<Assignments> assignments = assignmentsRepository.findByDueDateAfter(LocalDateTime.now());
        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }
        return objectMappers.mapAssignmentsFromDatabase(assignments);
    }

    public List<AssignmentResponseDTO> getPastDueAssignments() {
        List<Assignments> assignments = assignmentsRepository.findByDueDateBefore(LocalDateTime.now());
        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }
        return objectMappers.mapAssignmentsFromDatabase(assignments);
    }

    public List<AssignmentWithCourseDTO> getAssignmentsWithCourseDetails() {
        List<Assignments> assignments = assignmentsRepository.findAll();

        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }

        // Get unique course IDs
        Set<String> courseIds = assignments.stream()
                .map(Assignments::getCourseId)
                .collect(Collectors.toSet());

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
        return assignments.stream()
                .map(assignment -> {
                    AssignmentResponseDTO assignmentDTO = objectMappers.mapAssignmentFromDatabase(assignment);
                    CourseRegistrationResponseDTO courseDTO = courseMap.get(assignment.getCourseId());

                    return AssignmentWithCourseDTO.builder()
                            .assignment(assignmentDTO)
                            .course(courseDTO)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateAssignment(String assignmentId, AssignmentRequestDTO assignmentRequestDTO) {
        Assignments assignment = getAssignmentByIdOrThrow(assignmentId);

        // Cannot change assignment ID or course ID
        if (!assignment.getAssignmentId().equals(assignmentRequestDTO.assignmentId())) {
            throw new IllegalArgumentException("ASSIGNMENT ID CANNOT BE CHANGED");
        }

        if (!assignment.getCourseId().equals(assignmentRequestDTO.courseId())) {
            throw new IllegalArgumentException("COURSE ID CANNOT BE CHANGED");
        }

        // Update fields
        assignment.setTitle(assignmentRequestDTO.title());
        assignment.setDescription(assignmentRequestDTO.description());
        assignment.setTotalPoints(assignmentRequestDTO.totalPoints());
        assignment.setDueDate(assignmentRequestDTO.dueDate());
        assignment.setStatus(assignmentRequestDTO.status());

        assignmentsRepository.saveAndFlush(assignment);

        // Publish event
        Events event = createEventForPublication(assignment);
        rabbitTemplate.convertAndSend("assignment-exchange", "assignment.updated", event);

        log.info("Assignment updated: {}", assignment);
    }

    @Transactional
    public void publishAssignment(String assignmentId) {
        Assignments assignment = getAssignmentByIdOrThrow(assignmentId);

        // Only publish if in DRAFT status
        if (assignment.getStatus() == AssignmentStatus.DRAFT) {
            assignment.setStatus(AssignmentStatus.PUBLISHED);
            assignmentsRepository.saveAndFlush(assignment);

            // Publish event
            Events event = createEventForPublication(assignment);
            rabbitTemplate.convertAndSend("assignment-exchange", "assignment.published", event);

            log.info("Assignment published: {}", assignment);
        } else {
            log.warn("Cannot publish assignment that is not in DRAFT status");
        }
    }

    @Transactional
    public void closeAssignment(String assignmentId) {
        Assignments assignment = getAssignmentByIdOrThrow(assignmentId);

        // Only close if in PUBLISHED status
        if (assignment.getStatus() == AssignmentStatus.PUBLISHED) {
            assignment.setStatus(AssignmentStatus.CLOSED);
            assignmentsRepository.saveAndFlush(assignment);

            // Publish event
            Events event = createEventForPublication(assignment);
            rabbitTemplate.convertAndSend("assignment-exchange", "assignment.closed", event);

            log.info("Assignment closed: {}", assignment);
        } else {
            log.warn("Cannot close assignment that is not in PUBLISHED status");
        }
    }

    @Transactional
    public void deleteAssignment(String assignmentId) {
        Assignments assignment = getAssignmentByIdOrThrow(assignmentId);

        // Archive instead of actual delete
        assignment.setStatus(AssignmentStatus.ARCHIVED);
        assignmentsRepository.saveAndFlush(assignment);

        // Publish event
        Events event = createEventForPublication(assignment);
        rabbitTemplate.convertAndSend("assignment-exchange", "assignment.deleted", event);

        log.info("Assignment archived: {}", assignment);
    }

    // Helper methods
    private Events createEventForPublication(Assignments assignment) {
        Events event = new Events();
        event.setEventId(assignment.getAssignmentId() + "-" + UUID.randomUUID().toString().substring(0, 8));
        event.setEventType("assignment-exchange-events");
        event.setEntityId(assignment.getAssignmentId());
        eventsRepository.saveAndFlush(event);
        return event;
    }

    private Assignments getAssignmentByIdOrThrow(String assignmentId) {
        return assignmentsRepository.findByAssignmentId(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException("ASSIGNMENT NOT FOUND WITH ID: " + assignmentId));
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
