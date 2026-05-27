package com.xdata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    // Removed JavaMailSender to fix build issues for now
    public void sendPasswordResetMail(String email, String token) {
        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        log.info("Sending password reset email to {}. URL: {}", email, resetUrl);
        log.info("Note: Real email sending is currently disabled to allow build.");
    }
}
