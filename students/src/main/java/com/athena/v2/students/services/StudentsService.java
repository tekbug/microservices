package com.athena.v2.students.services;

import com.athena.v2.students.dtos.requests.StudentRegistrationRequestDTO;
import com.athena.v2.students.dtos.requests.UserRequestDTO;
import com.athena.v2.students.exceptions.UnauthorizedAccessException;
import com.athena.v2.students.models.Students;
import com.athena.v2.students.repositories.StudentsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentsService {

  private final StudentsRepository studentsRepository;
  private final WebClient webClient;

  @Transactional
  public void registerStudent(StudentRegistrationRequestDTO studentRegistrationRequestDTO) {

    String userId = getUserId();

    UserRequestDTO user = webClient.get()
        .uri("/users/" + userId)
        .retrieve()
        .bodyToMono(UserRequestDTO.class)
        .block();

    if (user == null) {
      throw new RuntimeException("User is not found");
    }

    Students registerStudent = new Students();
    registerStudent.setUserId(studentRegistrationRequestDTO.userId());
    registerStudent.setDepartment(studentRegistrationRequestDTO.department());
    registerStudent.setBatch(studentRegistrationRequestDTO.batch());
    studentsRepository.saveAndFlush(registerStudent);
  }

  private static String getUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assert authentication != null;

    String userId = "";

    if(authentication instanceof JwtAuthenticationToken) {
      Jwt token = ((JwtAuthenticationToken) authentication).getToken();
      userId = token.getClaimAsString("preferred_username");
      log.info("Keycloak token userId as a preferred_username component: {}", userId);
      assert userId != null;
      assert !userId.isEmpty();
    } else {
      throw new UnauthorizedAccessException("TOKEN CANNOT BE FOUND");
    }
    return userId;
  }
}
