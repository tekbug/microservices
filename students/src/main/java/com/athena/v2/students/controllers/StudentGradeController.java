package com.athena.v2.students.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("students/grade")
@RequiredArgsConstructor
public class StudentGradeController {

    @GetMapping("{courseId}")
    public ResponseEntity<Object> getStudentGradeById(@PathVariable("courseId") String courseId) {
        return null;
    }

    @GetMapping("all")
    public ResponseEntity<Object> getAllStudentGradeHistory() {
        return null;
    }
}