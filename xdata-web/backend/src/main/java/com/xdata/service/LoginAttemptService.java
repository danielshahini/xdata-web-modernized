package com.xdata.service;

import com.xdata.model.XDataUser;
import com.xdata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Brute-force protection: locks an account for a cooldown window after too many
 * consecutive failed logins. Counter writes run in their own transaction so they
 * persist even though the login endpoint itself is read-only.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LoginAttemptService {

    public static final int MAX_ATTEMPTS = 5;
    public static final int LOCK_MINUTES = 15;

    private final UserRepository userRepository;

    /** True if the account is currently within an active lock window. */
    public boolean isLocked(XDataUser user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
    }

    public long minutesRemaining(XDataUser user) {
        if (!isLocked(user)) return 0;
        return Math.max(1, java.time.Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes() + 1);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String loginId) {
        userRepository.findByLoginIdIgnoreCase(loginId).ifPresent(u -> {
            int attempts = u.getFailedLoginAttempts() + 1;
            u.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_ATTEMPTS) {
                u.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
                log.warn("Konto {} nach {} Fehlversuchen für {} Minuten gesperrt.", loginId, attempts, LOCK_MINUTES);
            }
            userRepository.save(u);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String loginId) {
        userRepository.findByLoginIdIgnoreCase(loginId).ifPresent(u -> {
            if (u.getFailedLoginAttempts() != 0 || u.getLockedUntil() != null) {
                u.setFailedLoginAttempts(0);
                u.setLockedUntil(null);
                userRepository.save(u);
            }
        });
    }
}
