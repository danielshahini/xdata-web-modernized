package com.xdata.repository;

import com.xdata.model.RegradeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegradeRequestRepository extends JpaRepository<RegradeRequest, Integer> {
    List<RegradeRequest> findBySubmissionId(Integer submissionId);
    List<RegradeRequest> findByStatusOrderByCreatedAtDesc(String status);
    List<RegradeRequest> findBySubmissionIdIn(List<Integer> submissionIds);
}
