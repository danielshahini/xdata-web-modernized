package com.xdata.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** An individual, per-student deadline override for one assignment. */
@Entity
@Table(name = "xdata_deadline_extensions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "assignment_id")
    private Integer assignmentId;

    @Column(name = "student_login_id")
    private String studentLoginId;

    @Column(name = "extended_deadline")
    private LocalDateTime extendedDeadline;
}
