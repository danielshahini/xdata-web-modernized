package com.xdata.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubmissionDTO {
    private Integer id;
    private Integer assignmentId;
    private Integer questionId;
    private String studentId;
    private String query;
    private LocalDateTime submissionTime;
    private Float marks;
    private Boolean verifiedCorrect;
    private Boolean evaluated;
}
