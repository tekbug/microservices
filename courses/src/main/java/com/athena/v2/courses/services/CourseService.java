package com.athena.v2.courses.services;

import com.athena.v2.courses.exceptions.CourseAlreadyExistsException;
import com.athena.v2.courses.exceptions.CourseNotFoundException;
import com.athena.v2.courses.exceptions.UnauthorizedAccessException;
import com.athena.v2.courses.models.Courses;
import com.athena.v2.courses.models.Events;
import com.athena.v2.courses.repositories.CoursesRepository;
import com.athena.v2.courses.repositories.EventsRepository;
import com.athena.v2.courses.utils.ObjectMappers;
import com.athena.v2.libraries.dtos.requests.CourseRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.responses.CourseRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.CourseWithTeacherResponseDTO;
import com.athena.v2.libraries.dtos.responses.TeacherRegistrationResponseDTO;
import com.athena.v2.libraries.enums.CourseStatus;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
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
public class CourseService {

    private final CoursesRepository coursesRepository;
    private final EmailService emailService;
    private final WebClient userServiceWebClient;
    private final WebClient teacherServiceWebClient;
    private final RabbitTemplate rabbitTemplate;
    private final EventsRepository eventsRepository;
    private final ObjectMappers objectMappers;

    public CourseService(
            CoursesRepository coursesRepository,
            EmailService emailService,
            @Qualifier("userServiceWebClient") WebClient userServiceWebClient,
            @Qualifier("teacherServiceWebClient") WebClient teacherServiceWebClient,
            RabbitTemplate rabbitTemplate,
            EventsRepository eventsRepository,
            ObjectMappers objectMappers) {
        this.coursesRepository = coursesRepository;
        this.emailService = emailService;
        this.userServiceWebClient = userServiceWebClient;
        this.teacherServiceWebClient = teacherServiceWebClient;
        this.rabbitTemplate = rabbitTemplate;
        this.eventsRepository = eventsRepository;
        this.objectMappers = objectMappers;
    }

    @Transactional
    public void registerCourse(CourseRegistrationRequestDTO courseRegistrationRequestDTO) {
        if (validateCourse(courseRegistrationRequestDTO)) {
            throw new CourseAlreadyExistsException("COURSE ALREADY EXISTS WITH THE PROVIDED COURSE ID");
        }

        if (courseRegistrationRequestDTO.teacherId() != null && !courseRegistrationRequestDTO.teacherId().isEmpty()) {
            boolean teacherExists = Boolean.TRUE.equals(userServiceWebClient.post()
                    .uri("api/v2/users/exists-user")
                    .bodyValue(Map.of(
                            "userId", courseRegistrationRequestDTO.teacherId()
                    ))
                    .headers(headers -> headers.setBearerAuth(extractToken()))
                    .exchangeToMono(response -> {
                        if (response.statusCode().is2xxSuccessful()) {
                            return response.bodyToMono(Boolean.class);
                        } else if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                            log.warn("Teacher existence endpoint returned 404");
                            return Mono.just(Boolean.FALSE);
                        } else {
                            log.error("Teacher existence check failed with status: {}", response.statusCode());
                            return Mono.just(Boolean.FALSE);
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("Error checking teacher existence", e);
                        return Mono.just(Boolean.FALSE);
                    })
                    .block());

            if (!teacherExists) {
                log.warn("Assigned teacher ID {} does not exist", courseRegistrationRequestDTO.teacherId());
                throw new IllegalArgumentException("ASSIGNED TEACHER DOES NOT EXIST");
            }
        }

        Courses registerCourse = objectMappers.mapCourseToDatabase(courseRegistrationRequestDTO);
        coursesRepository.saveAndFlush(registerCourse);

        // If a teacher is assigned, notify them via email
        if (registerCourse.getTeacherId() != null && !registerCourse.getTeacherId().isEmpty()) {
            // Get teacher email from user service
            String teacherEmail = getEmailFromUserService(registerCourse.getTeacherId());
            if (teacherEmail != null) {
                emailService.sendEmailToCourseStakeholders(
                        teacherEmail,
                        "COURSE ASSIGNMENT",
                        "You have been assigned to teach course: " + registerCourse.getCourseTitle());
            }
        }

        log.info("Registered course to the database. Body value: {}", registerCourse);

        Events event = createEventForPublication(registerCourse);
        rabbitTemplate.convertAndSend("course-exchange", "course.created", event);

        log.info("Published course.created event for course ID: {}", registerCourse.getCourseId());
    }

    public CourseRegistrationResponseDTO getCourseByCourseId(String courseId) {
        Courses course = getCourseByCourseIdOrThrow(courseId);
        return objectMappers.mapCourseFromDatabase(course);
    }


    public List<CourseRegistrationResponseDTO> getAllCourses() {
        List<Courses> coursesList = coursesRepository.findAll();
        if(coursesList.isEmpty()) {
            return Collections.emptyList();
        }

        return coursesList.stream()
                .map(objectMappers::mapCourseFromDatabase)
                .collect(Collectors.toList());
    }

    public List<CourseWithTeacherResponseDTO> getCoursesInfoCombinedWithItsTeacher() {
        List<Courses> courses = coursesRepository.findAll();

        if(courses.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> teacherIds = courses.stream()
                .map(Courses::getTeacherId)
                .filter(Objects::nonNull)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toSet());

        Map<String, TeacherRegistrationResponseDTO> teachersMap = new HashMap<>();

        for (String teacherId : teacherIds) {
            try {
                TeacherRegistrationResponseDTO teacher = teacherServiceWebClient.get()
                        .uri("api/v2/teachers/get-teacher/" + teacherId)
                        .headers(headers -> headers.setBearerAuth(extractToken()))
                        .retrieve()
                        .bodyToMono(TeacherRegistrationResponseDTO.class)
                        .block();

                if (teacher != null) {
                    teachersMap.put(teacherId, teacher);
                }
            } catch (Exception e) {
                log.error("Failed to retrieve teacher with ID {}: {}", teacherId, e.getMessage());
            }
        }

        return courses.stream()
                .map(course -> {
                    CourseRegistrationResponseDTO courseDTO = objectMappers.mapCourseFromDatabase(course);
                    TeacherRegistrationResponseDTO teacherDTO = null;

                    if (course.getTeacherId() != null && !course.getTeacherId().isEmpty()) {
                        teacherDTO = teachersMap.get(course.getTeacherId());
                    }

                    return CourseWithTeacherResponseDTO.builder()
                            .courseDTO(courseDTO)
                            .teacherDTO(teacherDTO)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateCourse(String id, CourseRegistrationRequestDTO courseRegistrationRequestDTO) {
        Courses target = getCourseByCourseIdOrThrow(id);

        if (!target.getCourseId().equals(courseRegistrationRequestDTO.courseId())) {
            throw new IllegalArgumentException("Course ID cannot be changed.");
        }

        String oldTeacherId = target.getTeacherId();
        String newTeacherId = courseRegistrationRequestDTO.teacherId();

        target.getPrerequisites().clear();
        target.getPrerequisites().addAll(objectMappers.mapCoursePrerequisitesToDatabase(courseRegistrationRequestDTO.prerequisites()));

        target.setCourseTitle(courseRegistrationRequestDTO.courseTitle());
        target.setCourseDescription(courseRegistrationRequestDTO.courseDescription());
        target.setDepartment(courseRegistrationRequestDTO.department());
        target.setCreditHours(courseRegistrationRequestDTO.creditHours());
        target.setTeacherId(newTeacherId);
        target.setMaxCapacity(courseRegistrationRequestDTO.maxCapacity());
        target.setScheduleDays(courseRegistrationRequestDTO.scheduleDays());
        target.setStartTime(courseRegistrationRequestDTO.startTime());
        target.setEndTime(courseRegistrationRequestDTO.endTime());
        target.setStatus(courseRegistrationRequestDTO.status());

        coursesRepository.saveAndFlush(target);

        log.info("Updated Course: {}", target);

        if (oldTeacherId == null && newTeacherId != null || oldTeacherId != null && !oldTeacherId.equals(newTeacherId)) {

            if (oldTeacherId != null && !oldTeacherId.isEmpty()) {
                String oldTeacherEmail = getEmailFromUserService(oldTeacherId);
                if (oldTeacherEmail != null) {
                    emailService.sendEmailToCourseStakeholders(
                            oldTeacherEmail,
                            "COURSE ASSIGNMENT REMOVED",
                            "You are no longer assigned to teach course: " + target.getCourseTitle()
                    );
                }
            }

            if (newTeacherId != null && !newTeacherId.isEmpty()) {
                String newTeacherEmail = getEmailFromUserService(newTeacherId);
                if (newTeacherEmail != null) {
                    emailService.sendEmailToCourseStakeholders(
                            newTeacherEmail,
                            "NEW COURSE ASSIGNMENT",
                            "You have been assigned to teach course: " + target.getCourseTitle()
                    );
                }
            }
        }

        Events event = createEventForPublication(target);
        rabbitTemplate.convertAndSend("course-exchange", "course.updated", event);

        log.info("Published course.updated event for course ID: {}", target.getCourseId());
    }

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


    private Events createEventForPublication(Courses course) {
        Events event = new Events();
        event.setEventId(course.getCourseId() + "-" + UUID.randomUUID().toString().substring(0, 8));
        event.setEventType("course-exchange-events");
        event.setEntityId(course.getCourseId());
        eventsRepository.saveAndFlush(event);
        return event;
    }

    private Courses getCourseByCourseIdOrThrow(String id) {
        return coursesRepository.findCourseByCourseId(id)
                .orElseThrow(() -> new CourseNotFoundException("COURSE IS NOT FOUND WITH THE GIVEN ID: " + id));
    }

    private boolean validateCourse(CourseRegistrationRequestDTO requestDTO) {
        return coursesRepository.existsCourseByCourseId(requestDTO.courseId());
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

    public void deleteCourse(String id) {
        Courses course = getCourseByCourseIdOrThrow(id);
        course.setStatus(CourseStatus.CANCELLED);
        coursesRepository.saveAndFlush(course);

        log.info("Course {} logically deleted (status CANCELLED)", course.getCourseId());

        // Notify teacher if assigned
        if (course.getTeacherId() != null && !course.getTeacherId().isEmpty()) {
            String teacherEmail = getEmailFromUserService(course.getTeacherId());
            if (teacherEmail != null) {
                emailService.sendEmailToCourseStakeholders(
                        teacherEmail,
                        "COURSE CANCELLED",
                        "The course you were assigned to teach has been cancelled: " + course.getCourseTitle()
                );
            }
        }

        Events deleteEvent = createEventForPublication(course);
        rabbitTemplate.convertAndSend("course-exchange", "course.deleted", deleteEvent);

        log.info("Published course.deleted event for course ID: {}", course.getCourseId());
    }

    // Additional useful methods for course management

    public List<CourseRegistrationResponseDTO> getCoursesByDepartment(String department) {
        List<Courses> courses = coursesRepository.findCoursesByDepartment(department);
        return courses.stream()
                .map(objectMappers::mapCourseFromDatabase)
                .collect(Collectors.toList());
    }

    public List<CourseRegistrationResponseDTO> getCoursesByTeacher(String teacherId) {
        List<Courses> courses = coursesRepository.findCoursesByTeacherId(teacherId);
        return courses.stream()
                .map(objectMappers::mapCourseFromDatabase)
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementEnrollment(String courseId) {
        Courses course = getCourseByCourseIdOrThrow(courseId);

        if (course.getCurrentEnrollment() >= course.getMaxCapacity()) {
            throw new IllegalStateException("Course has reached maximum capacity");
        }

        course.setCurrentEnrollment(course.getCurrentEnrollment() + 1);
        coursesRepository.saveAndFlush(course);

        log.info("Incremented enrollment for course {}, current count: {}",
                course.getCourseId(), course.getCurrentEnrollment());
    }

    @Transactional
    public void decrementEnrollment(String courseId) {
        Courses course = getCourseByCourseIdOrThrow(courseId);

        if (course.getCurrentEnrollment() <= 0) {
            throw new IllegalStateException("Course enrollment cannot be negative");
        }

        course.setCurrentEnrollment(course.getCurrentEnrollment() - 1);
        coursesRepository.saveAndFlush(course);

        log.info("Decremented enrollment for course {}, current count: {}",
                course.getCourseId(), course.getCurrentEnrollment());
    }
}
