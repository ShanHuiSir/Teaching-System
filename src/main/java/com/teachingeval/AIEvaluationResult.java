package com.teachingeval;

import java.math.BigDecimal;

public class AIEvaluationResult {

    private Long id;
    private Long submissionId;
    private BigDecimal aiScore;
    private String aiIssues;
    private String aiComment;
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
        return "EvaluationResult{" +
                "id=" + id +
                ", submissionId=" + submissionId +
                ", aiScore=" + aiScore +
                ", aiIssues='" + aiIssues + '\'' +
                ", aiComment='" + aiComment + '\'' +
                ", status=" + status +
                '}';
    }
}
