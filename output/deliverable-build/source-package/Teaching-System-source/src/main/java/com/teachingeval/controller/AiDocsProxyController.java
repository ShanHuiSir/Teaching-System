package com.teachingeval.controller;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "AI服务文档")
class AiDocsProxyController {

    private static final Logger log = LoggerFactory.getLogger(AiDocsProxyController.class);
    private static final String AI_OPENAPI_URL = "http://localhost:8000/openapi.json";
    private final RestTemplate restTemplate = new RestTemplate();

    @Operation(summary = "AI 服务 OpenAPI 规范", description = "代理 Python AI 服务的 OpenAPI 文档，供 Swagger UI 下拉菜单合并展示。内部调用：" + AI_OPENAPI_URL)
    @GetMapping(value = "/v3/ai-docs", produces = "application/json")
    public ResponseEntity<?> aiDocs() {
        try {
            String spec = restTemplate.getForObject(AI_OPENAPI_URL, String.class);
            log.info("Fetched AI service OpenAPI spec from {}", AI_OPENAPI_URL);
            return ResponseEntity.ok(spec);
        } catch (RestClientException e) {
            log.warn("AI service unavailable at {}: {}", AI_OPENAPI_URL, e.getMessage());
            Map<String, Object> fallback = Map.of(
                    "openapi", "3.0.1",
                    "info", Map.of(
                            "title", "AI 服务 (当前不可用)",
                            "description", "Python AI 服务未启动。请运行 start.bat 或 cd ai-service && python -m docxconv.server",
                            "version", "—"
                    ),
                    "paths", Collections.emptyMap()
            );
            return ResponseEntity.status(HttpStatus.OK).body(fallback);
        }
    }
}
