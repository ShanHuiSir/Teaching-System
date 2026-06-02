package com.teachingeval.service.impl;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Duration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachingeval.dto.AIEvalRequest;
import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.WorkSubmission;

@Service
public class RealAIService {

    private final RestTemplateBuilder restTemplateBuilder;
    private final Environment environment;
    private final ObjectMapper objectMapper;

    public RealAIService(RestTemplateBuilder restTemplateBuilder,
                         Environment environment,
                         ObjectMapper objectMapper) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.environment = environment;
        this.objectMapper = objectMapper;
    }

    public EvaluationResult evaluate(WorkSubmission submission, AIEvalRequest request) {
        if (submission.getFilePath() == null || submission.getFilePath().isBlank()) {
            throw new IllegalArgumentException("当前作品没有真实文件，无法执行真实 AI 评价");
        }

        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(getLongProperty("app.ai.real.connect-timeout-ms", 3000L)))
                .setReadTimeout(Duration.ofMillis(getLongProperty("app.ai.real.read-timeout-ms", 30000L)))
                .build();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("studentName", resolveStudentName(submission, request));
        body.add("file", new FileSystemResource(Path.of(submission.getFilePath())));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    getEndpointUrl(),
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("真实 AI 服务返回异常状态：" + response.getStatusCode().value());
            }
            return parseResult(response.getBody());
        } catch (RestClientException exception) {
            throw new IllegalStateException("真实 AI 服务调用失败：" + exception.getMessage());
        }
    }

    private EvaluationResult parseResult(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody == null ? "{}" : rawBody);
            EvaluationResult result = new EvaluationResult();
            result.setAiScore(new BigDecimal(root.path("aiScore").asText("0")));
            result.setAiIssues(root.path("aiIssues").asText(""));
            result.setAiComment(root.path("aiComment").asText(""));
            result.setStatus(root.path("status").asInt(EvaluationResult.STATUS_AI_REVIEWED));
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("真实 AI 服务响应解析失败：" + exception.getMessage());
        }
    }

    private String resolveStudentName(WorkSubmission submission, AIEvalRequest request) {
        if (request != null && request.getStudentName() != null && !request.getStudentName().isBlank()) {
            return request.getStudentName();
        }
        return submission.getStudentName();
    }

    private String getEndpointUrl() {
        return environment.getProperty("app.ai.real.endpoint-url", "http://localhost:8000/api/evaluate/real");
    }

    private long getLongProperty(String propertyName, long defaultValue) {
        return environment.getProperty(propertyName, Long.class, defaultValue);
    }
}
