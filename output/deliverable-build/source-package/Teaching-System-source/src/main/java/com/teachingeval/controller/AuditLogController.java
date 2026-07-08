package com.teachingeval.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teachingeval.entity.AuditLog;
import com.teachingeval.service.AuditLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "审计日志")
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Operation(summary = "查询最近审计日志", description = "返回最近的关键接口操作日志，用于追溯谁在什么时候做了什么。")
    @GetMapping
    public List<AuditLog> listLatest(@RequestParam(required = false) Integer limit) {
        return auditLogService.listLatest(limit);
    }
}
