package com.xdata.controller;

import com.xdata.model.CourseMaterial;
import com.xdata.repository.CourseMaterialRepository;
import com.xdata.security.CourseAccessGuard;
import com.xdata.service.AccessControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final CourseMaterialRepository materialRepository;
    private final AccessControlService accessControlService;
    private final CourseAccessGuard courseAccessGuard;

    /** Materials for the current user's courses (all roles). */
    @GetMapping
    public ResponseEntity<List<CourseMaterial>> myMaterials() {
        List<String> courseIds = accessControlService.getUserCourseIds();
        if (courseIds.isEmpty()) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(materialRepository.findByCourseIdInOrderByCreatedAtDesc(courseIds));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<?> create(@RequestParam String courseId, @RequestBody Map<String, String> body) {
        courseAccessGuard.requireCourseAccess(courseId);
        String title = body.get("title");
        if (title == null || title.isBlank()) return ResponseEntity.badRequest().body("Titel ist erforderlich.");
        String type = body.getOrDefault("type", "LINK");
        CourseMaterial m = CourseMaterial.builder()
                .courseId(courseId).title(title).type(type).content(body.get("content")).build();
        return ResponseEntity.ok(materialRepository.save(m));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        return materialRepository.findById(id).map(m -> {
            courseAccessGuard.requireCourseAccess(m.getCourseId());
            materialRepository.delete(m);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
