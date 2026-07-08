package com.teachingeval.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Schema(description = "AI 评分结果持久化请求（流式评分完成后使用）")
public class SaveEvaluationRequest {

    @NotNull(message = "AI 分数不能为空")
    @DecimalMin(value = "0.0", message = "AI 分数不能低于0")
    @DecimalMax(value = "100.0", message = "AI 分数不能超过100")
    @Schema(description = "AI 评分", example = "89.00")
    private BigDecimal aiScore;

    @Schema(description = "AI 发现的问题", example = "1. 问题一\\n2. 问题二")
    private String aiIssues;

    @Schema(description = "AI 综合评语", example = "整体完成度较好")
    private String aiComment;

    @Schema(description = "分维度评分 JSON", example = "[{\"name\":\"代码质量\",\"score\":88,\"comment\":\"良好\"}]")
    private String dimensionScores;

    public SaveEvaluationRequest() {}

    public BigDecimal getAiScore() { return aiScore; }
    public void setAiScore(BigDecimal aiScore) { this.aiScore = aiScore; }

    public String getAiIssues() { return aiIssues; }
    public void setAiIssues(String aiIssues) { this.aiIssues = aiIssues; }

    public String getAiComment() { return aiComment; }
    public void setAiComment(String aiComment) { this.aiComment = aiComment; }

    public String getDimensionScores() { return dimensionScores; }
    public void setDimensionScores(String dimensionScores) { this.dimensionScores = dimensionScores; }
}
