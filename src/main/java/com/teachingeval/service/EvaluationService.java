package com.teachingeval.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.dto.AIEvalRequest;
import com.teachingeval.dto.SaveEvaluationRequest;
import com.teachingeval.dto.TeacherReviewRequest;
import com.teachingeval.repository.EvaluationRepository;
import com.teachingeval.repository.SubmissionRepository;

@Service
public class EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationService.class);

    private final AIService aiService;
    private final EvaluationRepository evaluationRepository;
    private final SubmissionRepository submissionRepository;
    private final RestTemplateBuilder restTemplateBuilder;
    private final Environment environment;
    private final ObjectMapper objectMapper;

    public EvaluationService(AIService aiService,
                             EvaluationRepository evaluationRepository,
                             SubmissionRepository submissionRepository,
                             RestTemplateBuilder restTemplateBuilder,
                             Environment environment,
                             ObjectMapper objectMapper) {
        this.aiService = aiService;
        this.evaluationRepository = evaluationRepository;
        this.submissionRepository = submissionRepository;
        this.restTemplateBuilder = restTemplateBuilder;
        this.environment = environment;
        this.objectMapper = objectMapper;
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
        EvaluationResult saved = evaluationRepository.save(evaluation);

        // After successful save, notify Python ai-service to write the final log entry
        try {
            postEvalLog(submissionId, saved);
        } catch (Exception e) {
            log.warn("评价日志写入失败，不影响批改结果: submissionId={}", submissionId, e);
        }

        return saved;
    }

    private void postEvalLog(Long submissionId, EvaluationResult evaluation) {
        WorkSubmission submission = submissionRepository.findById(submissionId)
                .orElse(null);
        if (submission == null) {
            log.warn("无法记录评价日志：提交记录不存在 submissionId={}", submissionId);
            return;
        }

        String endpointUrl = environment.getProperty(
                "app.ai.eval-log-url", "http://localhost:8000/api/eval-log");

        List<Map<String, Object>> dimensionScores = parseDimensionScores(
                evaluation.getDimensionScores());

        Map<String, Object> body = Map.of(
                "student_name", submission.getStudentName(),
                "ai_score", evaluation.getAiScore() != null
                        ? evaluation.getAiScore().doubleValue() : 0.0,
                "ai_issues", evaluation.getAiIssues() != null
                        ? evaluation.getAiIssues() : "",
                "ai_comment", evaluation.getAiComment() != null
                        ? evaluation.getAiComment() : "",
                "dimension_scores", dimensionScores,
                "teacher_score", evaluation.getTeacherScore() != null
                        ? evaluation.getTeacherScore().doubleValue() : 0.0,
                "teacher_comment", evaluation.getTeacherComment() != null
                        ? evaluation.getTeacherComment() : ""
        );

        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(3000))
                .setReadTimeout(Duration.ofMillis(5000))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(
                endpointUrl,
                new HttpEntity<>(body, headers),
                String.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseDimensionScores(String dimensionScoresJson) {
        if (dimensionScoresJson == null || dimensionScoresJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dimensionScoresJson, List.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse dimensionScores JSON: {}", dimensionScoresJson, e);
            return new ArrayList<>();
        }
    }

    public EvaluationResult rejectEvaluation(Long submissionId) {
        EvaluationResult evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("评价结果不存在"));
        evaluation.setStatus(0);
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
