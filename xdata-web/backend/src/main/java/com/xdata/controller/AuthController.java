package com.xdata.controller;

import lombok.extern.slf4j.Slf4j;
import com.xdata.dto.LoginRequest;
import com.xdata.dto.LoginResponse;
import com.xdata.model.XDataUser;
import com.xdata.repository.UserRepository;
import com.xdata.model.PasswordResetToken;
import com.xdata.repository.PasswordResetTokenRepository;
import com.xdata.service.MailService;
import com.xdata.service.AuditService;
import com.xdata.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository tokenRepository;
    private final MailService mailService;
    private final AuditService auditService;
    private final com.xdata.service.LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String loginIdInput = request.getLoginId() != null ? request.getLoginId().trim() : "";
        String passwordInput = request.getPassword();
        
        log.info("Login attempt for: {}", loginIdInput);
        
        XDataUser user = userRepository.findByLoginIdIgnoreCase(loginIdInput)
                .orElse(null);

        if (user == null) {
            log.error("LOGIN_FAILED: User {} not found.", loginIdInput);
            auditService.log("LOGIN_FAILED", loginIdInput, "User not found");
            return ResponseEntity.status(401).build();
        }

        if (user.isEnabled() == false) {
            log.warn("LOGIN_FAILED: User {} is disabled.", loginIdInput);
            auditService.log("LOGIN_FAILED", loginIdInput, "Account disabled");
            return ResponseEntity.status(401).body(null);
        }

        // Brute-force lockout: refuse while the cooldown window is active.
        if (loginAttemptService.isLocked(user)) {
            long mins = loginAttemptService.minutesRemaining(user);
            log.warn("LOGIN_BLOCKED: Account {} locked ({} min remaining).", user.getLoginId(), mins);
            auditService.log("LOGIN_BLOCKED", user.getLoginId(), "Account locked, " + mins + " min remaining");
            return ResponseEntity.status(423).header("X-Lock-Minutes", String.valueOf(mins)).build();
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getLoginId(), passwordInput)
            );
        } catch (Exception e) {
            log.error("LOGIN_FAILED: Password match failed for {}.", user.getLoginId());
            auditService.log("LOGIN_FAILED", user.getLoginId(), "Invalid password");
            loginAttemptService.recordFailure(user.getLoginId());
            return ResponseEntity.status(401).build();
        }

        loginAttemptService.recordSuccess(user.getLoginId());

        String role = user.getRole();
        if (role == null) role = "STUDENT";
        
        UserDetails userDetails = new User(
                user.getLoginId(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()))
        );

        String jwtToken = jwtService.generateToken(userDetails);
        
        return ResponseEntity.ok(LoginResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .loginId(user.getLoginId())
                .role(role.trim().toUpperCase())
                .courseId(user.getCourseId())
                .courseIds(user.getCourseIds())
                .mustChangePassword(user.isMustChangePassword())
                .build());
    }

    /** Self-service password change for the logged-in user (works locally without e-mail). */
    @PostMapping("/change-password")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        String currentLoginId = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body("The new password must be at least 8 characters long.");
        }

        XDataUser user = userRepository.findByLoginIdIgnoreCase(currentLoginId).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(400).body("The current password is incorrect.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("The new password must differ from the old one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
        auditService.log("PASSWORD_CHANGED_SELF", user.getLoginId(), "Self-service change");
        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        return userRepository.findByEmail(email)
            .map(user -> {
                String token = UUID.randomUUID().toString();
                PasswordResetToken resetToken = PasswordResetToken.builder()
                        .token(token)
                        .user(user)
                        .expiryDate(LocalDateTime.now().plusHours(24))
                        .build();
                tokenRepository.save(resetToken);
                mailService.sendPasswordResetMail(user.getEmail(), token);
                auditService.log("FORGOT_PASSWORD_REQUEST", user.getLoginId(), "Email: " + email);
                return ResponseEntity.ok("Reset email sent if user exists");
            })
            .orElse(ResponseEntity.ok("Reset email sent if user exists"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");
        if (newPassword == null || newPassword.length() < 8) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long.");
        }
        return tokenRepository.findByToken(token)
            .map(resetToken -> {
                if (resetToken.isExpired()) {
                    return ResponseEntity.badRequest().body("Token expired");
                }
                XDataUser user = resetToken.getUser();
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                tokenRepository.delete(resetToken);
                auditService.log("PASSWORD_RESET_SELF", user.getLoginId(), "Via token");
                return ResponseEntity.ok("Password reset successful");
            })
            .orElse(ResponseEntity.badRequest().body("Invalid token"));
    }
}
