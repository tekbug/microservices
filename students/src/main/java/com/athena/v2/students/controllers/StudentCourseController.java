package com.athena.v2.students.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/students")
@RequiredArgsConstructor
public class StudentCourseController {

    @GetMapping("{id}/courses")
    public ResponseEntity<Object> getStudentCoursesById(@PathVariable("id") String studentId) {
        return null;
    }

    @GetMapping("{id}/courses/{courseId}/grades")
    public ResponseEntity<Object> getStudentCourseGradeById(@PathVariable("id") String studentId, @PathVariable("courseId") String courseId) {
        return null;
    }
}
