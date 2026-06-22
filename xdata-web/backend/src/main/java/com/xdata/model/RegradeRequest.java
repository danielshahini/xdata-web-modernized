package com.xdata.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** A student's objection / regrade request for one graded submission. */
@Entity
@Table(name = "xdata_regrade_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegradeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "submission_id")
    private Integer submissionId;

    @Column(name = "student_login_id")
    private String studentLoginId;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "status")
    @Builder.Default
    private String status = "OPEN"; // OPEN | RESOLVED

    @Column(name = "instructor_response", columnDefinition = "TEXT")
    private String instructorResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "OPEN";
    }
}
