package com.xdata.service;

import com.xdata.model.Announcement;
import com.xdata.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;

    public List<Announcement> getAnnouncementsForUser(List<String> courseIds) {
        return announcementRepository.findByCourse_InstructorCourseIdInOrderByCreatedAtDesc(courseIds);
    }

    public Announcement createAnnouncement(Announcement announcement) {
        return announcementRepository.save(announcement);
    }

    public void deleteAnnouncement(Integer id) {
        announcementRepository.deleteById(id);
    }
}
