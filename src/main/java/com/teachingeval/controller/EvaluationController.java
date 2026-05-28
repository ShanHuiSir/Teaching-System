package com.teachingeval.controller;

import com.teachingeval.dto.AIEvalRequest;
import com.teachingeval.dto.TeacherReviewRequest;
import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "AI 评价")
@RestController
@RequestMapping("/api")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @Operation(summary = "执行 AI 评价并保存", description = "接收学生作品提交信息，调用 AI 评价服务并保存评分、问题列表和综合评语。")
    @PostMapping("/submissions/{submissionId}/evaluate")
    public EvaluationResult evaluate(@PathVariable Long submissionId,
                                     @RequestBody AIEvalRequest request) {
        return evaluationService.evaluate(submissionId, request);
    }

    @Operation(summary = "查询评价结果", description = "根据作品提交 ID 查询 AI 和教师评价结果。")
    @GetMapping("/submissions/{submissionId}/evaluation")
    public ResponseEntity<EvaluationResult> getEvaluation(@PathVariable Long submissionId) {
        return evaluationService.findBySubmissionId(submissionId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "查询全部评价结果", description = "返回系统中已有的评价结果，用于状态列表批量匹配作业状态。")
    @GetMapping("/evaluations")
    public List<EvaluationResult> listEvaluations() {
        return evaluationService.listEvaluations();
    }

    @Operation(summary = "保存教师最终评价", description = "保存教师最终评分（0-100）和评语，并将评价状态改为教师已确认。")
    @PostMapping("/submissions/{submissionId}/teacher-review")
    public EvaluationResult saveTeacherReview(@PathVariable Long submissionId,
                                              @Valid @RequestBody TeacherReviewRequest request) {
        return evaluationService.saveTeacherReview(submissionId, request);
    }
}
