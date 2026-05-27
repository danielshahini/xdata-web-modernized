package com.xdata.controller.admin;

import com.xdata.model.Announcement;
import com.xdata.model.Course;
import com.xdata.repository.CourseRepository;
import com.xdata.repository.UserRepository;
import com.xdata.service.AccessControlService;
import com.xdata.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementController {
    private final AnnouncementService announcementService;
    private final AccessControlService accessControlService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Announcement>> getAnnouncements() {
        List<String> courseIds = accessControlService.getUserCourseIds();
        return ResponseEntity.ok(announcementService.getAnnouncementsForUser(courseIds));
    }

    @PostMapping
    public ResponseEntity<?> createAnnouncement(@RequestBody Announcement announcement, @RequestParam String courseId) {
        if (!accessControlService.isInstructor() && !accessControlService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        
        Course course = courseRepository.findByInstructorCourseId(courseId)
                .orElseThrow(() -> new RuntimeException("Kurs nicht gefunden"));
        
        announcement.setCourse(course);
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setCreatedBy(userRepository.findByLoginIdIgnoreCase(accessControlService.getCurrentUserLoginId()).get());
        
        return ResponseEntity.ok(announcementService.createAnnouncement(announcement));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Integer id) {
        if (!accessControlService.isInstructor() && !accessControlService.isAdmin()) {
            return ResponseEntity.status(403).build();
        }
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.ok().build();
    }
}
