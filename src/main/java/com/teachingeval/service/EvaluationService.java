package com.teachingeval.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.teachingeval.model.AIEvalRequestDTO;
import com.teachingeval.model.AIEvaluationResult;
import com.teachingeval.model.TeacherReviewRequest;
import com.teachingeval.repository.EvaluationRepository;
import com.teachingeval.repository.SubmissionRepository;

@Service
public class EvaluationService {

    private final AIService aiService;
    private final EvaluationRepository evaluationRepository;
    private final SubmissionRepository submissionRepository;

    public EvaluationService(AIService aiService,
                             EvaluationRepository evaluationRepository,
                             SubmissionRepository submissionRepository) {
        this.aiService = aiService;
        this.evaluationRepository = evaluationRepository;
        this.submissionRepository = submissionRepository;
    }

    public AIEvaluationResult evaluateAndSave(AIEvalRequestDTO request) {
        if (request.getSubmissionId() == null) {
            throw new IllegalArgumentException("作品提交 ID 不能为空");
        }
        submissionRepository.findById(request.getSubmissionId())
                .orElseThrow(() -> new IllegalArgumentException("作品提交不存在"));

        AIEvaluationResult result = aiService.evaluate(request);
        AIEvaluationResult saved = evaluationRepository.findBySubmissionId(request.getSubmissionId())
                .orElseGet(AIEvaluationResult::new);

        saved.setSubmissionId(request.getSubmissionId());
        saved.setAiScore(result.getAiScore());
        saved.setAiIssues(result.getAiIssues());
        saved.setAiComment(result.getAiComment());
        saved.setStatus(1);
        return evaluationRepository.save(saved);
    }

    public AIEvaluationResult saveTeacherReview(Long submissionId, TeacherReviewRequest request) {
        AIEvaluationResult evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("请先完成 AI 评价"));

        validateScore(request.getTeacherScore());
        if (isBlank(request.getTeacherComment())) {
            throw new IllegalArgumentException("教师评语不能为空");
        }

        evaluation.setTeacherScore(request.getTeacherScore());
        evaluation.setTeacherComment(request.getTeacherComment());
        evaluation.setStatus(2);
        return evaluationRepository.save(evaluation);
    }

    public AIEvaluationResult getBySubmissionId(Long submissionId) {
        return evaluationRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("评价结果不存在"));
    }

    private void validateScore(BigDecimal score) {
        if (score == null) {
            throw new IllegalArgumentException("教师分数不能为空");
        }
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("教师分数必须在 0 到 100 之间");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
