package com.teachingeval.entity;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "evaluation")
@Schema(description = "AI 评价结果实体，承载 AI 对一份学生作品的自动评价数据")
public class EvaluationResult {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_AI_REVIEWED = 1;
    public static final int STATUS_TEACHER_CONFIRMED = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键，数据库自增，新建对象时为 null", example = "1")
    private Long id;

    @Column(name = "submission_id")
    @Schema(description = "关联的作品提交 ID，用于追溯评价对应的提交记录", example = "1001")
    private Long submissionId;

    @Column(name = "ai_score", precision = 5, scale = 2)
    @Schema(description = "AI 建议分数，取值范围 0.00 ~ 100.00", example = "82.50")
    private BigDecimal aiScore;

    @Column(name = "ai_issues", columnDefinition = "TEXT")
    @Schema(description = "AI 发现的问题，多条问题以换行符分隔",
            example = "1. 结构不够清晰，建议优化段落层次\n2. 缺少核心论点支撑材料")
    private String aiIssues;

    @Column(name = "ai_comment", columnDefinition = "TEXT")
    @Schema(description = "AI 综合评语", example = "整体完成度较好，但在结构组织上还有提升空间")
    private String aiComment;

    @Column(name = "dimension_scores", columnDefinition = "TEXT")
    @Schema(description = "AI 分维度评分详情，JSON 数组", example = "[{\"name\":\"代码质量\",\"score\":88,\"comment\":\"命名规范\"}]")
    private String dimensionScores;

    @Column(name = "teacher_score", precision = 5, scale = 2)
    @Schema(description = "教师最终评分，取值范围 0.00 ~ 100.00", example = "88.00")
    private BigDecimal teacherScore;

    @Column(name = "teacher_comment", columnDefinition = "TEXT")
    @Schema(description = "教师最终评语", example = "整体完成较好，建议继续完善代码注释。")
    private String teacherComment;

    @Column(name = "status")
    @Schema(description = "评价状态：0 表示未评价，1 表示 AI 已评价，2 表示教师已确认", example = "1")
    private int status;

    public EvaluationResult() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public BigDecimal getAiScore() { return aiScore; }
    public void setAiScore(BigDecimal aiScore) { this.aiScore = aiScore; }

    public String getAiIssues() { return aiIssues; }
    public void setAiIssues(String aiIssues) { this.aiIssues = aiIssues; }

    public String getAiComment() { return aiComment; }
    public void setAiComment(String aiComment) { this.aiComment = aiComment; }

    public String getDimensionScores() { return dimensionScores; }
    public void setDimensionScores(String dimensionScores) { this.dimensionScores = dimensionScores; }

    public BigDecimal getTeacherScore() { return teacherScore; }
    public void setTeacherScore(BigDecimal teacherScore) { this.teacherScore = teacherScore; }

    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public boolean isAiEvaluated() { return status >= STATUS_AI_REVIEWED; }

    public boolean isTeacherConfirmed() { return status >= STATUS_TEACHER_CONFIRMED; }

    @Override
    public String toString() {
        return "EvaluationResult{" +
                "id=" + id +
                ", submissionId=" + submissionId +
                ", aiScore=" + aiScore +
                ", aiIssues='" + aiIssues + '\'' +
                ", aiComment='" + aiComment + '\'' +
                ", dimensionScores='" + dimensionScores + '\'' +
                ", teacherScore=" + teacherScore +
                ", teacherComment='" + teacherComment + '\'' +
                ", status=" + status +
                '}';
    }
}
