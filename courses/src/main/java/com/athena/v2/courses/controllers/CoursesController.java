package com.athena.v2.courses.controllers;

import com.athena.v2.courses.services.CourseService;
import com.athena.v2.libraries.dtos.requests.CourseRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.responses.CourseRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.CourseWithTeacherResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v2/courses")
@RequiredArgsConstructor
@EnableMethodSecurity
public class CoursesController {

    private final CourseService coursesService;

    @PostMapping("/register-course")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<String> registerCourse(@Valid @RequestBody CourseRegistrationRequestDTO requestDTO) {
        coursesService.registerCourse(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Course has been successfully registered");
    }

    @GetMapping("get-course/{courseId}")
    public ResponseEntity<CourseRegistrationResponseDTO> getCourseByCourseId(@PathVariable String courseId) {
        return ResponseEntity.status(HttpStatus.OK).body(coursesService.getCourseByCourseId(courseId));
    }

    @GetMapping("get-all-courses")
    public ResponseEntity<List<CourseRegistrationResponseDTO>> getAllCourses() {
        return ResponseEntity.status(HttpStatus.OK).body(coursesService.getAllCourses());
    }

    @GetMapping("get-courses-by-department/{department}")
    public ResponseEntity<List<CourseRegistrationResponseDTO>> getCoursesByDepartment(@PathVariable String department) {
        return ResponseEntity.status(HttpStatus.OK).body(coursesService.getCoursesByDepartment(department));
    }

    @GetMapping("get-courses-by-teacher/{teacherId}")
    public ResponseEntity<List<CourseRegistrationResponseDTO>> getCoursesByTeacher(@PathVariable String teacherId) {
        return ResponseEntity.status(HttpStatus.OK).body(coursesService.getCoursesByTeacher(teacherId));
    }

    @GetMapping("get-courses-with-teachers")
    public ResponseEntity<List<CourseWithTeacherResponseDTO>> getCoursesWithTeacherInfo() {
        return ResponseEntity.status(HttpStatus.OK).body(coursesService.getCoursesInfoCombinedWithItsTeacher());
    }

    @PutMapping("update-course/{courseId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<String> updateCourse(@PathVariable String courseId, @RequestBody CourseRegistrationRequestDTO requestDTO) {
        coursesService.updateCourse(courseId, requestDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Course has been updated");
    }

    @DeleteMapping("delete-course/{courseId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<String> deleteCourse(@PathVariable String courseId) {
        coursesService.deleteCourse(courseId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Course has been deleted");
    }

    @PostMapping("increment-enrollment/{courseId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<String> incrementEnrollment(@PathVariable String courseId) {
        coursesService.incrementEnrollment(courseId);
        return ResponseEntity.status(HttpStatus.OK).body("Course enrollment incremented");
    }

    @PostMapping("decrement-enrollment/{courseId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<String> decrementEnrollment(@PathVariable String courseId) {
        coursesService.decrementEnrollment(courseId);
        return ResponseEntity.status(HttpStatus.OK).body("Course enrollment decremented");
    }
}
