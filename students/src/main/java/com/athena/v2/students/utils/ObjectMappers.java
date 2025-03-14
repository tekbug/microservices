package com.athena.v2.students.utils;


import com.athena.v2.libraries.dtos.responses.GuardianResponseDTO;
import com.athena.v2.libraries.dtos.responses.StudentRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.requests.GuardianRequestDTO;
import com.athena.v2.students.dtos.requests.StudentRegistrationRequestDTO;
import com.athena.v2.students.models.Guardians;
import com.athena.v2.students.models.Students;
import com.athena.v2.students.repositories.StudentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ObjectMappers {

    private final StudentsRepository studentsRepository;

    @Transactional
    public Students mapStudentsToDatabase(StudentRegistrationRequestDTO studentRegistrationRequestDTO) {
        Students students = new Students();
        students.setUserId(studentRegistrationRequestDTO.userId());
        students.setEmail(studentRegistrationRequestDTO.email());
        students.setDepartment(studentRegistrationRequestDTO.department());
        students.setBatch(studentRegistrationRequestDTO.batch());
        students.setGuardians(mapStudentGuardiansToDatabase(studentRegistrationRequestDTO.guardians()));
        students.setStatus(studentRegistrationRequestDTO.status());
        studentsRepository.saveAndFlush(students);
        return students;
    }

    public StudentRegistrationResponseDTO mapStudentsFromDatabase(Students students) {
        return StudentRegistrationResponseDTO.builder()
                .userId(students.getUserId())
                .email(students.getEmail())
                .department(students.getDepartment())
                .batch(students.getBatch())
                .guardians(students.getGuardians()
                        .stream()
                        .map(guardians -> new GuardianResponseDTO(
                                guardians.getName(),
                                guardians.getEmail(),
                                guardians.getPhoneNumber(),
                                guardians.getRelationship()
                                ))
                        .collect(Collectors.toList())
                )
                .build();
    }

    public List<Guardians> mapStudentGuardiansToDatabase(List<GuardianRequestDTO> guardians) {
        return guardians.stream()
                .map(dto -> {
                    Guardians gd = new Guardians();
                    gd.setName(dto.name());
                    gd.setEmail(dto.email());
                    gd.setRelationship(dto.relationship());
                    gd.setPhoneNumber(dto.phoneNumber());
                    return gd;
                })
                .collect(Collectors.toList());
    }

}
