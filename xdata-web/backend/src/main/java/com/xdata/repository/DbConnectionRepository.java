package com.xdata.repository;

import com.xdata.model.DbConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DbConnectionRepository extends JpaRepository<DbConnection, Integer> {
    List<DbConnection> findByCourse_InstructorCourseId(String courseId);
    List<DbConnection> findByCourse_InstructorCourseIdIn(List<String> courseIds);
}
