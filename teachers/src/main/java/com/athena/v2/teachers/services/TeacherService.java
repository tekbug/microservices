package com.athena.v2.teachers.services;

import com.athena.v2.libraries.dtos.requests.TeacherRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.requests.UserRequestDTO;
import com.athena.v2.libraries.dtos.responses.TeacherRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.TeacherWithUserResponseDTO;
import com.athena.v2.libraries.dtos.responses.UserResponseDTO;
import com.athena.v2.teachers.repositories.EventsRepository;
import com.athena.v2.teachers.repositories.TeachersRepository;
import com.athena.v2.teachers.utils.ObjectMappers;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeachersRepository teachersRepository;
    private final EmailService emailService;
    private final WebClient webClient;
    private final RabbitTemplate rabbitTemplate;
    private final EventsRepository eventsRepository;
    private final ObjectMappers objectMappers;

    public void registerTeacher(TeacherRegistrationRequestDTO requestDTO) {

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
