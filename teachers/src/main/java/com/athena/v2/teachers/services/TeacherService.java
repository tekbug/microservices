package com.athena.v2.teachers.services;

import com.athena.v2.libraries.dtos.requests.TeacherRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.responses.TeacherRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {
    public void registerTeacher(@Valid TeacherRegistrationRequestDTO requestDTO) {

    }

    public UserResponseDTO getTeacherByUserId(String id) {
        return null;
    }

    public TeacherRegistrationResponseDTO getTeachersByUserId(String id) {
        return null;
    }

    public List<UserResponseDTO> getAllTeachersFromUserTable() {
        return null;
    }

    public List<TeacherWithUserResponseDTO> getTeachersInfoCombinedWithItsUser() {
        return null;
    }

    public List<TeacherRegistrationResponseDTO> getAllTeachers() {
        return null;
    }

    public void updateTeacher(String id, TeacherRegistrationRequestDTO requestDTO) {

    }

    public void deleteTeacher(String id) {

    }
}
