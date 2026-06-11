package com.teachingeval.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.WorkSubmission;
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
        return getSummary(null, null);
    }

    public StatisticsSummaryResponse getSummary(Long assignmentId, Long classId) {
        List<Student> students = studentRepository.findAll();
        List<WorkSubmission> submissions = submissionRepository.findAll();
        if (classId != null) {
            submissions = submissions.stream()
                    .filter(submission -> {
                        Student student = students.stream()
                                .filter(item -> item.getId().equals(submission.getStudentId()))
                                .findFirst()
                                .orElse(null);
                        return student != null && classId.equals(student.getClassId());
                    })
                    .toList();
        }
        if (assignmentId != null) {
            submissions = submissions.stream()
                    .filter(submission -> assignmentId.equals(submission.getAssignmentId()))
                    .toList();
        }

        Set<Long> submissionIds = submissions.stream()
                .map(WorkSubmission::getId)
                .collect(Collectors.toSet());

        List<EvaluationResult> evaluations = evaluationRepository.findAll().stream()
                .filter(evaluation -> submissionIds.contains(evaluation.getSubmissionId()))
                .toList();
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
                classId == null ? students.size() : students.stream().filter(student -> classId.equals(student.getClassId())).count(),
                submissions.size(),
                evaluations.stream().filter(evaluation -> evaluation.getStatus() >= EvaluationResult.STATUS_AI_REVIEWED).count(),
                evaluations.stream().filter(evaluation -> evaluation.getStatus() >= EvaluationResult.STATUS_TEACHER_CONFIRMED).count(),
                average,
                assignmentId,
                classId
        );
    }
}
