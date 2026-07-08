package com.teachingeval.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "教师最终评价保存请求")
public class TeacherReviewRequest {

    @NotNull(message = "教师分数不能为空")
    @DecimalMin(value = "0.0", message = "教师分数不能低于0")
    @DecimalMax(value = "100.0", message = "教师分数不能超过100")
    @Schema(description = "教师最终分数", example = "88.00")
    private BigDecimal teacherScore;

    @NotBlank(message = "教师评语不能为空")
    @Schema(description = "教师最终评语", example = "整体完成较好，建议继续完善代码注释。")
    private String teacherComment;

    public BigDecimal getTeacherScore() {
        return teacherScore;
    }

    public void setTeacherScore(BigDecimal teacherScore) {
        this.teacherScore = teacherScore;
    }

    public String getTeacherComment() {
        return teacherComment;
    }

    public void setTeacherComment(String teacherComment) {
        this.teacherComment = teacherComment;
    }
}
