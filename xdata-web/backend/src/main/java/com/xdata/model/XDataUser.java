package com.xdata.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "xdata_users")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XDataUser extends BaseAuditEntity {

    @Id
    @Column(name = "internal_user_id")
    @NotBlank
    private String id;

    @Column(name = "user_name")
    @NotBlank
    @Size(min = 2, max = 100)
    private String username;

    @Column(name = "email")
    @Email
    private String email;

    @Column(name = "password")
    @NotBlank
    private String password;

    @Column(name = "role")
    @NotBlank
    private String role;

    @Column(name = "login_user_id")
    @NotBlank
    private String loginId;

    @Column(name = "enabled")
    @Builder.Default
    private boolean enabled = true;
    
    @Column(name = "xp")
    private Integer xp = 0;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "xdata_user_courses",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "users", "updatedAt", "createdAt"})
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private Set<Course> courses = new HashSet<>();

    @Transient
    private Set<String> tempCourseIds;

    @Transient
    @JsonProperty("courseIds")
    public Set<String> getCourseIds() {
        if (courses != null && !courses.isEmpty()) {
            return courses.stream()
                    .map(Course::getInstructorCourseId)
                    .collect(Collectors.toSet());
        }
        return tempCourseIds != null ? tempCourseIds : new HashSet<>();
    }

    @Transient
    @JsonProperty("courseIds")
    public void setCourseIds(Set<String> courseIds) {
        this.tempCourseIds = courseIds;
    }

    @Transient
    @JsonProperty("courseId")
    @Deprecated
    public String getCourseId() {
        return (courses != null && !courses.isEmpty()) ? courses.iterator().next().getInstructorCourseId() : null;
    }
}
