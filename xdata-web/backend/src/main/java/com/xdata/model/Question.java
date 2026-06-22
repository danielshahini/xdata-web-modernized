package com.xdata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "xdata_queries")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Question extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "query_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id")
    @JsonIgnore
    private Assignment assignment;

    @Transient
    @JsonProperty("assignmentId")
    @com.fasterxml.jackson.annotation.JsonAlias({"assignment_id"})
    private Integer assignmentId;

    @Column(name = "query_string", length = 5000)
    @JsonProperty("instructorQuery")
    @com.fasterxml.jackson.annotation.JsonAlias({"queryString", "query"})
    private String instructorQuery;

    @Column(name = "query_name")
    @JsonProperty("name")
    @com.fasterxml.jackson.annotation.JsonAlias({"questionName", "text"})
    private String name;

    @Column(name = "marks")
    @JsonProperty("marks")
    private Float marks;

    // Comma-separated topic tags, e.g. "JOIN, GROUP BY".
    @Column(name = "tags")
    @JsonProperty("tags")
    private String tags;

    // Progressive hints, one per line.
    @Column(name = "hints", columnDefinition = "TEXT")
    @JsonProperty("hints")
    private String hints;

    // Teacher-set difficulty: "EASY" | "MEDIUM" | "HARD". Nullable — when unset the
    // frontend derives difficulty from the question's marks.
    @Column(name = "difficulty", length = 16)
    @JsonProperty("difficulty")
    private String difficulty;

    // Natural-language task statement shown to students (the actual problem text,
    // separate from the short name/title). Nullable.
    @Column(name = "description", columnDefinition = "TEXT")
    @JsonProperty("description")
    private String description;

    @Column(name = "partial_mark_info", columnDefinition = "TEXT")
    @Convert(converter = com.xdata.util.PartialMarkParametersConverter.class)
    @JsonProperty("partialMarkParameters")
    private com.xdata.partialmarking.core.PartialMarkParameters partialMarkParameters;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Submission> submissions = new ArrayList<>();

    @JsonProperty("assignmentId")
    public void setAssignmentId(Integer assignmentId) {
        this.assignmentId = assignmentId;
    }

    @JsonProperty("assignmentId")
    public Integer getAssignmentId() {
        if (this.assignmentId != null) return this.assignmentId;
        return assignment != null ? assignment.getId() : null;
    }
}
