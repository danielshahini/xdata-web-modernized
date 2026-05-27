package com.xdata.controller;

import com.xdata.model.DbConnection;
import com.xdata.repository.DbConnectionRepository;
import com.xdata.repository.CourseRepository;
import com.xdata.service.AccessControlService;
import com.xdata.service.AuditService;
import com.xdata.service.DatabaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping({"/api/v1/instructor/connections", "/api/v1/db-connections"})
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
@Slf4j
@Transactional
public class DbConnectionController {

    private final DbConnectionRepository dbConnectionRepository;
    private final CourseRepository courseRepository;
    private final AccessControlService accessControlService;
    private final AuditService auditService;
    private final DatabaseService databaseService;

    @GetMapping
    public ResponseEntity<List<DbConnection>> getAllConnections() {
        if (accessControlService.isAdmin()) {
            log.info("Admin fetching all connections");
            return ResponseEntity.ok(dbConnectionRepository.findAll());
        }
        List<String> courseIds = accessControlService.getUserCourseIds();
        log.info("Fetching connections for user '{}' with courses: {}", 
            accessControlService.getCurrentUserLoginId(), courseIds);
            
        if (!courseIds.isEmpty()) {
            List<DbConnection> connections = dbConnectionRepository.findByCourse_InstructorCourseIdIn(courseIds);
            log.info("Found {} connections for instructor", connections.size());
            return ResponseEntity.ok(connections);
        }
        log.warn("No courses found for user '{}'", accessControlService.getCurrentUserLoginId());
        return ResponseEntity.ok(new ArrayList<>());
    }

    @PostMapping
    public ResponseEntity<?> createConnection(@RequestBody DbConnection connection) {
        String courseId = connection.getCourseId();
        log.info("Request to create connection '{}' for course '{}'", connection.getName(), courseId);

        if (!accessControlService.canAccessCourse(courseId)) {
            log.warn("Permission denied for course '{}'", courseId);
            return ResponseEntity.status(403).body("Keine Berechtigung für diesen Kurs.");
        }

        // Normalize URL
        connection.setUrl(normalizeUrl(connection.getUrl()));

        // Validation
        String validationError = validateConnectionData(connection, true);
        if (validationError != null) {
            return ResponseEntity.status(400).body(validationError);
        }

        // Resolve course
        if (connection.getCourse() == null && courseId != null) {
            connection.setCourse(courseRepository.findByInstructorCourseId(courseId).orElse(null));
        }

        if (connection.getCourse() == null) {
            return ResponseEntity.status(400).body("Zugehöriger Kurs wurde nicht gefunden.");
        }

        // Test connection
        String testError = testConnectionInternal(connection);
        if (testError != null) {
            log.warn("Connection test failed for connection '{}': {}", connection.getName(), testError);
            return ResponseEntity.status(400).body("Verbindungstest vor Speichern fehlgeschlagen: " + testError);
        }

        try {
            DbConnection saved = dbConnectionRepository.save(connection);
            auditService.log("DB_CONNECTION_CREATED", saved.getName(), "Course: " + courseId);
            log.info("Successfully created connection '{}' with ID {}", saved.getName(), saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error saving connection: {}", e.getMessage());
            return ResponseEntity.status(500).body("Interner Fehler beim Speichern: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateConnection(@PathVariable Integer id, @RequestBody DbConnection connection) {
        return dbConnectionRepository.findById(id).map(existing -> {
            String existingCourseId = existing.getCourse() != null ? existing.getCourse().getInstructorCourseId() : null;
            String newCourseId = connection.getCourseId();
            
            log.info("Request to update connection ID {} ('{}'). Course: {} -> {}", id, existing.getName(), existingCourseId, newCourseId);

            if (!accessControlService.canAccessCourse(existingCourseId) || 
                (newCourseId != null && !accessControlService.canAccessCourse(newCourseId))) {
                log.warn("Permission denied for updating connection ID {}", id);
                return ResponseEntity.status(403).body("Keine Berechtigung.");
            }

            // Update fields
            if (connection.getName() != null) existing.setName(connection.getName());
            if (connection.getUrl() != null) existing.setUrl(normalizeUrl(connection.getUrl()));
            if (connection.getUser() != null) existing.setUser(connection.getUser());
            if (connection.getPassword() != null && !connection.getPassword().isEmpty()) {
                existing.setPassword(connection.getPassword());
            }
            
            if (newCourseId != null && (existing.getCourse() == null || !newCourseId.equals(existingCourseId))) {
                courseRepository.findByInstructorCourseId(newCourseId).ifPresent(existing::setCourse);
            }

            // Validation
            String validationError = validateConnectionData(existing, false);
            if (validationError != null) {
                return ResponseEntity.status(400).body(validationError);
            }

            // Test connection
            String testError = testConnectionInternal(existing);
            if (testError != null) {
                log.warn("Connection test failed for updating connection ID {}: {}", id, testError);
                return ResponseEntity.status(400).body("Verbindungstest vor Update fehlgeschlagen: " + testError);
            }

            DbConnection saved = dbConnectionRepository.save(existing);
            auditService.log("DB_CONNECTION_UPDATED", saved.getName(), "ID: " + id);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConnection(@PathVariable Integer id) {
        return dbConnectionRepository.findById(id).map(existing -> {
            String courseId = existing.getCourse() != null ? existing.getCourse().getInstructorCourseId() : null;
            if (!accessControlService.canAccessCourse(courseId)) {
                return ResponseEntity.status(403).<Void>build();
            }
            dbConnectionRepository.deleteById(id);
            auditService.log("DB_CONNECTION_DELETED", "ID: " + id, "");
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/test")
    public ResponseEntity<String> testAllConnections() {
        if (!accessControlService.isAdmin()) {
            return ResponseEntity.status(403).body("Nur Administratoren können alle Verbindungen testen.");
        }
        
        List<DbConnection> connections = dbConnectionRepository.findAll();
        StringBuilder results = new StringBuilder();
        int success = 0;
        
        for (DbConnection conn : connections) {
            String error = testConnectionInternal(conn);
            if (error == null) {
                results.append("SUCCESS: ").append(conn.getName()).append("\n");
                success++;
            } else {
                results.append("FAILED: ").append(conn.getName()).append(" - ").append(error).append("\n");
            }
        }
        
        String summary = String.format("Test abgeschlossen. %d/%d Verbindungen ERFOLGREICH.\n\n%s", 
                                       success, connections.size(), results.toString());
        return ResponseEntity.ok(summary);
    }

    @RequestMapping(value = "/{id}/test", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> testConnection(@PathVariable Integer id) {
        return dbConnectionRepository.findById(id).map(conn -> {
            String courseId = conn.getCourse() != null ? conn.getCourse().getInstructorCourseId() : null;
            if (!accessControlService.canAccessCourse(courseId)) {
                return ResponseEntity.status(403).body("Keine Berechtigung.");
            }

            String testError = testConnectionInternal(conn);
            if (testError == null) {
                return ResponseEntity.ok("Connection successful");
            } else {
                return ResponseEntity.status(400).body("Verbindungstest fehlgeschlagen: " + testError);
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    private String validateConnectionData(DbConnection conn, boolean isNew) {
        if (conn.getName() == null || conn.getName().trim().isEmpty()) {
            return "Name der Verbindung darf nicht leer sein.";
        }
        if (conn.getUrl() == null || conn.getUrl().trim().isEmpty()) {
            return "Datenbank-URL darf nicht leer sein.";
        }
        if (conn.getUser() == null || conn.getUser().trim().isEmpty()) {
            return "Datenbank-Benutzer darf nicht leer sein.";
        }
        if (isNew && (conn.getPassword() == null || conn.getPassword().trim().isEmpty())) {
            return "Passwort darf nicht leer sein.";
        }
        return null;
    }

    private String normalizeUrl(String url) {
        if (url == null) return null;
        url = url.trim();
        if (url.startsWith("jdbc:postgresql:") && !url.startsWith("jdbc:postgresql://")) {
            String remainder = url.substring("jdbc:postgresql:".length());
            if (remainder.contains(":") || (remainder.contains("/") && remainder.indexOf("/") > 0)) {
                return "jdbc:postgresql://" + remainder;
            }
        }
        if (url.startsWith("jdbc:mysql:") && !url.startsWith("jdbc:mysql://")) {
            String remainder = url.substring("jdbc:mysql:".length());
            if (remainder.contains(":") || (remainder.contains("/") && remainder.indexOf("/") > 0)) {
                return "jdbc:mysql://" + remainder;
            }
        }
        return url;
    }

    private String testConnectionInternal(DbConnection conn) {
        try (Connection testConn = databaseService.getConnection(conn)) {
            if (testConn.isValid(5)) {
                return null; // Success
            } else {
                return "Die Verbindung konnte nicht validiert werden.";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
