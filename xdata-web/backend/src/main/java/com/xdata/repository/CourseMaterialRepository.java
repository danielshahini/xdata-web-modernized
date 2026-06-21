package com.xdata.repository;

import com.xdata.model.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Integer> {
    List<CourseMaterial> findByCourseIdInOrderByCreatedAtDesc(List<String> courseIds);
    List<CourseMaterial> findByCourseIdOrderByCreatedAtDesc(String courseId);
}
