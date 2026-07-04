package com.teachingeval.service;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teachingeval.dto.ExportRow;
import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.repository.AssignmentClassRepository;
import com.teachingeval.repository.EvaluationRepository;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionRepository;

@Service
public class ExportService {

    private static final int WRITE_BATCH_SIZE = 100;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final EvaluationRepository evaluationRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final AssignmentClassRepository assignmentClassRepository;

    public ExportService(EvaluationRepository evaluationRepository,
                         SubmissionRepository submissionRepository,
                         StudentRepository studentRepository,
                         AssignmentClassRepository assignmentClassRepository) {
        this.evaluationRepository = evaluationRepository;
        this.submissionRepository = submissionRepository;
        this.studentRepository = studentRepository;
        this.assignmentClassRepository = assignmentClassRepository;
    }

    public void exportTo(OutputStream outputStream) {
        exportTo(outputStream, null, null, null);
    }

    public void exportTo(OutputStream outputStream, Long assignmentId, Long classId, String workType) {
        // 1. Resolve authorized classes → all students in those classes
        Set<Long> authorizedClassIds = (assignmentId != null)
                ? assignmentClassRepository.findByAssignmentIdOrderByIdAsc(assignmentId).stream()
                        .map(ac -> ac.getClassId())
                        .collect(Collectors.toSet())
                : Collections.emptySet();

        // 2. Load all students from authorized classes
        List<Student> allStudents = authorizedClassIds.isEmpty()
                ? Collections.emptyList()
                : studentRepository.findByClassIdIn(authorizedClassIds);
        Map<Long, Student> studentMap = allStudents.stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        // 3. Optional class filter
        if (classId != null && !authorizedClassIds.isEmpty()) {
            allStudents = allStudents.stream()
                    .filter(s -> classId.equals(s.getClassId()))
                    .collect(Collectors.toList());
        }

        // 4. Sort students: className → studentNo
        allStudents.sort(Comparator
                .comparing(Student::getClassName, Comparator.nullsLast(String::compareTo))
                .thenComparing(Student::getStudentNo, Comparator.nullsLast(String::compareTo)));

        // Empty guard
        if (allStudents.isEmpty()) {
            EasyExcel.write(outputStream, ExportRow.class)
                    .sheet("成绩汇总")
                    .doWrite(Collections.emptyList());
            return;
        }

        // 5. Load submissions for this assignment, map by studentId
        List<WorkSubmission> submissions = (assignmentId != null)
                ? submissionRepository.findByAssignmentId(assignmentId)
                : submissionRepository.findAll();
        // Apply workType filter if needed
        if (workType != null && !workType.isBlank()) {
            submissions = submissions.stream()
                    .filter(s -> workType.equals(s.getWorkType()))
                    .collect(Collectors.toList());
        }
        Map<Long, WorkSubmission> submissionByStudent = submissions.stream()
                .collect(Collectors.toMap(
                        WorkSubmission::getStudentId,
                        Function.identity(),
                        (a, b) -> a)); // keep first if duplicate

        // 6. Batch-load evaluations for existing submissions
        Set<Long> submissionIds = submissions.stream()
                .map(WorkSubmission::getId)
                .collect(Collectors.toSet());
        Map<Long, EvaluationResult> evalMap = submissionIds.isEmpty()
                ? Collections.emptyMap()
                : evaluationRepository.findBySubmissionIdIn(submissionIds).stream()
                        .collect(Collectors.toMap(EvaluationResult::getSubmissionId, Function.identity(), (a, b) -> a));

        // 7. Stream-write: one row per student (including non-submitters)
        ExcelWriter excelWriter = EasyExcel.write(outputStream, ExportRow.class).build();
        WriteSheet writeSheet = EasyExcel.writerSheet("成绩汇总").build();
        List<ExportRow> batch = new ArrayList<>(WRITE_BATCH_SIZE);

        for (Student student : allStudents) {
            WorkSubmission submission = submissionByStudent.get(student.getId());
            batch.add(buildRow(student, submission, evalMap));
            if (batch.size() >= WRITE_BATCH_SIZE) {
                excelWriter.write(batch, writeSheet);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            excelWriter.write(batch, writeSheet);
        }
        excelWriter.finish();
    }

    private ExportRow buildRow(Student student,
                               WorkSubmission submission,
                               Map<Long, EvaluationResult> evalMap) {
        ExportRow row = new ExportRow();
        row.setStudentNo(student.getStudentNo());
        row.setStudentName(student.getName());
        row.setClassName(student.getClassName());

        if (submission != null) {
            row.setTitle(submission.getTitle());
            row.setWorkType(submission.getWorkType());
            row.setFileName(submission.getFileName());

            EvaluationResult eval = evalMap.get(submission.getId());
            if (eval != null) {
                row.setAiScore(eval.getAiScore());
                row.setAiIssues(eval.getAiIssues());
                row.setAiComment(eval.getAiComment());
                row.setDimensionScores(formatDimensionScores(eval.getDimensionScores()));
                row.setTeacherScore(eval.getTeacherScore());
                row.setTeacherComment(eval.getTeacherComment());
                row.setStatusText(formatStatusText(eval.getStatus()));
            } else {
                row.setStatusText("未评价");
            }
        } else {
            row.setStatusText("未提交");
        }

        return row;
    }

    private static String formatDimensionScores(String raw) {
        if (raw == null || raw.isBlank()) return "";
        try {
            List<Map<String, Object>> items = OBJECT_MAPPER.readValue(raw, new TypeReference<>() {});
            return items.stream()
                    .map(item -> {
                        String name = String.valueOf(item.getOrDefault("name", ""));
                        String score = String.valueOf(item.getOrDefault("score", ""));
                        String comment = String.valueOf(item.getOrDefault("comment", ""));
                        return name + ": " + score + " 分" + (comment.isEmpty() ? "" : " (" + comment + ")");
                    })
                    .collect(Collectors.joining("；"));
        } catch (Exception e) {
            return raw; // fallback: return original on parse error
        }
    }

    private static String formatStatusText(int status) {
        return switch (status) {
            case 0 -> "未评价";
            case 1 -> "已AI评价";
            default -> "教师已确认";
        };
    }
}
