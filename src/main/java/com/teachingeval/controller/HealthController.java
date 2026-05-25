package com.teachingeval.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "系统状态")
@RestController
public class HealthController {
    @Operation(summary = "健康检查", description = "用于确认后端服务是否正常运行。")
    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "message", "教学评价系统运行中"
        );
    }
}

