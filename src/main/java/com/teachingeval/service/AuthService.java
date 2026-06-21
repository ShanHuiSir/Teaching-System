package com.teachingeval.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;

import com.teachingeval.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class AuthService {

    public static final String AUTH_USER_ATTRIBUTE = "authUser";

    private final Map<String, String> credentials = new ConcurrentHashMap<>();
    private final Duration sessionTtl;
    private final Duration rememberTtl;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final TeacherRepository teacherRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(@Value("${app.auth.username:teacher}") String username,
                       @Value("${app.auth.password:123456}") String password,
                       @Value("${app.auth.accounts:}") String additionalAccounts,
                       @Value("${app.auth.session-ttl-hours:12}") long sessionTtlHours,
                       @Value("${app.auth.remember-ttl-days:7}") long rememberTtlDays,
                       TeacherRepository teacherRepository) {
        this.credentials.put(username, ensureHashed(password));
        for (String account : additionalAccounts.split(",")) {
            String trimmed = account.trim();
            if (trimmed.isEmpty()) continue;
            String[] parts = trimmed.split(":", 2);
            if (parts.length == 2) {
                credentials.put(parts[0], ensureHashed(parts[1]));
            }
        }
        this.sessionTtl = Duration.ofHours(sessionTtlHours);
        this.rememberTtl = Duration.ofDays(rememberTtlDays);
        this.teacherRepository = teacherRepository;
    }

    @PostConstruct
    void loadTeacherCredentials() {
        teacherRepository.findAll().forEach(t -> credentials.put(t.getUsername(), t.getPassword()));
    }

    public LoginSession login(String username, String password, boolean rememberMe) {
        String expectedHash = credentials.get(username);
        if (expectedHash == null || !passwordEncoder.matches(password, expectedHash)) {
            throw new IllegalArgumentException("账户名或密钥错误");
        }

        Duration ttl = rememberMe ? rememberTtl : sessionTtl;
        String token = newToken();
        Instant expiresAt = Instant.now().plus(ttl);
        sessions.put(token, new Session(username, expiresAt));
        return new LoginSession(token, username, ttl);
    }

    public Optional<String> validate(String token) {
        return currentSession(token).map(CurrentSession::username);
    }

    public Optional<CurrentSession> currentSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        Session session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }
        Duration remainingTtl = Duration.between(Instant.now(), session.expiresAt());
        return Optional.of(new CurrentSession(session.username(), remainingTtl));
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    /** 每 15 分钟清理过期的 session，防止内存泄漏 */
    @Scheduled(fixedRate = 15 * 60 * 1000)
    void purgeExpiredSessions() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Session>> it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().expiresAt().isBefore(now)) {
                it.remove();
            }
        }
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** 如果传入值是明文则自动 BCrypt 哈希；已是 BCrypt 则原样返回 */
    private String ensureHashed(String raw) {
        if (raw.startsWith("$2a$") || raw.startsWith("$2b$") || raw.startsWith("$2y$")) {
            return raw;
        }
        return passwordEncoder.encode(raw);
    }

    private record Session(String username, Instant expiresAt) {
    }

    public record LoginSession(String token, String username, Duration ttl) {
    }

    public record CurrentSession(String username, Duration ttl) {
    }
}
