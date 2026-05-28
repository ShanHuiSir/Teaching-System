package com.teachingeval.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.teachingeval.entity.EvaluationResult;
import org.springframework.stereotype.Service;

import com.teachingeval.dto.StatisticsSummaryResponse;
import com.teachingeval.repository.EvaluationRepository;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionRepository;

@Service
public class StatisticsService {

    private final StudentRepository studentRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;

    public StatisticsService(StudentRepository studentRepository,
                             SubmissionRepository submissionRepository,
                             EvaluationRepository evaluationRepository) {
        this.studentRepository = studentRepository;
        this.submissionRepository = submissionRepository;
        this.evaluationRepository = evaluationRepository;
    }

    public StatisticsSummaryResponse getSummary() {
        List<EvaluationResult> evaluations = evaluationRepository.findAll();
        List<BigDecimal> confirmedScores = evaluations.stream()
                .filter(EvaluationResult::isTeacherConfirmed)
                .map(EvaluationResult::getTeacherScore)
                .filter(score -> score != null)
                .toList();

        BigDecimal average = BigDecimal.ZERO;
        if (!confirmedScores.isEmpty()) {
            BigDecimal total = confirmedScores.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            average = total.divide(BigDecimal.valueOf(confirmedScores.size()), 2, RoundingMode.HALF_UP);
        }

        return new StatisticsSummaryResponse(
                studentRepository.count(),
                submissionRepository.count(),
                evaluationRepository.countByStatusGreaterThanEqual(EvaluationResult.STATUS_AI_REVIEWED),
                evaluationRepository.countByStatusGreaterThanEqual(EvaluationResult.STATUS_TEACHER_CONFIRMED),
                average
        );
    }
}
