package com.xdata.repository;

import com.xdata.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
    List<Submission> findByUser_LoginId(String loginId);
    Optional<Submission> findByUser_LoginIdAndQuestion_Id(String loginId, Integer questionId);
    List<Submission> findByUser_LoginIdAndQuestion_IdOrderBySubmissionTimeDesc(String loginId, Integer questionId);
    List<Submission> findByQuestion_Assignment_Id(Integer assignmentId);
    List<Submission> findByQuestion_Id(Integer questionId);
}
