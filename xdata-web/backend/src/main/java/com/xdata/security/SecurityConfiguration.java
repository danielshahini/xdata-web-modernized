package com.xdata.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:80,http://localhost}")
    private String allowedOrigins;

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // SockJS/STOMP handshake (/ws-grading, /ws-grading/info, /ws-grading/**) cannot
                        // carry the JWT in an Authorization header, so it must be permitted at the HTTP
                        // layer; otherwise the handshake falls through to anyRequest().authenticated() -> 403.
                        .requestMatchers("/ws-grading/**").permitAll()
                        .requestMatchers("/api/v1/admin/users/**").hasAnyRole("ADMIN", "INSTRUCTOR")
                        .requestMatchers("/api/v1/admin/courses/**").hasAnyRole("ADMIN", "INSTRUCTOR")
                        // Audit logs contain system-wide security events (failed logins, password
                        // resets, impersonations) -> ADMIN only, not instructors.
                        .requestMatchers("/api/v1/admin/audit-logs/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/assignments/**").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT", "TUTOR")
                        .requestMatchers("/api/v1/schemas/**").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT", "TUTOR")
                        .requestMatchers("/api/v1/student/**").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT")
                        .requestMatchers("/api/v1/evaluation/**").hasAnyRole("ADMIN", "INSTRUCTOR", "STUDENT", "TUTOR")
                        .requestMatchers("/api/v1/instructor/**").hasAnyRole("ADMIN", "INSTRUCTOR")
                        // API docs require authentication (no anonymous exposure on the school server).
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
