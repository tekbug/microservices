package com.athena.v2.students.services;

import com.athena.v2.libraries.dtos.responses.StudentRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.UserResponseDTO;
import com.athena.v2.students.dtos.requests.StudentRegistrationRequestDTO;
import com.athena.v2.students.dtos.requests.UserRequestDTO;
import com.athena.v2.students.dtos.responses.StudentWithUserResponseDTO;
import com.athena.v2.students.enums.StudentStatus;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    UserRequestDTO user = webClient.get()
        .uri("/api/v2/users/get-user/" + studentRegistrationRequestDTO.userId())
        .headers(headers -> headers.setBearerAuth(extractToken()))
        .retrieve()
        .bodyToMono(UserRequestDTO.class)
        .block();

    if (user == null) {
      throw new RuntimeException("User is not found");
    }

    if(!validateStudent(studentRegistrationRequestDTO)) {
      Students registerStudent = objectMappers.mapStudentsToDatabase(studentRegistrationRequestDTO);
      emailService.sendEmailToStudent(registerStudent.getEmail(), "STUDENT ACCOUNT CREATION", "Student Registration Success");

      log.info("Registered student body to the database. Body value: {}", registerStudent);

      createEventForPublication(registerStudent);
      Events event = createEventForPublication(registerStudent);

      rabbitTemplate.convertAndSend("student-exchange", "student.created", event);

      log.info("Published student.created event for user ID: {}", registerStudent.getUserId());
    } else {
      throw new StudentAlreadyExistsException("STUDENT ALREADY EXISTS WITH EITHER THE PROVIDED STUDENT ID OR EMAIL");
    }
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
    return Optional.of(studentsRepository.findStudentsByUserId(userId)
            .orElseThrow(() -> new StudentNotFoundException("STUDENT WITH THE GIVEN ID IS NOT FOUND")))
            .map(objectMappers::mapStudentsFromDatabase)
            .orElse(null);
  }

  public List<UserResponseDTO> getAllStudentsFromUserTable() {
    String userRole = "STUDENT";
    return Collections.singletonList(webClient.get()
            .uri("api/v2/users/get-all-users-by-roles/" + userRole)
            .headers(headers -> headers.setBearerAuth(extractToken()))
            .retrieve()
            .bodyToMono(UserResponseDTO.class)
            .block());
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

  public void updateStudent(String id, StudentRegistrationRequestDTO studentRegistrationRequestDTO) {

    Students target = getStudentByUserIdOrThrow(id);
      target.getGuardians().clear();
      target.setDepartment(studentRegistrationRequestDTO.department());
      target.setBatch(studentRegistrationRequestDTO.batch());
      target.setGuardians(objectMappers.mapStudentGuardiansToDatabase(studentRegistrationRequestDTO.guardians()));
      studentsRepository.saveAndFlush(target);

    log.info("Updated Student body: {}", target);

    createEventForPublication(target);

    Events event = createEventForPublication(target);

    rabbitTemplate.convertAndSend("student-exchange", "user.updated", event);

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
      return studentsRepository.existsStudentsByUserIdAndEmail(requestDTO.userId(), requestDTO.email());
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
    log.info("User {} logically deleted (status SUSPENDED)", student.getUserId());

    Events deleteEvent = createEventForPublication(student);
    rabbitTemplate.convertAndSend("user-exchange", "user.deleted", deleteEvent);
    log.info("Published user.deleted event for user ID: {}", student.getUserId());

  }

}
