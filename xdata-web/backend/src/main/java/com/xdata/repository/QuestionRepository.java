package com.xdata.repository;

import com.xdata.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findByAssignment_Id(Integer assignmentId);
    Optional<Question> findByName(String name);
}
