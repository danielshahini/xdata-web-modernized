package com.xdata.service;

import com.xdata.model.XDataUser;
import com.xdata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final UserRepository userRepository;

    public String getCurrentUserLoginId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    public Optional<XDataUser> getCurrentUser() {
        return userRepository.findByLoginIdIgnoreCase(getCurrentUserLoginId());
    }

    public boolean isAdmin() {
        return getCurrentUser().map(user -> {
            String role = user.getRole();
            return role != null && "ADMIN".equalsIgnoreCase(role.trim());
        }).orElse(false);
    }

    public boolean isInstructor() {
        return getCurrentUser().map(user -> {
            String role = user.getRole();
            return role != null && "INSTRUCTOR".equalsIgnoreCase(role.trim());
        }).orElse(false);
    }

    public boolean canAccessCourse(String courseId) {
        return getCurrentUser().map(user -> {
            String role = user.getRole();
            if (role == null) return false;
            role = role.trim().toUpperCase();
            if ("ADMIN".equals(role)) {
                return true;
            }
            if (courseId == null) return false;
            if (user.getCourses() != null) {
                return user.getCourses().stream()
                        .anyMatch(c -> courseId.equals(c.getInstructorCourseId()));
            }
            return false;
        }).orElse(false);
    }
    
    public boolean isStudent() {
        return getCurrentUser().map(user -> {
            String role = user.getRole();
            return role != null && "STUDENT".equalsIgnoreCase(role.trim());
        }).orElse(false);
    }

    /** Teaching assistant / tutor: may grade and give feedback, but not manage users/courses. */
    public boolean isTutor() {
        return getCurrentUser().map(user -> {
            String role = user.getRole();
            return role != null && "TUTOR".equalsIgnoreCase(role.trim());
        }).orElse(false);
    }

    public List<String> getUserCourseIds() {
        return getCurrentUser().map(user -> {
            if (user.getCourses() != null) {
                return user.getCourses().stream()
                        .map(com.xdata.model.Course::getInstructorCourseId)
                        .collect(Collectors.toList());
            }
            return (List<String>) new ArrayList<String>();
        }).orElse(new ArrayList<>());
    }

    @Deprecated
    public String getUserCourseId() {
        List<String> ids = getUserCourseIds();
        return ids.isEmpty() ? null : ids.get(0);
    }
}
