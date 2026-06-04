package com.xdata.controller;

import com.xdata.model.SchemaInfo;
import com.xdata.model.Course;
import com.xdata.repository.CourseRepository;
import com.xdata.service.DatasetGenerationService;
import com.xdata.service.core.SchemaService;
import com.xdata.service.FileStorageService;
import com.xdata.service.AccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/playground")
public class DatasetPlaygroundController {

    private static final Logger log = LoggerFactory.getLogger(DatasetPlaygroundController.class);

    private final DatasetGenerationService datasetGenerationService;
    private final FileStorageService fileStorageService;
    private final SchemaService schemaService;
    private final AccessControlService accessControlService;
    private final CourseRepository courseRepository;

    @Autowired
    public DatasetPlaygroundController(
            DatasetGenerationService datasetGenerationService,
            FileStorageService fileStorageService,
            SchemaService schemaService,
            AccessControlService accessControlService,
            CourseRepository courseRepository) {
        this.datasetGenerationService = datasetGenerationService;
        this.fileStorageService = fileStorageService;
        this.schemaService = schemaService;
        this.accessControlService = accessControlService;
        this.courseRepository = courseRepository;
    }

    @PostMapping("/generate-killing-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<DatasetPlaygroundResponse> generateKillingData(@RequestBody DatasetPlaygroundRequest request) {
        log.info("Playground: Generating killing dataset for query: {} in schema: {}", request.getQuery(), request.getSchemaId());
        
        try {
            List<String> inserts = datasetGenerationService.generateDatasetFromQuery(
                    request.getQuery(), 
                    request.getMutantQuery(),
                    request.getSchemaId(), 
                    request.getMutationTypes()
            );
            
            DatasetPlaygroundResponse response = new DatasetPlaygroundResponse();
            response.setInserts(inserts);
            
            // Handle specific errors returned as comments in the inserts list
            Optional<String> errorMsg = inserts.stream()
                .filter(s -> s.startsWith("-- ERROR:"))
                .map(s -> s.substring(9).trim())
                .findFirst();
                
            if (errorMsg.isPresent()) {
                response.setSuccess(false);
                response.setMessage(errorMsg.get());
            } else if (inserts.isEmpty()) {
                response.setSuccess(true);
                response.setMessage("No data could be generated. This might happen if the query result is naturally empty for the given constraints.");
            } else {
                response.setSuccess(true);
                response.setMessage("Successfully generated " + (int)inserts.stream().filter(s -> !s.startsWith("--")).count() + " insert statements.");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in playground data generation", e);
            DatasetPlaygroundResponse response = new DatasetPlaygroundResponse();
            response.setSuccess(false);
            response.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/upload-schema")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<DatasetPlaygroundResponse> uploadSchema(@RequestParam("file") MultipartFile file) {
        log.info("Playground: Uploading schema file: {}", file.getOriginalFilename());
        DatasetPlaygroundResponse response = new DatasetPlaygroundResponse();
        try {
            String fileName = fileStorageService.storeFile(file);
            Path filePath = fileStorageService.getFilePath(fileName);
            String content = Files.readString(filePath);
            
            // Create a new SchemaInfo in the database so it can be selected
            String schemaName = file.getOriginalFilename();
            if (schemaName != null && schemaName.contains(".")) {
                schemaName = schemaName.substring(0, schemaName.lastIndexOf("."));
            }

            SchemaInfo.SchemaInfoBuilder schemaBuilder = SchemaInfo.builder()
                    .schemaName(schemaName + "_" + System.currentTimeMillis() / 1000)
                    .content(content);

            // If instructor, try to assign to their first course so they can see it
            if (accessControlService.isInstructor()) {
                String courseId = accessControlService.getUserCourseId();
                if (courseId != null) {
                    Optional<Course> course = courseRepository.findByInstructorCourseId(courseId);
                    course.ifPresent(schemaBuilder::course);
                    log.info("Assigning uploaded schema to course: {}", courseId);
                }
            }

            SchemaInfo savedSchema = schemaService.saveSchema(schemaBuilder.build());
            
            response.setSuccess(true);
            response.setMessage("Schema uploaded and saved to database.");
            response.setExtractedSchema(content);
            response.setSchemaId(savedSchema.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading schema", e);
            response.setSuccess(false);
            response.setMessage("Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    public static class DatasetPlaygroundRequest {
        private String query;
        private String mutantQuery;
        private Integer schemaId;
        private List<String> mutationTypes;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }
        public String getMutantQuery() { return mutantQuery; }
        public void setMutantQuery(String mutantQuery) { this.mutantQuery = mutantQuery; }
        public Integer getSchemaId() { return schemaId; }
        public void setSchemaId(Integer schemaId) { this.schemaId = schemaId; }
        public List<String> getMutationTypes() { return mutationTypes; }
        public void setMutationTypes(List<String> mutationTypes) { this.mutationTypes = mutationTypes; }
    }

    public static class DatasetPlaygroundResponse {
        private List<String> inserts;
        private boolean success;
        private String message;
        private String extractedSchema;
        private Integer schemaId;

        public List<String> getInserts() { return inserts; }
        public void setInserts(List<String> inserts) { this.inserts = inserts; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getExtractedSchema() { return extractedSchema; }
        public void setExtractedSchema(String extractedSchema) { this.extractedSchema = extractedSchema; }
        public Integer getSchemaId() { return schemaId; }
        public void setSchemaId(Integer schemaId) { this.schemaId = schemaId; }
    }
}
