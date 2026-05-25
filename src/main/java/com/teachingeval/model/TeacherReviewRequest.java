package com.teachingeval.model;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "教师最终评价保存请求")
public class TeacherReviewRequest {

    @Schema(description = "教师最终分数", example = "88.00")
    private BigDecimal teacherScore;

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
