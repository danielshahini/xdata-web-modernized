package com.xdata.repository;

import com.xdata.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer> {
    Optional<Course> findByInstructorCourseId(String instructorCourseId);
    List<Course> findAllByInstructorCourseId(String instructorCourseId);
    List<Course> findAllByInstructorCourseIdIn(List<String> instructorCourseIds);
}
