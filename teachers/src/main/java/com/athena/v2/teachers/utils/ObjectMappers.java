package com.athena.v2.teachers.utils;

import com.athena.v2.libraries.dtos.requests.QualificationRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.requests.TeacherRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.responses.QualificationRegistrationResponseDTO;
import com.athena.v2.libraries.dtos.responses.TeacherRegistrationResponseDTO;
import com.athena.v2.teachers.models.Qualifications;
import com.athena.v2.teachers.models.Teachers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ObjectMappers {

    public Teachers mapTeachersToDatabase(TeacherRegistrationRequestDTO dto) {
        Teachers teacher = new Teachers();
        teacher.setUserId(dto.userId());
        teacher.setEmail(dto.email());
        teacher.setEmploymentStatus(dto.employmentStatus());
        teacher.setHiredDate(dto.hiredDate());
        teacher.setSpecializations(dto.specializations());
        teacher.setOfficeHours(dto.officeHours());
        teacher.setQualifications(mapTeacherQualificationsToDatabase(dto.qualifications()));
        return teacher;
    }

    public List<Qualifications> mapTeacherQualificationsToDatabase(
            List<QualificationRegistrationRequestDTO> qualifications) {
        return qualifications.stream()
                .map(this::mapQualificationToDatabase)
                .collect(Collectors.toList());
    }

    private Qualifications mapQualificationToDatabase(QualificationRegistrationRequestDTO dto) {
        Qualifications qualification = new Qualifications();
        qualification.setQualificationType(dto.qualificationType());
        qualification.setFieldOfStudy(dto.fieldOfStudy());
        qualification.setYearObtained(dto.yearObtained());
        qualification.setInstitution(dto.institution());
        qualification.setDocumentLink(dto.documentsLink());
        qualification.setIsVerified(dto.isVerified() != null && dto.isVerified());
        return qualification;
    }

    public TeacherRegistrationResponseDTO mapTeachersFromDatabase(Teachers teacher) {
        return TeacherRegistrationResponseDTO.builder()
                .userId(teacher.getUserId())
                .email(teacher.getEmail())
                .employmentStatus(teacher.getEmploymentStatus())
                .hiredDate(teacher.getHiredDate())
                .specializations(teacher.getSpecializations())
                .officeHours(teacher.getOfficeHours())
                .qualifications(mapQualificationsFromDatabase(teacher.getQualifications()))
                .build();
    }

    private List<QualificationRegistrationResponseDTO> mapQualificationsFromDatabase(List<Qualifications> qualifications) {
        return qualifications.stream()
                .map(this::mapQualificationFromDatabase)
                .collect(Collectors.toList());
    }


    private QualificationRegistrationResponseDTO mapQualificationFromDatabase(Qualifications qualification) {
        return QualificationRegistrationResponseDTO.builder()
                .qualificationType(qualification.getQualificationType())
                .fieldOfStudy(qualification.getFieldOfStudy())
                .yearObtained(qualification.getYearObtained())
                .institution(qualification.getInstitution())
                .documentsLink(qualification.getDocumentLink())
                .isVerified(qualification.getIsVerified())
                .build();
    }
}
