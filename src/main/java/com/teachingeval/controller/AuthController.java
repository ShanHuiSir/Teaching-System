package com.teachingeval.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teachingeval.dto.LoginRequest;
import com.teachingeval.dto.LoginResponse;
import com.teachingeval.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "登录认证")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final String cookieName;
    private final boolean cookieSecure;

    public AuthController(AuthService authService,
                          @Value("${app.auth.cookie-name:auth_token}") String cookieName,
                          @Value("${app.auth.cookie-secure:false}") boolean cookieSecure) {
        this.authService = authService;
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
    }

    @Operation(summary = "教师登录", description = "校验演示教师账号，成功后写入 HttpOnly 会话 Cookie。")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request,
                               HttpServletResponse response) {
        AuthService.LoginSession session = authService.login(
                request.getUsername().trim(),
                request.getPassword(),
                request.isRememberMe()
        );

        ResponseCookie cookie = ResponseCookie.from(cookieName, session.token())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(session.ttl())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new LoginResponse(session.username(), session.ttl().toSeconds());
    }

    @Operation(summary = "退出登录", description = "清除当前教师会话。")
    @PostMapping("/logout")
    public void logout(HttpServletRequest request,
                       HttpServletResponse response) {
        authService.logout(resolveToken(request));
        ResponseCookie cookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Operation(summary = "查询当前登录会话", description = "根据 HttpOnly 会话 Cookie 返回当前教师身份。")
    @GetMapping("/me")
    public LoginResponse currentSession(HttpServletRequest request) {
        AuthService.CurrentSession session = authService.currentSession(resolveToken(request))
                .orElseThrow(() -> new IllegalArgumentException("请先登录"));
        return new LoginResponse(session.username(), session.ttl().toSeconds());
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
}
