package com.xdata.repository;

import com.xdata.model.DeadlineExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeadlineExtensionRepository extends JpaRepository<DeadlineExtension, Integer> {
    List<DeadlineExtension> findByAssignmentId(Integer assignmentId);
    Optional<DeadlineExtension> findByAssignmentIdAndStudentLoginId(Integer assignmentId, String studentLoginId);
    void deleteByAssignmentIdAndStudentLoginId(Integer assignmentId, String studentLoginId);
}
