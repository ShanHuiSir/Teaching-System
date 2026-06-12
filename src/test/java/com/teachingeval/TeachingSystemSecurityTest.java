package com.teachingeval;

import com.teachingeval.config.DataInitializer;
import com.teachingeval.repository.SubmissionRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.upload.root=target/security-test-uploads",
        "app.auth.enabled=true",
        "app.auth.username=teacher",
        "app.auth.password=123456",
        "app.ai.real.enabled=false",
        "app.ai.rate-limit-per-minute=1",
        "app.ai.eval-log.enabled=false"
})
@AutoConfigureMockMvc
class TeachingSystemSecurityTest {

    private static final String REQUESTED_WITH = "X-Requested-With";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataInitializer dataInitializer;

    @Autowired
    private SubmissionRepository submissionRepository;

    @BeforeEach
    void resetData() {
        FileSystemUtils.deleteRecursively(Paths.get("target/security-test-uploads").toFile());
        dataInitializer.resetDemoData();
    }

    @Test
    void protectedApiRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    @Test
    void currentSessionUsesServerSideAuthCookie() throws Exception {
        Cookie authCookie = login();

        mockMvc.perform(get("/api/auth/me")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("teacher"))
                .andExpect(jsonPath("$.expiresInSeconds").isNumber());
    }

    @Test
    void stateChangingApiRequiresRequestedWithHeader() throws Exception {
        Cookie authCookie = login();

        mockMvc.perform(post("/api/students")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentNo": "2026999",
                                  "name": "安全测试",
                                  "className": "软件 1 班"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("缺少防跨站请求头"));
    }

    @Test
    void logoutClearsSessionCookieAndInvalidatesToken() throws Exception {
        Cookie authCookie = login();

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(authCookie)
                        .header(REQUESTED_WITH, "XMLHttpRequest"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("auth_token", 0))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        mockMvc.perform(get("/api/auth/me")
                        .cookie(authCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void submissionFileDownloadRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/submissions/1/file"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    @Test
    void submissionFileDownloadSucceedsForAuthenticatedUser() throws Exception {
        Cookie authCookie = login();

        Long submissionId = submissionRepository.findAll().get(0).getId();
        mockMvc.perform(get("/api/submissions/{id}/file", submissionId)
                        .cookie(authCookie))
                .andExpect(status().isOk());
    }

    @Test
    void aiEvaluationRequestsAreRateLimitedPerMinute() throws Exception {
        Cookie authCookie = login();
        Long submissionId = submissionRepository.findAll().get(0).getId();

        mockMvc.perform(post("/api/submissions/{submissionId}/evaluate", submissionId)
                        .cookie(authCookie)
                        .header(REQUESTED_WITH, "XMLHttpRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/submissions/{submissionId}/evaluate", submissionId)
                        .cookie(authCookie)
                        .header(REQUESTED_WITH, "XMLHttpRequest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("AI接口调用过于频繁，请稍后再试"));
    }

    private Cookie login() throws Exception {
        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "teacher",
                                  "password": "123456",
                                  "rememberMe": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("teacher"))
                .andExpect(cookie().exists("auth_token"))
                .andReturn();

        Cookie authCookie = result.getResponse().getCookie("auth_token");
        assertThat(authCookie).isNotNull();
        return authCookie;
    }
}
