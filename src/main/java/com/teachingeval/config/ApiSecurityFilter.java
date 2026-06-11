package com.teachingeval.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.teachingeval.service.AuthService;
import com.teachingeval.service.AuditLogService;
import com.teachingeval.service.RateLimitService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ApiSecurityFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final RateLimitService rateLimitService;
    private final boolean authEnabled;
    private final String cookieName;
    private final int aiRateLimitPerMinute;

    public ApiSecurityFilter(AuthService authService,
                             AuditLogService auditLogService,
                             RateLimitService rateLimitService,
                             @Value("${app.auth.enabled:true}") boolean authEnabled,
                             @Value("${app.auth.cookie-name:auth_token}") String cookieName,
                             @Value("${app.ai.rate-limit-per-minute:10}") int aiRateLimitPerMinute) {
        this.authService = authService;
        this.auditLogService = auditLogService;
        this.rateLimitService = rateLimitService;
        this.authEnabled = authEnabled;
        this.cookieName = cookieName;
        this.aiRateLimitPerMinute = aiRateLimitPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!authEnabled || isPublicRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<String> user = authService.validate(resolveToken(request));
        if (user.isEmpty()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
            auditLogService.record(request, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
            return;
        }
        request.setAttribute(AuthService.AUTH_USER_ATTRIBUTE, user.get());

        if (isStateChanging(request) && !hasRequestedWithHeader(request)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "缺少防跨站请求头");
            auditLogService.record(request, HttpServletResponse.SC_FORBIDDEN, "缺少防跨站请求头");
            return;
        }

        if (isAiEvaluationRequest(request) && !rateLimitService.tryAcquire(resolveIp(request), aiRateLimitPerMinute)) {
            writeError(response, 429, "AI接口调用过于频繁，请稍后再试");
            auditLogService.record(request, 429, "AI接口调用过于频繁");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        return path.equals("/api/auth/login")
                || path.equals("/api/health")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/v3/ai-docs")
                || path.startsWith("/h2-console")
                || path.equals("/error");
    }

    private boolean isStateChanging(HttpServletRequest request) {
        return switch (request.getMethod()) {
            case "POST", "PUT", "PATCH", "DELETE" -> true;
            default -> false;
        };
    }

    private boolean hasRequestedWithHeader(HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }

    private boolean isAiEvaluationRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().matches("/api/submissions/\\d+/evaluate");
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }
        if (request.getCookies() == null) {
            return null;
        }
        for (var cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
