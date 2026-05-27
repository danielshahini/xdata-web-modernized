package com.xdata.repository;

import com.xdata.model.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {
    List<Announcement> findByCourse_InstructorCourseIdInOrderByCreatedAtDesc(List<String> courseIds);
}
