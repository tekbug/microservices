package com.athena.v2.students.utils;

import com.athena.v2.libraries.dtos.requests.StudentRegistrationRequestDTO;
import com.athena.v2.students.models.Students;
import com.athena.v2.students.repositories.StudentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ObjectMappers {

    private final StudentsRepository studentsRepository;

    @Transactional
    public Students mapStudentsToDatabase(StudentRegistrationRequestDTO studentRegistrationRequestDTO) {
        Students students = new Students();

    }
}
