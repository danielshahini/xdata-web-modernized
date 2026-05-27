package com.xdata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSummaryDTO {
    private Integer assignmentId;
    private String assignmentName;
    private List<StudentResultDTO> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentResultDTO {
        private String studentId;
        private String studentName;
        private Float totalMarks;
        private List<QuestionMarkDTO> questionMarks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionMarkDTO {
        private Integer questionId;
        private String questionName;
        private Float marks;
    }
}
