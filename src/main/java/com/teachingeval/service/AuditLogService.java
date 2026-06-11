package com.teachingeval.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.teachingeval.entity.AuditLog;
import com.teachingeval.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditLogService {

    private static final int MAX_LIMIT = 200;

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(HttpServletRequest request, int statusCode, String message) {
        AuditLog log = new AuditLog();
        Object actor = request.getAttribute(AuthService.AUTH_USER_ATTRIBUTE);
        log.setActor(actor == null ? "anonymous" : String.valueOf(actor));
        log.setMethod(request.getMethod());
        log.setPath(request.getRequestURI());
        log.setAction(resolveAction(request.getMethod(), request.getRequestURI()));
        log.setIpAddress(resolveIp(request));
        log.setUserAgent(truncate(request.getHeader("User-Agent"), 512));
        log.setStatusCode(statusCode);
        log.setSuccess(statusCode >= 200 && statusCode < 400);
        log.setMessage(truncate(message, 512));
        auditLogRepository.save(log);
    }

    public List<AuditLog> listLatest(Integer limit) {
        int safeLimit = limit == null ? 100 : Math.max(1, Math.min(limit, MAX_LIMIT));
        return auditLogRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, safeLimit));
    }

    private String resolveAction(String method, String path) {
        String normalized = path == null ? "" : path;
        if (normalized.contains("/auth/login")) return "LOGIN";
        if (normalized.contains("/auth/logout")) return "LOGOUT";
        if (normalized.contains("/evaluate")) return "AI_EVALUATE";
        if (normalized.contains("/teacher-review")) return "TEACHER_REVIEW";
        if (normalized.contains("/export")) return "EXPORT";
        if (normalized.contains("/upload")) return "UPLOAD";
        return switch (method) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "REQUEST";
        };
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
