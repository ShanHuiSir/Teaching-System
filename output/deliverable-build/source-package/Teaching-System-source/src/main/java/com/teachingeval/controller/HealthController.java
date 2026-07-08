package com.teachingeval.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Tag(name = "系统状态")
@RestController
public class HealthController {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Operation(summary = "健康检查", description = "用于确认后端服务是否正常运行。")
    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "message", "教学评价系统运行中"
        );
    }

    @Operation(summary = "SSE心跳", description = "SSE长连接，每10s推送心跳。浏览器断线后5s自动重连。")
    @GetMapping(value = "/api/heartbeat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter heartbeat(HttpServletResponse response) {
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Cache-Control", "no-cache");
        SseEmitter emitter = new SseEmitter(0L);
        String clientId = "client_" + System.currentTimeMillis();
        emitters.put(clientId, emitter);

        try {
            emitter.send(SseEmitter.event()
                    .reconnectTime(5000L)
                    .name("heartbeat")
                    .data("{\"status\":\"connected\"}"));
        } catch (IOException e) {
            emitters.remove(clientId);
        }

        emitter.onCompletion(() -> removeEmitter(clientId));
        emitter.onTimeout(() -> removeEmitter(clientId));
        emitter.onError(e -> removeEmitter(clientId));

        return emitter;
    }

    @Scheduled(fixedRate = 10_000)
    public void sendHeartbeat() {
        emitters.forEach((clientId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data("alive"));
            } catch (IOException | IllegalStateException e) {
                removeEmitter(clientId);
            }
        });
    }

    private void removeEmitter(String clientId) {
        emitters.remove(clientId);
    }
}
