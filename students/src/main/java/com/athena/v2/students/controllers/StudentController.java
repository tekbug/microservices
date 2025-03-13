package com.athena.v2.students.controllers;

import com.athena.v2.students.dtos.requests.StudentRegistrationRequestDTO;
import com.athena.v2.students.services.StudentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("student")
@RequiredArgsConstructor
public class StudentController {

  private final StudentsService studentsService;

  @PostMapping("register")
  public ResponseEntity<String> registerStudent(@RequestBody StudentRegistrationRequestDTO requestDTO) {
    studentsService.registerStudent(requestDTO);
    return ResponseEntity.ok("Student has been successfully registered");
  }

}
