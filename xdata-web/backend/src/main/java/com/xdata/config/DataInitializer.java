package com.xdata.config;
import lombok.extern.slf4j.Slf4j;
import com.xdata.model.XDataUser;
import com.xdata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Ensure admin1 exists
        initializeUser("admin1", "admin1", "ADMIN", "Administrator");
        
        // Ensure instructor daniel exists for testing
        initializeUser("daniel", "daniel", "INSTRUCTOR", "Daniel Instructor");
    }

    private void initializeUser(String loginId, String password, String role, String name) {
        userRepository.findByLoginIdIgnoreCase(loginId).ifPresentOrElse(
            user -> {
                user.setPassword(passwordEncoder.encode(password));
                user.setRole(role);
                userRepository.save(user);
                log.info("[DEBUG_LOG] Admin-Daten fuer " + loginId + " wurden aktualisiert");
            },
            () -> {
                XDataUser admin = XDataUser.builder()
                        .id("admin-uuid-" + loginId)
                        .username(name)
                        .loginId(loginId)
                        .password(passwordEncoder.encode(password))
                        .role(role)
                        .build();
                userRepository.save(admin);
                log.info("[DEBUG_LOG] Standard-" + role + " " + loginId + " erstellt");
            }
        );

        // Verifikation
        userRepository.findByLoginIdIgnoreCase(loginId).ifPresent(u -> {
            boolean match = passwordEncoder.matches(password, u.getPassword());
            log.info("[DEBUG_LOG] Verifikation in DB fuer " + loginId + ": Match=" + match + ", Role=" + u.getRole());
        });
    }
}
