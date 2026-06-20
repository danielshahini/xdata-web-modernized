package com.xdata.controller;

import com.xdata.dto.SchemaMetadataDTO;
import com.xdata.model.SchemaInfo;
import com.xdata.service.core.SchemaService;
import com.xdata.service.AccessControlService;
import com.xdata.security.CourseAccessGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schemas")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchemaController {

    private final SchemaService schemaService;
    private final AccessControlService accessControlService;
    private final CourseAccessGuard courseAccessGuard;
    private final com.xdata.repository.CourseRepository courseRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<SchemaInfo>> getAllSchemas() {
        if (accessControlService.isAdmin()) {
            return ResponseEntity.ok(schemaService.getAllSchemas());
        }
        List<String> courseIds = accessControlService.getUserCourseIds();
        if (!courseIds.isEmpty()) {
            return ResponseEntity.ok(schemaService.getSchemasByCourses(courseIds));
        }
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<SchemaInfo>> getSchemasByCourse(@PathVariable String courseId) {
        courseAccessGuard.requireCourseAccess(courseId);
        return ResponseEntity.ok(schemaService.getSchemasByCourse(courseId));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<SchemaInfo> uploadSchema(
            @RequestParam("courseId") String courseId,
            @RequestParam("schemaName") String schemaName,
            @RequestParam("file") MultipartFile file) throws IOException {

        courseAccessGuard.requireCourseAccess(courseId);

        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        
        // Load course to set the reference
        com.xdata.model.Course course = courseRepository.findByInstructorCourseId(courseId).orElse(null);

        SchemaInfo schemaInfo = SchemaInfo.builder()
                .course(course)
                .schemaName(schemaName)
                .content(content)
                .build();

        return ResponseEntity.ok(schemaService.saveSchema(schemaInfo));
    }

    @GetMapping("/{id}/metadata")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<SchemaMetadataDTO> getSchemaMetadata(@PathVariable Integer id) {
        // Berechtigungsprüfung: Gehört das Schema zum Kurs des Nutzers?
        return schemaService.getSchemaById(id)
                .map(schema -> {
                    String courseId = schema.getCourse() != null ? schema.getCourse().getInstructorCourseId() : null;
                    courseAccessGuard.requireCourseAccess(courseId);
                    return ResponseEntity.ok(schemaService.getSchemaMetadata(id));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<Void> deleteSchema(@PathVariable Integer id) {
        return schemaService.getSchemaById(id)
                .map(schema -> {
                    String courseId = schema.getCourse() != null ? schema.getCourse().getInstructorCourseId() : null;
                    courseAccessGuard.requireCourseAccess(courseId);
                    schemaService.deleteSchema(id);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/datasets")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public ResponseEntity<List<String>> getDefaultDataSets() {
        // Dummy-Implementierung, da die Datensätze im Dateisystem oder einer speziellen Tabelle liegen könnten
        return ResponseEntity.ok(java.util.Arrays.asList("Dataset1", "Dataset2", "Dataset3"));
    }

    @PostMapping("/sample-data/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<?> uploadSampleData(
            @RequestParam("schemaName") String schemaName,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        // Logik zum Hochladen von Beispieldaten in das Schema
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        // Hier würde normalerweise ein Service aufgerufen, der die Daten in die DB schreibt
        return ResponseEntity.ok("Sample data uploaded to " + schemaName);
    }
}
