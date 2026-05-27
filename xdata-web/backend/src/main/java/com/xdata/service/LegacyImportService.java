package com.xdata.service;

import com.xdata.model.Course;
import com.xdata.model.Question;
import com.xdata.model.XDataUser;
import com.xdata.repository.CourseRepository;
import com.xdata.repository.QuestionRepository;
import com.xdata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegacyImportService {

    private final JdbcTemplate legacyJdbcTemplate;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;

    /**
     * Imports users from the legacy xdata_users table.
     * Assumes a secondary DataSource is configured if importing from a different DB,
     * or the table exists in the same DB.
     */
    @Transactional
    public void importLegacyUsers() {
        log.info("Starting legacy user import...");
        try {
            List<Map<String, Object>> legacyUsers = legacyJdbcTemplate.queryForList("SELECT * FROM xdata_users");
            for (Map<String, Object> row : legacyUsers) {
                String loginId = (String) row.get("userid");
                if (userRepository.findByLoginIdIgnoreCase(loginId).isEmpty()) {
                    XDataUser user = XDataUser.builder()
                            .loginId(loginId)
                            .password((String) row.get("password"))
                            .email(loginId + "@example.com") // Placeholder
                            .role("STUDENT") // Default
                            .build();
                    userRepository.save(user);
                }
            }
            log.info("Imported {} users.", legacyUsers.size());
        } catch (Exception e) {
            log.error("Failed to import legacy users: {}", e.getMessage());
        }
    }

    @Transactional
    public void importLegacyQuestions() {
        log.info("Starting legacy question import...");
        try {
            List<Map<String, Object>> legacyQs = legacyJdbcTemplate.queryForList("SELECT * FROM xdata_qinfo");
            for (Map<String, Object> row : legacyQs) {
                String qname = (String) row.get("qname");
                String query = (String) row.get("query");
                Double marks = row.get("marks") != null ? ((Number) row.get("marks")).doubleValue() : 0.0;
                
                // Search for corresponding assignment (simplified matching)
                List<Course> courses = courseRepository.findAll();
                if (courses.isEmpty()) continue;
                
                // Link to first found course for now, or match by some logic
                Course targetCourse = courses.get(0);
                
                if (questionRepository.findByName(qname).isEmpty()) {
                    Question question = Question.builder()
                            .name(qname)
                            .instructorQuery(query)
                            .marks(marks.floatValue())
                            .build();
                    questionRepository.save(question);
                }
            }
            log.info("Imported {} questions.", legacyQs.size());
        } catch (Exception e) {
            log.error("Failed to import legacy questions: {}", e.getMessage());
        }
    }
}
