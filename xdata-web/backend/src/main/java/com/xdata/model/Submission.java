package com.xdata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "xdata_student_queries")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Submission extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("submissionId")
    @Column(name = "submission_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"courses", "password", "id", "updatedAt", "createdAt"})
    private XDataUser user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "query_id")
    @JsonIgnoreProperties({"submissions", "assignment", "updatedAt", "createdAt"})
    private Question question;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id")
    @JsonIgnoreProperties({"questions", "course", "connection", "updatedAt", "createdAt"})
    private Assignment assignment;

    @Column(name = "querystring", columnDefinition = "TEXT")
    @JsonProperty("query")
    private String query;

    private Float marks;
    
    @Column(name = "evaluation_details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "submissiontime")
    private LocalDateTime submissionTime;
    
    @Transient
    @Builder.Default
    private Boolean evaluated = false;

    @Column(name = "mark_info_json", columnDefinition = "TEXT")
    private String markInfoJson;

    @Column(name = "instructor_feedback", columnDefinition = "TEXT")
    private String instructorFeedback;
    
    @Column(name = "verifiedcorrect")
    @Builder.Default
    private Boolean verifiedCorrect = false;

    @PrePersist
    protected void onCreate() {
        if (submissionTime == null) {
            submissionTime = LocalDateTime.now();
        }
    }

    @JsonProperty("studentId")
    public String getStudentId() {
        return user != null ? user.getLoginId() : null;
    }
    
    @JsonProperty("questionId")
    public Integer getQuestionId() {
        return question != null ? question.getId() : null;
    }
}
