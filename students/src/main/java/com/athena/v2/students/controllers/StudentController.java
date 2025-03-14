package com.athena.v2.students.controllers;

import com.athena.v2.libraries.dtos.requests.StudentRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.responses.StudentRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.UserResponseDTO;
import com.athena.v2.students.dtos.responses.StudentWithUserResponseDTO;
import com.athena.v2.students.services.StudentsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v2/students")
@RequiredArgsConstructor
@EnableMethodSecurity
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class StudentController {

  private final StudentsService studentsService;

  @PostMapping("/register-student")
  public ResponseEntity<String> registerStudent(@Valid @RequestBody StudentRegistrationRequestDTO requestDTO) {
    studentsService.registerStudent(requestDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body("Student has been successfully registered");
  }

  @GetMapping("get-student-info/{id}")
  public ResponseEntity<UserResponseDTO> getStudent(@PathVariable String id) {
    return ResponseEntity.status(HttpStatus.OK).body(studentsService.getStudentByUserId(id));
  }

  @GetMapping("get-student/{id}")
  public ResponseEntity<StudentRegistrationResponseDTO> getStudentById(@PathVariable String id) {
    return ResponseEntity.status(HttpStatus.OK).body(studentsService.getStudentsByUserId(id));
  }

  @GetMapping("get-all-students-info")
  public ResponseEntity<List<UserResponseDTO>> getAllStudentInfo() {
    return ResponseEntity.status(HttpStatus.OK).body(studentsService.getAllStudentsFromUserTable());
  }

  @GetMapping("get-students")
  public ResponseEntity<List<StudentWithUserResponseDTO>> getStudentsInfoInCombination() {
    return ResponseEntity.status(HttpStatus.OK).body(studentsService.getStudentsInfoCombinedWithItsUser());
  }

  @GetMapping("get-all-students")
  public ResponseEntity<List<StudentRegistrationResponseDTO>> getAllStudents() {
    return ResponseEntity.status(HttpStatus.OK).body(studentsService.getAllStudents());
  }

  @PutMapping("update-student/{id}")
  public ResponseEntity<String> updateStudent(@PathVariable String id, @RequestBody StudentRegistrationRequestDTO requestDTO) {
    studentsService.updateStudent(id, requestDTO);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Student has been updated");
  }

  @DeleteMapping("delete-student/{id}")
  public ResponseEntity<String> deleteStudent(@PathVariable String id) {
    studentsService.deleteStudent(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Student has been deleted");
  }

}
