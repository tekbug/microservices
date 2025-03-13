package com.athena.v2.students.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v2/students")
@RequiredArgsConstructor
public class StudentGradeController {

    @GetMapping("grade/{courseId}")
    public ResponseEntity<Object> getStudentGradeById(@PathVariable("courseId") String courseId) {
        return null;
    }

    @GetMapping("grade-all")
    public ResponseEntity<Object> getAllStudentGradeHistory() {
        return null;
    }

}