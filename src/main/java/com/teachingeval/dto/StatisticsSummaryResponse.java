package com.teachingeval.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "成绩统计摘要")
public record StatisticsSummaryResponse(
        @Schema(description = "学生总数", example = "30")
        long studentCount,
        @Schema(description = "作品提交数", example = "28")
        long submissionCount,
        @Schema(description = "AI 已评价数", example = "20")
        long aiEvaluatedCount,
        @Schema(description = "教师已确认数", example = "18")
        long teacherConfirmedCount,
        @Schema(description = "教师最终平均分", example = "86.50")
        BigDecimal averageTeacherScore
) {
}
