package com.xdata.repository;

import com.xdata.model.XDataUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<XDataUser, String> {
    Optional<XDataUser> findByUsername(String username);
    Optional<XDataUser> findByLoginIdIgnoreCase(String loginId);
    Optional<XDataUser> findByEmail(String email);
    java.util.List<XDataUser> findDistinctByCourses_InstructorCourseId(String courseId);
    java.util.List<XDataUser> findDistinctByCourses_InstructorCourseIdIn(java.util.Collection<String> courseIds);
    java.util.List<XDataUser> findAllByRoleIgnoreCaseAndCoursesIsEmpty(String role);
    java.util.List<XDataUser> findDistinctByCourses_Id(Integer courseId);
}
