package com.teachingeval.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public static final String AUTH_USER_ATTRIBUTE = "authUser";

    private final String username;
    private final String password;
    private final Duration sessionTtl;
    private final Duration rememberTtl;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthService(@Value("${app.auth.username:teacher}") String username,
                       @Value("${app.auth.password:123456}") String password,
                       @Value("${app.auth.session-ttl-hours:12}") long sessionTtlHours,
                       @Value("${app.auth.remember-ttl-days:7}") long rememberTtlDays) {
        this.username = username;
        this.password = password;
        this.sessionTtl = Duration.ofHours(sessionTtlHours);
        this.rememberTtl = Duration.ofDays(rememberTtlDays);
    }

    public LoginSession login(String username, String password, boolean rememberMe) {
        if (!this.username.equals(username) || !this.password.equals(password)) {
            throw new IllegalArgumentException("账户名或密钥错误");
        }

        Duration ttl = rememberMe ? rememberTtl : sessionTtl;
        String token = newToken();
        Instant expiresAt = Instant.now().plus(ttl);
        sessions.put(token, new Session(username, expiresAt));
        return new LoginSession(token, username, ttl);
    }

    public Optional<String> validate(String token) {
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
        return Optional.of(session.username());
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private record Session(String username, Instant expiresAt) {
    }

    public record LoginSession(String token, String username, Duration ttl) {
    }
}
