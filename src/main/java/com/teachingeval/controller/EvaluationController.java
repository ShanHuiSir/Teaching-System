package com.teachingeval.controller;

import com.teachingeval.model.AIEvalRequestDTO;
import com.teachingeval.model.AIEvaluationResult;
import com.teachingeval.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 评价")
@RestController
@RequestMapping("/api")
public class EvaluationController {

    private final AIService aiService;

    public EvaluationController(AIService aiService) {
        this.aiService = aiService;
    }

    @Operation(summary = "执行 AI 评价", description = "接收学生作品提交信息，调用 AI 评价服务返回评分、问题列表和综合评语。")
    @PostMapping("/evaluate")
    public AIEvaluationResult evaluate(@RequestBody AIEvalRequestDTO request) {
        return aiService.evaluate(request);
    }
}
