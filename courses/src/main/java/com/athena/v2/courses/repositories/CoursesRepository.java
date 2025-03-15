package com.athena.v2.courses.repositories;

import com.athena.v2.courses.models.Courses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoursesRepository extends JpaRepository<Courses, Long> {
    Optional<Courses> findCourseByCourseId(String courseId);
    boolean existsCourseByCourseId(String courseId);
    List<Courses> findCoursesByTeacherId(String teacherId);
    List<Courses> findCoursesByDepartment(String department);
}
