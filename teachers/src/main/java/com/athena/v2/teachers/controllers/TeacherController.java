package com.athena.v2.teachers.controllers;

import com.athena.v2.libraries.dtos.requests.TeacherRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.responses.TeacherRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.UserResponseDTO;

import com.athena.v2.teachers.services.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v2/teachers")
@RequiredArgsConstructor
@EnableMethodSecurity
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class TeacherController {

  private final TeacherService teacherService;

  @PostMapping("/register-teacher")
  public ResponseEntity<String> registerTeacher(@Valid @RequestBody TeacherRegistrationRequestDTO requestDTO) {
    teacherService.registerTeacher(requestDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body("teacher has been successfully registered");
  }

  @GetMapping("get-teacher-info/{id}")
  public ResponseEntity<UserResponseDTO> getTeacher(@PathVariable String id) {
    return ResponseEntity.status(HttpStatus.OK).body(teacherService.getTeacherByUserId(id));
  }

  @GetMapping("get-teacher/{id}")
  public ResponseEntity<TeacherRegistrationResponseDTO> getTeacherById(@PathVariable String id) {
    return ResponseEntity.status(HttpStatus.OK).body(teacherService.getTeachersByUserId(id));
  }

  @GetMapping("get-all-teachers-info")
  public ResponseEntity<List<UserResponseDTO>> getAllTeacherInfo() {
    return ResponseEntity.status(HttpStatus.OK).body(teacherService.getAllTeachersFromUserTable());
  }

  @GetMapping("get-teachers")
  public ResponseEntity<List<TeacherWithUserResponseDTO>> getteachersInfoInCombination() {
    return ResponseEntity.status(HttpStatus.OK).body(teacherService.getTeachersInfoCombinedWithItsUser());
  }

  @GetMapping("get-all-teachers")
  public ResponseEntity<List<TeacherRegistrationResponseDTO>> getAllTeachers() {
    return ResponseEntity.status(HttpStatus.OK).body(teacherService.getAllTeachers());
  }

  @PutMapping("update-teacher/{id}")
  public ResponseEntity<String> updateTeacher(@PathVariable String id, @RequestBody TeacherRegistrationRequestDTO requestDTO) {
    teacherService.updateTeacher(id, requestDTO);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("teacher has been updated");
  }

  @DeleteMapping("delete-teacher/{id}")
  public ResponseEntity<String> deleteTeacher(@PathVariable String id) {
    teacherService.deleteTeacher(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body("teacher has been deleted");
  }

}
