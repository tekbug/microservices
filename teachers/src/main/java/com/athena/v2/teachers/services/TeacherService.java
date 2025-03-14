package com.athena.v2.teachers.services;

import com.athena.v2.libraries.dtos.requests.TeacherRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.responses.TeacherRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.TeacherWithUserResponseDTO;
import com.athena.v2.libraries.dtos.responses.UserResponseDTO;
import com.athena.v2.libraries.enums.EmploymentStatus;
import com.athena.v2.teachers.exceptions.TeacherAlreadyExistsException;
import com.athena.v2.teachers.exceptions.TeacherNotFoundException;
import com.athena.v2.teachers.exceptions.UnauthorizedAccessException;
import com.athena.v2.teachers.models.Events;
import com.athena.v2.teachers.models.Teachers;
import com.athena.v2.teachers.repositories.EventsRepository;
import com.athena.v2.teachers.repositories.TeachersRepository;
import com.athena.v2.teachers.utils.ObjectMappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeachersRepository teachersRepository;
    private final EmailService emailService;
    private final WebClient webClient;
    private final RabbitTemplate rabbitTemplate;
    private final EventsRepository eventsRepository;
    private final ObjectMappers objectMappers;

    @Transactional
    public void registerTeacher(TeacherRegistrationRequestDTO teacherRegistrationRequestDTO) {
        if (validateTeacher(teacherRegistrationRequestDTO)) {
            throw new TeacherAlreadyExistsException("TEACHER ALREADY EXISTS WITH THE PROVIDED TEACHER ID");
        }

        boolean user = Boolean.TRUE.equals(webClient.post()
                .uri("api/v2/users/exists")
                .bodyValue(Map.of(
                        "userId", teacherRegistrationRequestDTO.userId(),
                        "email", teacherRegistrationRequestDTO.email()
                ))
                .headers(headers -> headers.setBearerAuth(extractToken()))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(Boolean.class);
                    } else if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        log.warn("User existence endpoint returned 404");
                        return Mono.just(Boolean.FALSE);
                    } else {
                        log.error("User existence check failed with status: {}", response.statusCode());
                        return Mono.just(Boolean.FALSE);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error checking user existence", e);
                    return Mono.just(Boolean.FALSE);
                })
                .block());

        if (!user) {
            throw new UnauthorizedAccessException("CREDENTIAL DOES NOT MATCH THE USER");
        }

        Teachers registerTeacher = objectMappers.mapTeachersToDatabase(teacherRegistrationRequestDTO);
        registerTeacher.setEmail(teacherRegistrationRequestDTO.email());
        teachersRepository.saveAndFlush(registerTeacher);
        emailService.sendEmailToTeacher(teacherRegistrationRequestDTO.email(), "TEACHER ACCOUNT CREATION", "Teacher Registration Success");

        log.info("Registered teacher body to the database. Body value: {}", registerTeacher);

        Events event = createEventForPublication(registerTeacher);
        rabbitTemplate.convertAndSend("teacher-exchange", "teacher.created", event);

        log.info("Published teacher.created event for user ID: {}", registerTeacher.getUserId());
    }

    public UserResponseDTO getTeacherByUserId(String userId) {
        return webClient.get()
                .uri("api/v2/users/get-user/" + userId)
                .headers(headers -> headers.setBearerAuth(extractToken()))
                .retrieve()
                .bodyToMono(UserResponseDTO.class)
                .block();
    }

    public TeacherRegistrationResponseDTO getTeachersByUserId(String userId) {
        Optional<Teachers> teacher = teachersRepository.findTeachersByUserId(userId);
        if(teacher.isPresent()) {
            Teachers target = teacher.get();
            return objectMappers.mapTeachersFromDatabase(target);
        } else {
            throw new TeacherNotFoundException("TEACHER IS NOT FOUND");
        }
    }

    public List<UserResponseDTO> getAllTeachersFromUserTable() {
        String userRole = "TEACHER";
        return webClient.get()
                .uri("api/v2/users/get-all-users-by-roles/" + userRole)
                .headers(headers -> headers.setBearerAuth(extractToken()))
                .retrieve()
                .bodyToFlux(UserResponseDTO.class)
                .collectList()
                .block();
    }

    public List<TeacherRegistrationResponseDTO> getAllTeachers() {
        List<Teachers> teachersList = teachersRepository.findAll();
        if(teachersList.isEmpty()) {
            return Collections.emptyList();
        }

        return teachersList.stream()
                .map(objectMappers::mapTeachersFromDatabase)
                .collect(Collectors.toList());
    }

    public List<TeacherWithUserResponseDTO> getTeachersInfoCombinedWithItsUser() {
        List<UserResponseDTO> teachersList = getAllTeachersFromUserTable();

        if(teachersList.isEmpty()) {
            return Collections.emptyList();
        }

        List<TeacherRegistrationResponseDTO> teachers = teachersRepository.findAll()
                .stream()
                .map(objectMappers::mapTeachersFromDatabase)
                .toList();

        Map<String, TeacherRegistrationResponseDTO> teachersMap = teachers.stream()
                .collect(Collectors.toMap(
                        TeacherRegistrationResponseDTO::userId,
                        teacher -> teacher
                ));

        return teachersList.stream()
                .map(teacher -> {
                    TeacherRegistrationResponseDTO teacherInfo = teachersMap.get(teacher.userId());

                    return TeacherWithUserResponseDTO.builder()
                            .userResponseDTO(teacher)
                            .teacherDTO(teacherInfo)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateTeacher(String id, TeacherRegistrationRequestDTO teacherRegistrationRequestDTO) {
        Teachers target = getTeacherByUserIdOrThrow(id);
        if ((!target.getUserId().equals(teacherRegistrationRequestDTO.userId())) ||
                (!target.getEmail().equals(teacherRegistrationRequestDTO.email()))) {
            throw new IllegalArgumentException("Neither the user ID nor the email can be changed.");
        }

        target.getQualifications().clear();
        target.getQualifications().addAll(objectMappers.mapTeacherQualificationsToDatabase(teacherRegistrationRequestDTO.qualifications()));

        target.setEmploymentStatus(teacherRegistrationRequestDTO.employmentStatus());
        target.setHiredDate(teacherRegistrationRequestDTO.hiredDate());
        target.setSpecializations(teacherRegistrationRequestDTO.specializations());
        target.setOfficeHours(teacherRegistrationRequestDTO.officeHours());

        teachersRepository.saveAndFlush(target);

        log.info("Updated Teacher body: {}", target);

        Events event = createEventForPublication(target);
        rabbitTemplate.convertAndSend("teacher-exchange", "teacher.updated", event);

        log.info("Published teacher.updated event for user ID: {}", target.getUserId());
    }

    private Events createEventForPublication(Teachers registerTeacher) {
        Events event = new Events();
        event.setEventId(registerTeacher.getUserId() + "-" + UUID.randomUUID().toString().substring(0, 8));
        event.setEventType("teacher-exchange-events");
        event.setEntityId(registerTeacher.getUserId());
        eventsRepository.saveAndFlush(event);
        return event;
    }

    private Teachers getTeacherByUserIdOrThrow(String id) {
        return teachersRepository.findTeachersByUserId(id)
                .orElseThrow(() -> new TeacherNotFoundException("TEACHER IS NOT FOUND WITH THE GIVEN ID: " + id));
    }

    private boolean validateTeacher(TeacherRegistrationRequestDTO requestDTO) {
        return teachersRepository.existsTeachersByUserId(requestDTO.userId());
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

    public void deleteTeacher(String id) {
        Teachers teacher = getTeacherByUserIdOrThrow(id);
        teacher.setEmploymentStatus(EmploymentStatus.TERMINATED);
        teachersRepository.saveAndFlush(teacher);
        log.info("Teacher {} logically deleted (status TERMINATED)", teacher.getUserId());

        Events deleteEvent = createEventForPublication(teacher);
        rabbitTemplate.convertAndSend("teacher-exchange", "teacher.deleted", deleteEvent);
        log.info("Published teacher.deleted event for user ID: {}", teacher.getUserId());
    }
}
