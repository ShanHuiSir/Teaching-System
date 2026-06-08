package com.teachingeval.service;

import java.util.List;

import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.WorkSubmission;
import org.springframework.stereotype.Service;

import com.teachingeval.dto.AIEvalRequest;
import com.teachingeval.dto.SaveEvaluationRequest;
import com.teachingeval.dto.TeacherReviewRequest;
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

    public EvaluationResult evaluate(Long submissionId, AIEvalRequest request) {
        WorkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("作品提交不存在"));

        EvaluationResult result = aiService.evaluate(submission, request);
        EvaluationResult saved = evaluationRepository.findBySubmissionId(submissionId)
                .orElseGet(EvaluationResult::new);

        saved.setSubmissionId(submissionId);
        saved.setAiScore(result.getAiScore());
        saved.setAiIssues(result.getAiIssues());
        saved.setAiComment(result.getAiComment());
        saved.setDimensionScores(result.getDimensionScores());
        if (saved.getStatus() < 2) {
            saved.setStatus(1);
        }
        return evaluationRepository.save(saved);
    }

    public EvaluationResult saveAiResult(Long submissionId, SaveEvaluationRequest request) {
        EvaluationResult saved = evaluationRepository.findBySubmissionId(submissionId)
                .orElseGet(EvaluationResult::new);
        saved.setSubmissionId(submissionId);
        saved.setAiScore(request.getAiScore());
        saved.setAiIssues(request.getAiIssues() != null ? request.getAiIssues() : "");
        saved.setAiComment(request.getAiComment() != null ? request.getAiComment() : "");
        saved.setDimensionScores(request.getDimensionScores() != null ? request.getDimensionScores() : "[]");
        if (saved.getStatus() < 1) {
            saved.setStatus(EvaluationResult.STATUS_AI_REVIEWED);
        }
        return evaluationRepository.save(saved);
    }

    public EvaluationResult saveTeacherReview(Long submissionId, TeacherReviewRequest request) {
        EvaluationResult evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("请先完成 AI 评价"));

        evaluation.setTeacherScore(request.getTeacherScore());
        evaluation.setTeacherComment(request.getTeacherComment());
        evaluation.setStatus(2);
        return evaluationRepository.save(evaluation);
    }

    public EvaluationResult getBySubmissionId(Long submissionId) {
        return evaluationRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("评价结果不存在"));
    }

    public List<EvaluationResult> listEvaluations() {
        return evaluationRepository.findAll();
    }
}
