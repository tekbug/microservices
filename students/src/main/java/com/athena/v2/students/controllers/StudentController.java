package com.athena.v2.students.controllers;

import com.athena.v2.libraries.dtos.responses.UserResponseDTO;
import com.athena.v2.students.dtos.requests.StudentRegistrationRequestDTO;
import com.athena.v2.students.services.StudentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v2/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
@EnableMethodSecurity
public class StudentController {

  private final StudentsService studentsService;

  @PostMapping("/register-student")
  public ResponseEntity<String> registerStudent(@RequestBody StudentRegistrationRequestDTO requestDTO) {
    studentsService.registerStudent(requestDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body("Student has been successfully registered");
  }

  @GetMapping("get-student/{id}")
  public ResponseEntity<UserResponseDTO> getStudent(@PathVariable String id) {
    return ResponseEntity.status(HttpStatus.OK).body(studentsService.getStudentByUserId(id));
  }



  @PutMapping("update-student/{id}")
  public ResponseEntity<String> updateStudent(@PathVariable String id, @RequestBody StudentRegistrationRequestDTO requestDTO) {
    studentsService.updateStudent(id, requestDTO);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Student has been updated");
  }


}
