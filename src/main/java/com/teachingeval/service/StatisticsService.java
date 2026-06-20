package com.teachingeval.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
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
        return getSummary(null, null, null);
    }

    public StatisticsSummaryResponse getSummary(Long assignmentId, Long classId) {
        return getSummary(assignmentId, classId, null);
    }

    public StatisticsSummaryResponse getSummary(Long assignmentId, Long classId, List<Long> teacherClassIds) {
        // 1. 学生：按教师管辖班级范围过滤（数据库层）
        List<Student> scopedStudents = (teacherClassIds != null)
                ? studentRepository.findByClassIdIn(teacherClassIds)
                : studentRepository.findAll();

        if (classId != null) {
            scopedStudents = scopedStudents.stream()
                    .filter(s -> classId.equals(s.getClassId()))
                    .toList();
        }

        // 2. 提交：按作业和/或学生范围过滤（数据库层）
        List<WorkSubmission> submissions;
        if (assignmentId != null) {
            submissions = submissionRepository.findByAssignmentId(assignmentId);
        } else {
            submissions = submissionRepository.findAll();
        }

        // 进一步按学生范围过滤
        if (teacherClassIds != null || classId != null) {
            Set<Long> studentIds = scopedStudents.stream()
                    .map(Student::getId)
                    .collect(Collectors.toSet());
            submissions = submissions.stream()
                    .filter(s -> studentIds.contains(s.getStudentId()))
                    .toList();
        }

        // 3. 评价：按提交范围过滤（数据库层）
        Set<Long> submissionIds = submissions.stream()
                .map(WorkSubmission::getId)
                .collect(Collectors.toSet());
        List<EvaluationResult> evaluations = submissionIds.isEmpty()
                ? Collections.emptyList()
                : evaluationRepository.findBySubmissionIdIn(submissionIds);

        // 4. 汇总统计
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

        long aiReviewedCount = evaluations.stream()
                .filter(e -> e.getStatus() >= EvaluationResult.STATUS_AI_REVIEWED)
                .count();
        long confirmedCount = evaluations.stream()
                .filter(e -> e.getStatus() >= EvaluationResult.STATUS_TEACHER_CONFIRMED)
                .count();

        return new StatisticsSummaryResponse(
                scopedStudents.size(),
                submissions.size(),
                aiReviewedCount,
                confirmedCount,
                average,
                assignmentId,
                classId
        );
    }
}
