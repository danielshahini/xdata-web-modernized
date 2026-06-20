package com.xdata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "xdata_db_connections")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DbConnection extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "connection_id")
    private Integer id;

    @Column(name = "connection_name")
    private String name;

    @Column(name = "db_url")
    private String url;

    @Column(name = "db_user")
    private String user;

    @Column(name = "db_password")
    @Convert(converter = com.xdata.util.EncryptionConverter.class)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Transient
    @JsonProperty("courseId")
    private String courseId;

    public String getCourseId() {
        if (courseId != null && !courseId.isEmpty()) return courseId;
        return course != null ? course.getInstructorCourseId() : null;
    }

    @JsonProperty("dbType")
    public String getDbType() {
        if (url == null) return "POSTGRESQL";
        if (url.contains("postgresql")) return "POSTGRESQL";
        if (url.contains("mysql")) return "MYSQL";
        if (url.contains("sqlite")) return "SQLITE";
        if (url.contains("oracle")) return "ORACLE";
        return "POSTGRESQL";
    }

    @JsonProperty("databaseName")
    public String getDatabaseName() {
        if (url == null) return "";
        int lastSlash = url.lastIndexOf("/");
        if (lastSlash != -1) {
            return url.substring(lastSlash + 1);
        }
        return url;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;
}
