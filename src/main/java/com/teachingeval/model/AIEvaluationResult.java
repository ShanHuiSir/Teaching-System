package com.teachingeval.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * [1. 类概述]
 * AI 评价结果实体类，承载 AI 对一份学生作品提交的自动评价数据，
 * 包括分数、发现的问题、综合评语以及评价状态。
 * 使用 JPA 映射至数据库 ai_evaluation_result 表，由 Spring Data JPA 管理生命周期。
 * <p>
 * [2. 成员变量详解]
 * - private Long id:                 主键，数据库自增，新建对象时为 null。
 * - private Long submissionId:       关联的作品提交 ID，用于追溯评价对应的提交记录。
 * - private BigDecimal aiScore:      AI 建议分数，取值范围 0.00 ~ 100.00，精度两位小数。
 * - private String aiIssues:         AI 发现的问题，多条问题以换行符分隔。
 * - private String aiComment:        AI 综合评语。
 * - private int status:              评价状态：0 表示未评价，1 表示 AI 已评价。
 * <p>
 * [3. 方法调用指南]
 * - 无参构造函数用于 JPA 实例化，随后通过 setter 填充各字段。
 * - isAiEvaluated() 在 status >= 1 时返回 true，用于判断 AI 是否已完成评价。
 * - toString() 输出关键字段的快照，便于调试日志。
 * <p>
 * [4. 继承与实现关系]
 * - 直接继承 java.lang.Object，无实现的接口。
 * - 隶属于系统的实体层 (Entity)，由 Spring Data JPA Repository 进行持久化操作。
 */
@Entity
@Table(name = "ai_evaluation_result")
public class AIEvaluationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "ai_score", precision = 5, scale = 2)
    private BigDecimal aiScore;

    @Column(name = "ai_issues", columnDefinition = "TEXT")
    private String aiIssues;

    @Column(name = "ai_comment", columnDefinition = "TEXT")
    private String aiComment;

    @Column(name = "status")
    private int status;

    public AIEvaluationResult() {}

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

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public boolean isAiEvaluated() { return status >= 1; }

    @Override
    public String toString() {
        return "AIEvaluationResult{" +
                "id=" + id +
                ", submissionId=" + submissionId +
                ", aiScore=" + aiScore +
                ", aiIssues='" + aiIssues + '\'' +
                ", aiComment='" + aiComment + '\'' +
                ", status=" + status +
                '}';
    }
}
