package com.xdata.repository;

import com.xdata.model.SchemaInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SchemaRepository extends JpaRepository<SchemaInfo, Integer> {
    List<SchemaInfo> findByCourse_InstructorCourseId(String courseId);
    List<SchemaInfo> findByCourse_InstructorCourseIdIn(List<String> courseIds);
}
