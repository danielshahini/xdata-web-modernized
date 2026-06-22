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
    // Accept a password on input (create/update) but never serialize the stored
    // BCrypt hash back to clients.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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

    // --- Login security / password policy ---
    @Column(name = "failed_login_attempts")
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private int failedLoginAttempts = 0;

    @Column(name = "locked_until")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.time.LocalDateTime lockedUntil;

    @Column(name = "must_change_password")
    @Builder.Default
    private boolean mustChangePassword = false;

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

    // Serialize the derived ids out (READ_ONLY) but deserialize incoming ids via
    // the setter (WRITE_ONLY). Without splitting the access, Jackson treats this
    // as a setterless collection and writes incoming values into the throwaway
    // collection returned by the getter, so courseIds from the client were lost.
    @Transient
    @JsonProperty(value = "courseIds", access = JsonProperty.Access.READ_ONLY)
    public Set<String> getCourseIds() {
        if (courses != null && !courses.isEmpty()) {
            return courses.stream()
                    .map(Course::getInstructorCourseId)
                    .collect(Collectors.toSet());
        }
        return tempCourseIds != null ? tempCourseIds : new HashSet<>();
    }

    @Transient
    @JsonProperty(value = "courseIds", access = JsonProperty.Access.WRITE_ONLY)
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
