package com.xdata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardDTO {
    private String studentName;
    private String courseName;
    private List<AssignmentStatusDTO> assignments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignmentStatusDTO {
        private Integer assignmentId;
        private String name;
        private LocalDateTime deadline;
        private Integer totalQuestions;
        private Integer solvedQuestions;
        private Float currentMarks;
    }
}
