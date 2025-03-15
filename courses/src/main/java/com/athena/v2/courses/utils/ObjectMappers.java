package com.athena.v2.courses.utils;

import com.athena.v2.courses.models.CoursePrerequisite;
import com.athena.v2.courses.models.Courses;
import com.athena.v2.libraries.dtos.requests.CoursePrerequisiteRequestDTO;
import com.athena.v2.libraries.dtos.requests.CourseRegistrationRequestDTO;
import com.athena.v2.libraries.dtos.responses.CoursePrerequisiteResponseDTO;
import com.athena.v2.libraries.dtos.responses.CourseRegistrationResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ObjectMappers {

    public Courses mapCourseToDatabase(CourseRegistrationRequestDTO dto) {
        Courses course = new Courses();
        course.setCourseId(dto.courseId());
        course.setCourseTitle(dto.courseTitle());
        course.setCourseDescription(dto.courseDescription());
        course.setDepartment(dto.department());
        course.setCreditHours(dto.creditHours());
        course.setTeacherId(dto.teacherId());
        course.setMaxCapacity(dto.maxCapacity());
        course.setCurrentEnrollment(dto.currentEnrollment() != null ? dto.currentEnrollment() : 0);
        course.setScheduleDays(dto.scheduleDays());
        course.setStartTime(dto.startTime());
        course.setEndTime(dto.endTime());
        course.setPrerequisites(mapCoursePrerequisitesToDatabase(dto.prerequisites()));
        course.setStatus(dto.status());
        return course;
    }

    public List<CoursePrerequisite> mapCoursePrerequisitesToDatabase(
            List<CoursePrerequisiteRequestDTO> prerequisites) {
        if (prerequisites == null) {
            return List.of();
        }
        return prerequisites.stream()
                .map(this::mapPrerequisiteToDatabase)
                .collect(Collectors.toList());
    }

    private CoursePrerequisite mapPrerequisiteToDatabase(CoursePrerequisiteRequestDTO dto) {
        CoursePrerequisite prerequisite = new CoursePrerequisite();
        prerequisite.setPrerequisiteCourseId(dto.prerequisiteCourseId());
        prerequisite.setPrerequisiteCourseName(dto.prerequisiteCourseName());
        prerequisite.setMinimumGrade(dto.minimumGrade());
        return prerequisite;
    }

    public CourseRegistrationResponseDTO mapCourseFromDatabase(Courses course) {
        return CourseRegistrationResponseDTO.builder()
                .courseId(course.getCourseId())
                .courseTitle(course.getCourseTitle())
                .courseDescription(course.getCourseDescription())
                .department(course.getDepartment())
                .creditHours(course.getCreditHours())
                .teacherId(course.getTeacherId())
                .maxCapacity(course.getMaxCapacity())
                .currentEnrollment(course.getCurrentEnrollment())
                .scheduleDays(course.getScheduleDays())
                .startTime(LocalDateTime.from(course.getStartTime()))
                .endTime(LocalDateTime.from(course.getEndTime()))
                .prerequisites(mapPrerequisitesFromDatabase(course.getPrerequisites()))
                .status(course.getStatus())
                .build();
    }

    public List<CourseRegistrationResponseDTO> mapCoursesFromDatabase(List<Courses> courses) {
        return courses.stream()
                .map(this::mapCourseFromDatabase)
                .collect(Collectors.toList());
    }

    private List<CoursePrerequisiteResponseDTO> mapPrerequisitesFromDatabase(List<CoursePrerequisite> prerequisites) {
        return prerequisites.stream()
                .map(this::mapPrerequisiteFromDatabase)
                .collect(Collectors.toList());
    }

    private CoursePrerequisiteResponseDTO mapPrerequisiteFromDatabase(CoursePrerequisite prerequisite) {
        return CoursePrerequisiteResponseDTO.builder()
                .prerequisiteCourseId(prerequisite.getPrerequisiteCourseId())
                .prerequisiteCourseName(prerequisite.getPrerequisiteCourseName())
                .minimumGrade(prerequisite.getMinimumGrade())
                .build();
    }
}
