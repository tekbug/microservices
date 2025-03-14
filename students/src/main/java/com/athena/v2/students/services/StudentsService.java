package com.athena.v2.students.services;

import com.athena.v2.libraries.dtos.requests.StudentRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.responses.StudentRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.UserResponseDTO;
import com.athena.v2.libraries.enums.StudentStatus;
import com.athena.v2.students.dtos.responses.StudentWithUserResponseDTO;
import com.athena.v2.students.exceptions.StudentAlreadyExistsException;
import com.athena.v2.students.exceptions.StudentNotFoundException;
import com.athena.v2.students.exceptions.UnauthorizedAccessException;
import com.athena.v2.students.models.Events;
import com.athena.v2.students.models.Students;
import com.athena.v2.students.repositories.EventsRepository;
import com.athena.v2.students.repositories.StudentsRepository;
import com.athena.v2.students.utils.ObjectMappers;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
@RequiredArgsConstructor
@Slf4j
public class StudentsService {

  private final StudentsRepository studentsRepository;
  private final EmailService emailService;
  private final WebClient webClient;
  private final RabbitTemplate rabbitTemplate;
  private final EventsRepository eventsRepository;
  private final ObjectMappers objectMappers;


  @Transactional
  public void registerStudent(StudentRegistrationRequestDTO studentRegistrationRequestDTO) {

    if (validateStudent(studentRegistrationRequestDTO)) {
      throw new StudentAlreadyExistsException("STUDENT ALREADY EXISTS WITH THE PROVIDED STUDENT ID");
    }

    boolean user = Boolean.TRUE.equals(webClient.post()
            .uri("api/v2/users/exists")
            .bodyValue(Map.of(
                    "userId", studentRegistrationRequestDTO.userId(),
                    "email", studentRegistrationRequestDTO.email()
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

    Students registerStudent = objectMappers.mapStudentsToDatabase(studentRegistrationRequestDTO);
    registerStudent.setEmail(studentRegistrationRequestDTO.email());
    studentsRepository.saveAndFlush(registerStudent);
    emailService.sendEmailToStudent(studentRegistrationRequestDTO.email(), "STUDENT ACCOUNT CREATION", "Student Registration Success");

    log.info("Registered student body to the database. Body value: {}", registerStudent);

    Events event = createEventForPublication(registerStudent);
    rabbitTemplate.convertAndSend("student-exchange", "student.created", event);

    log.info("Published student.created event for user ID: {}", registerStudent.getUserId());
  }

  public UserResponseDTO getStudentByUserId(String userId) {
      return webClient.get()
              .uri("api/v2/users/get-user/" + userId)
              .headers(headers -> headers.setBearerAuth(extractToken()))
              .retrieve()
              .bodyToMono(UserResponseDTO.class)
              .block();
  }

  public StudentRegistrationResponseDTO getStudentsByUserId(String userId) {
    Optional<Students> student = studentsRepository.findStudentsByUserId(userId);
    if(student.isPresent()) {
      Students target = student.get();
      return objectMappers.mapStudentsFromDatabase(target);
    } else {
      throw new StudentNotFoundException("STUDENT IS NOT FOUND");
    }
  }

  public List<UserResponseDTO> getAllStudentsFromUserTable() {
    String userRole = "STUDENT";
    return webClient.get()
            .uri("api/v2/users/get-all-users-by-roles/" + userRole)
            .headers(headers -> headers.setBearerAuth(extractToken()))
            .retrieve()
            .bodyToFlux(UserResponseDTO.class)
            .collectList()
            .block();
  }

  public List<StudentRegistrationResponseDTO> getAllStudents() {
      List<Students> studentsList = studentsRepository.findAll();
      if(studentsList.isEmpty()) {
        return Collections.emptyList();
      }

      return studentsList.stream()
              .map(objectMappers::mapStudentsFromDatabase)
              .collect(Collectors.toList());
  }

  public List<StudentWithUserResponseDTO> getStudentsInfoCombinedWithItsUser() {

    List<UserResponseDTO> studentsList = getAllStudentsFromUserTable();

    if(studentsList.isEmpty()) {
      return Collections.emptyList();
    }

    List<StudentRegistrationResponseDTO> students = studentsRepository.findAll()
            .stream()
            .map(objectMappers::mapStudentsFromDatabase)
            .toList();

    Map<String, StudentRegistrationResponseDTO> studentsMap = students.stream()
            .collect(Collectors.toMap(
                    StudentRegistrationResponseDTO::userId,
                    student -> student
            ));

    return studentsList.stream()
            .map(student -> {
              StudentRegistrationResponseDTO studentInfo = studentsMap.get(student.userId());

              return StudentWithUserResponseDTO.builder()
                      .userResponseDTO(student)
                      .studentDTO(studentInfo)
                      .build();
            })
            .collect(Collectors.toList());
  }

  @Transactional
  public void updateStudent(String id, StudentRegistrationRequestDTO studentRegistrationRequestDTO) {

    Students target = getStudentByUserIdOrThrow(id);
    if ((!target.getUserId().equals(studentRegistrationRequestDTO.userId())) ||
            (!target.getEmail().equals(studentRegistrationRequestDTO.email()))) {
      throw new IllegalArgumentException("Neither the user ID nor the email can be changed.");
    }
    target.getGuardians().clear();
    target.getGuardians().addAll(objectMappers.mapStudentGuardiansToDatabase(studentRegistrationRequestDTO.guardians()));

    target.setDepartment(studentRegistrationRequestDTO.department());
    target.setBatch(studentRegistrationRequestDTO.batch());
    studentsRepository.saveAndFlush(target);

    log.info("Updated Student body: {}", target);

    createEventForPublication(target);

    Events event = createEventForPublication(target);

    rabbitTemplate.convertAndSend("student-exchange", "student.updated", event);

    log.info("Published user.updated event for user ID: {}", target.getUserId());

  }

  private Events createEventForPublication(Students registerStudent) {
    Events event = new Events();
    event.setEventId(registerStudent.getUserId() + "-" + UUID.randomUUID().toString().substring(0, 8));
    event.setEventType("student-exchange-events");
    event.setEntityId(registerStudent.getUserId());
    eventsRepository.saveAndFlush(event);
    return event;
  }

  private Students getStudentByUserIdOrThrow(String id) {
    return studentsRepository.findStudentsByUserId(id)
            .orElseThrow(() -> new StudentNotFoundException("STUDENT IS NOT FOUND WITH THE GIVEN ID: " + id));
  }

  private boolean validateStudent(StudentRegistrationRequestDTO requestDTO) {
      return studentsRepository.existsStudentsByUserId(requestDTO.userId());
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

  public void deleteStudent(String id) {

    Students student = getStudentByUserIdOrThrow(id);
    student.setStatus(StudentStatus.SUSPENDED);
    studentsRepository.saveAndFlush(student);
    log.info("Student {} logically deleted (status SUSPENDED)", student.getUserId());

    Events deleteEvent = createEventForPublication(student);
    rabbitTemplate.convertAndSend("student-exchange", "student.deleted", deleteEvent);
    log.info("Published user.deleted event for user ID: {}", student.getUserId());

  }

}
