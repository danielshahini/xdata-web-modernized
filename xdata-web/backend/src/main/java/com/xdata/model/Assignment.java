package com.xdata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "xdata_assignment")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Assignment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    @JsonIgnoreProperties({"users", "updatedAt", "createdAt"})
    private Course course;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "connection_id")
    @JsonIgnoreProperties({"course", "password", "updatedAt", "createdAt"})
    private DbConnection connection;

    @Column(name = "defaultschemaid")
    private Integer defaultSchemaId;

    @Column(name = "assignment_name")
    @NotBlank
    @JsonProperty("name")
    @com.fasterxml.jackson.annotation.JsonAlias({"assignmentName", "text"})
    private String name;

    @Column(name = "late_submission_allowed")
    @JsonProperty("lateSubmissionAllowed")
    private Boolean lateSubmissionAllowed;

    @Column(name = "deadline")
    @JsonProperty("deadline")
    private LocalDateTime deadline;

    @Column(name = "soft_deadline")
    @JsonProperty("softDeadline")
    private LocalDateTime softDeadline;

    @Column(name = "penalty_percentage")
    @JsonProperty("penaltyPercentage")
    private Float penaltyPercentage;

    @Column(name = "published_date")
    @JsonProperty("publishedDate")
    private LocalDateTime publishedDate;

    // null = unlimited attempts per question
    @Column(name = "max_attempts")
    @JsonProperty("maxAttempts")
    private Integer maxAttempts;

    // when false, grades are withheld from students until the instructor releases them
    @Column(name = "grades_released")
    @JsonProperty("gradesReleased")
    @Builder.Default
    private Boolean gradesReleased = true;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @JsonProperty("courseId")
    public String getCourseId() {
        return course != null ? course.getInstructorCourseId() : null;
    }
}
