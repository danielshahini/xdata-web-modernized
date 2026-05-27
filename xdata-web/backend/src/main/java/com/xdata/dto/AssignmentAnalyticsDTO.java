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
public class AssignmentAnalyticsDTO {
    private Integer assignmentId;
    private List<QuestionAnalyticsDTO> questionAnalytics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAnalyticsDTO {
        private Integer questionId;
        private String questionName;
        private Integer totalSubmissions;
        private Integer uniqueStudents;
        private Float averageMarks;
        private Integer perfectScores;
        private List<CommonErrorDTO> commonErrors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommonErrorDTO {
        private String errorType;
        private Integer count;
    }
}
