package com.xdata.controller.admin;

import com.xdata.model.Course;
import com.xdata.model.XDataUser;
import com.xdata.repository.CourseRepository;
import com.xdata.repository.UserRepository;
import com.xdata.service.AccessControlService;
import com.xdata.service.AuditService;
import com.xdata.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@Transactional
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
public class UserController {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessControlService accessControlService;
    private final AuditService auditService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    @GetMapping
    public ResponseEntity<List<XDataUser>> getAllUsers() {
        if (accessControlService.isAdmin()) {
            return ResponseEntity.ok(userRepository.findAll());
        } else if (accessControlService.isInstructor()) {
            List<String> courseIds = accessControlService.getUserCourseIds();
            if (courseIds != null && !courseIds.isEmpty()) {
                return ResponseEntity.ok(userRepository.findDistinctByCourses_InstructorCourseIdIn(courseIds));
            }
        }
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<XDataUser>> getUnassignedStudents() {
        return ResponseEntity.ok(userRepository.findAllByRoleIgnoreCaseAndCoursesIsEmpty("STUDENT"));
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody XDataUser user) {
        if (user.getId() == null || user.getId().trim().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
        }
        if (user.getLoginId() == null || user.getLoginId().trim().isEmpty()) {
            user.setLoginId(user.getUsername());
        }
        user.setLoginId(user.getLoginId().trim());

        if (userRepository.findByLoginIdIgnoreCase(user.getLoginId()).isPresent()) {
            return ResponseEntity.badRequest().body("User with login ID " + user.getLoginId() + " already exists.");
        }

        Set<String> targetCourseIds = new HashSet<>();
        if (accessControlService.isInstructor()) {
            user.setRole("STUDENT");
            targetCourseIds.addAll(accessControlService.getUserCourseIds());
        } else if (accessControlService.isAdmin()) {
            if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                user.setRole("STUDENT");
            }
            if (user.getCourseIds() != null) {
                targetCourseIds.addAll(user.getCourseIds());
            }
        } else {
            return ResponseEntity.status(403).build();
        }

        for (String cid : targetCourseIds) {
            courseRepository.findByInstructorCourseId(cid).ifPresent(user.getCourses()::add);
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!isValidPassword(user.getPassword())) {
                return ResponseEntity.badRequest().body("Password must be at least 8 characters long.");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            String randomPass = UUID.randomUUID().toString().substring(0, 12);
            user.setPassword(passwordEncoder.encode(randomPass));
            log.info("Random password generated for {}.", user.getLoginId());
        }

        // Force a password change on first login (admin/instructor sets the initial one).
        user.setMustChangePassword(true);
        XDataUser savedUser = userRepository.saveAndFlush(user);
        auditService.log("USER_CREATED", user.getLoginId(), "Role: " + user.getRole());
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/{loginId}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> updateUser(@PathVariable String loginId, @RequestBody Map<String, Object> body) {
        // Read fields explicitly from the JSON body. (Binding to the XDataUser
        // entity dropped the incoming courseIds because of its derived transient
        // property, which silently wiped a user's course assignments on save.)
        final String newUsername = body.get("username") != null ? body.get("username").toString() : null;
        final String newEmail = body.get("email") != null ? body.get("email").toString() : null;
        final String newRole = body.get("role") != null ? body.get("role").toString() : null;
        final String newPassword = body.get("password") != null ? body.get("password").toString() : null;
        final Boolean newEnabled = body.get("enabled") instanceof Boolean ? (Boolean) body.get("enabled") : null;
        final boolean courseIdsProvided = body.containsKey("courseIds") && body.get("courseIds") instanceof List;
        final List<String> newCourseIds = courseIdsProvided
                ? ((List<Object>) body.get("courseIds")).stream().map(String::valueOf).toList()
                : null;

        return userRepository.findByLoginIdIgnoreCase(loginId).map(user -> {
            boolean isSelf = accessControlService.getCurrentUserLoginId().equalsIgnoreCase(loginId);

            if (isSelf && newEnabled != null && !newEnabled && user.isEnabled()) {
                return ResponseEntity.badRequest().body("You cannot deactivate yourself.");
            }

            if (accessControlService.isInstructor()) {
                if (!"STUDENT".equalsIgnoreCase(user.getRole()) && !"INSTRUCTOR".equalsIgnoreCase(user.getRole())) {
                    return ResponseEntity.status(403).body("Instructors can only edit students or instructors.");
                }
                List<String> instructorCourses = accessControlService.getUserCourseIds();
                boolean hasAccess = isSelf || user.getCourses().stream().anyMatch(c -> instructorCourses.contains(c.getInstructorCourseId()));

                if (!hasAccess && !"STUDENT".equalsIgnoreCase(user.getRole())) {
                    return ResponseEntity.status(403).body("No permission for this user.");
                }

                if (newUsername != null) user.setUsername(newUsername);
                if (newEmail != null) user.setEmail(newEmail);
                if (newEnabled != null) user.setEnabled(newEnabled);

                if (newCourseIds != null) {
                    Set<String> targetIds = new HashSet<>(newCourseIds);
                    targetIds.removeIf(cid -> !instructorCourses.contains(cid));
                    user.getCourses().removeIf(c -> instructorCourses.contains(c.getInstructorCourseId()));
                    for (String cid : targetIds) {
                        courseRepository.findByInstructorCourseId(cid).ifPresent(user.getCourses()::add);
                    }
                }
            } else {
                if (newUsername != null) user.setUsername(newUsername);
                if (newEmail != null) user.setEmail(newEmail);
                if (newRole != null) user.setRole(newRole);
                if (newEnabled != null) user.setEnabled(newEnabled);

                if (newCourseIds != null) {
                    user.setCourses(new HashSet<>());
                    for (String cid : newCourseIds) {
                        courseRepository.findByInstructorCourseId(cid).ifPresent(user.getCourses()::add);
                    }
                }
            }

            if (newPassword != null && !newPassword.isEmpty()) {
                if (!isValidPassword(newPassword)) {
                    return ResponseEntity.badRequest().body("Password must be at least 8 characters long.");
                }
                user.setPassword(passwordEncoder.encode(newPassword));
            }

            XDataUser updated = userRepository.saveAndFlush(user);
            auditService.log("USER_UPDATED", loginId, "Updated by " + accessControlService.getCurrentUserLoginId());
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{loginId}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable String loginId) {
        String currentLoginId = accessControlService.getCurrentUserLoginId();
        if (currentLoginId.equalsIgnoreCase(loginId)) {
            return ResponseEntity.badRequest().body("You cannot change your own status.");
        }

        return userRepository.findByLoginIdIgnoreCase(loginId).map(user -> {
            if (accessControlService.isInstructor()) {
                if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
                    return ResponseEntity.status(403).body("Instructors can only change student status.");
                }
                List<String> myCourseIds = accessControlService.getUserCourseIds();
                boolean isMyStudent = user.getCourses().stream().anyMatch(c -> myCourseIds.contains(c.getInstructorCourseId()));
                if (!isMyStudent) {
                    return ResponseEntity.status(403).body("Student not in your courses.");
                }
            }
            user.setEnabled(!user.isEnabled());
            userRepository.saveAndFlush(user);
            auditService.log("USER_STATUS_TOGGLED", loginId, "New status: " + user.isEnabled());
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{loginId}/assign-course")
    public ResponseEntity<?> assignCourse(
            @PathVariable String loginId, 
            @RequestParam(required = false) String courseId, 
            @RequestParam(required = false) List<String> courseIds) {
        
        log.info("Assigning course to user {}: courseId={}, courseIds={}", loginId, courseId, courseIds);
        boolean isAdmin = accessControlService.isAdmin();
        boolean isInstructor = accessControlService.isInstructor();
        log.info("Access check: isAdmin={}, isInstructor={}", isAdmin, isInstructor);
        
        return userRepository.findByLoginIdIgnoreCase(loginId).map(user -> {
            Set<String> targetIds = new HashSet<>();
            if (courseIds != null) targetIds.addAll(courseIds);
            if (courseId != null && !courseId.isEmpty()) targetIds.add(courseId);

            if (accessControlService.isInstructor()) {
                if (!"STUDENT".equalsIgnoreCase(user.getRole()) && !"INSTRUCTOR".equalsIgnoreCase(user.getRole())) {
                    return ResponseEntity.badRequest().body("Only students or instructors allowed.");
                }
                List<String> instructorCourses = accessControlService.getUserCourseIds();
                targetIds.removeIf(id -> !instructorCourses.contains(id));
            } else if (!accessControlService.isAdmin()) {
                return ResponseEntity.status(403).build();
            }

            if (targetIds.isEmpty()) {
                return ResponseEntity.badRequest().body("No valid course ID provided.");
            }
            for (String cid : targetIds) {
                var course = courseRepository.findByInstructorCourseId(cid);
                if (course.isEmpty()) {
                    // Fail loudly instead of silently no-op'ing (e.g. when a numeric DB id
                    // is passed instead of the instructorCourseId).
                    return ResponseEntity.badRequest().body("Course not found: " + cid);
                }
                user.getCourses().add(course.get());
            }
            userRepository.saveAndFlush(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{loginId}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable String loginId, @RequestBody Map<String, String> request) {
        String newPassword = request.get("password");
        if (!isValidPassword(newPassword)) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long.");
        }
        return userRepository.findByLoginIdIgnoreCase(loginId).map(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setMustChangePassword(true); // force the user to set their own on next login
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.saveAndFlush(user);
            auditService.log("PASSWORD_RESET_ADMIN", loginId, "Reset by " + accessControlService.getCurrentUserLoginId());
            return ResponseEntity.ok("Password reset successfully.");
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{loginId}")
    public ResponseEntity<?> deleteUser(@PathVariable String loginId) {
        if (!accessControlService.isAdmin()) return ResponseEntity.status(403).build();
        return userRepository.findByLoginIdIgnoreCase(loginId).map(user -> {
            userRepository.delete(user);
            auditService.log("USER_DELETED", loginId, null);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{loginId}/impersonate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> impersonateUser(@PathVariable String loginId) {
        return userRepository.findByLoginIdIgnoreCase(loginId).map(user -> {
            var userDetails = userDetailsService.loadUserByUsername(user.getLoginId());
            String token = jwtService.generateToken(userDetails);
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            auditService.log("USER_IMPERSONATED", loginId, "Impersonated by " + accessControlService.getCurrentUserLoginId());
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping({"/import", "/import-csv"})
    public ResponseEntity<?> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("File is empty.");
        int imported = 0;
        int skipped = 0;
        List<String> logs = new ArrayList<>();
        // Instructors may only import STUDENTS into their own courses; admins may set any role.
        final boolean isAdmin = accessControlService.isAdmin();
        final List<String> instructorCourses = isAdmin ? null : accessControlService.getUserCourseIds();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                String username = parts[0].trim();
                String loginId = parts[1].trim();
                String password = parts.length > 2 ? parts[2].trim() : null;
                String email = parts.length > 3 ? parts[3].trim() : null;
                String role = parts.length > 4 ? parts[4].trim().toUpperCase() : "STUDENT";
                String courseId = parts.length > 5 ? parts[5].trim() : null;

                // Enforce least privilege for instructors regardless of CSV contents.
                if (!isAdmin) {
                    role = "STUDENT";
                    if (courseId != null && !instructorCourses.contains(courseId)) {
                        courseId = null;
                    }
                }

                if (userRepository.findByLoginIdIgnoreCase(loginId).isPresent()) {
                    logs.add("Skipped: " + loginId + " already exists.");
                    skipped++;
                    continue;
                }

                XDataUser newUser = new XDataUser();
                newUser.setId(UUID.randomUUID().toString());
                newUser.setUsername(username);
                newUser.setLoginId(loginId);
                newUser.setEmail(email);
                newUser.setRole(role);
                newUser.setEnabled(true);
                newUser.setMustChangePassword(true);

                if (password != null && !password.isEmpty()) {
                    newUser.setPassword(passwordEncoder.encode(password));
                } else {
                    String randomPass = UUID.randomUUID().toString().substring(0, 12);
                    newUser.setPassword(passwordEncoder.encode(randomPass));
                }

                if (courseId != null && !courseId.isEmpty()) {
                    courseRepository.findByInstructorCourseId(courseId).ifPresent(newUser.getCourses()::add);
                }

                userRepository.save(newUser);
                logs.add("Imported: " + loginId);
                imported++;
            }
            userRepository.flush();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error during import: " + e.getMessage());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("imported", imported);
        result.put("skipped", skipped);
        result.put("logs", logs);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> getCsvTemplate() {
        String header = "username,loginId,password,email,role,courseId\n";
        String example = "Max Mustermann,student1,password123,max@example.com,STUDENT,CS101\n";
        byte[] content = (header + example).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_import_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(content);
    }
}
