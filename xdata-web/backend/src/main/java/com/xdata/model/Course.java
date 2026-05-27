package com.xdata.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "xdata_course")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Course extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Integer id;

    @Column(name = "course_name")
    @NotBlank
    @JsonProperty("courseName")
    private String name;

    @Column(name = "instructor_course_id")
    @NotBlank
    private String instructorCourseId;

    @Column(name = "course_year")
    private Integer year;

    @Column(name = "course_semester")
    private String semester;

    @Column(name = "course_description")
    private String description;

    @ManyToMany(mappedBy = "courses")
    @JsonIgnore
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private Set<XDataUser> users = new HashSet<>();
}
