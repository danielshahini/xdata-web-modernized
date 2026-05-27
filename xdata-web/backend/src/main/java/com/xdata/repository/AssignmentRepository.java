package com.xdata.repository;

import com.xdata.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {
    List<Assignment> findByCourse_InstructorCourseId(String courseId);
    List<Assignment> findByCourse_InstructorCourseIdIn(List<String> courseIds);
}
