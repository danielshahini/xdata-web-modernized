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
public class SubmissionDiffDTO {
    private Integer submissionId;
    private Boolean isCorrect;
    private Float marks;
    private String details;
    private List<String> missingRows;
    private List<String> extraRows;
}
