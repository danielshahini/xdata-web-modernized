package com.xdata.service;

import com.xdata.model.LmsCredential;
import com.xdata.model.Submission;
import com.xdata.repository.LmsCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LmsIntegrationService {

    private final LmsCredentialRepository lmsCredentialRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public void syncGradeToLms(Submission submission) {
        log.info("Syncing grade to LMS for student: {}, assignment: {}", submission.getStudentId(), submission.getAssignment().getId());
        
        List<LmsCredential> credentials = lmsCredentialRepository.findAll();
        if (credentials.isEmpty()) {
            log.warn("No LMS credentials found. Skipping sync.");
            return;
        }

        LmsCredential config = credentials.get(0);
        
        try {
            // 1. Get Access Token
            String accessToken = getAccessToken(config);
            if (accessToken == null) return;

            // 2. Push Grade (LTI 1.3 Advantage - Assignments and Graded Services)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/vnd.ims.lis.v1.score+json"));
            headers.setBearerAuth(accessToken);

            Map<String, Object> score = Map.of(
                "userId", submission.getStudentId(),
                "scoreGiven", submission.getMarks(),
                "scoreMaximum", 100.0,
                "comment", "Graded by XData",
                "timestamp", java.time.Instant.now().toString(),
                "activityProgress", "Completed",
                "gradingProgress", "FullyGraded"
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(score, headers);
            String url = config.getGradeServiceUrl(); // E.g. Moodle's lineitem score URL
            
            if (url != null && !url.isEmpty()) {
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
                log.info("LMS Sync Response: {}", response.getStatusCode());
            } else {
                log.warn("No Grade Service URL configured for LMS {}", config.getLmsName());
            }

        } catch (Exception e) {
            log.error("Failed to sync grade to LMS: {}", e.getMessage());
        }
    }

    private String getAccessToken(LmsCredential config) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");
            map.add("client_id", config.getClientId());
            map.add("client_secret", config.getClientSecret());
            map.add("scope", "https://purl.imsglobal.org/spec/lti-ags/scope/score");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(config.getTokenUrl(), request, Map.class);
            
            return (String) response.getBody().get("access_token");
        } catch (Exception e) {
            log.error("Failed to get LMS access token: {}", e.getMessage());
            return null;
        }
    }
}
