package com.teachingeval.service;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.teachingeval.dto.ExportRow;
import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.repository.EvaluationRepository;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionRepository;

@Service
public class ExportService {

    private static final int WRITE_BATCH_SIZE = 100;

    private final EvaluationRepository evaluationRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;

    public ExportService(EvaluationRepository evaluationRepository,
                         SubmissionRepository submissionRepository,
                         StudentRepository studentRepository) {
        this.evaluationRepository = evaluationRepository;
        this.submissionRepository = submissionRepository;
        this.studentRepository = studentRepository;
    }

    public void exportTo(OutputStream outputStream) {
        exportTo(outputStream, null, null, null);
    }

    public void exportTo(OutputStream outputStream, Long assignmentId, Long classId, String workType) {
        // 1. 按作业 ID 查询提交（数据库层过滤）
        List<WorkSubmission> submissions = (assignmentId != null)
                ? submissionRepository.findByAssignmentId(assignmentId)
                : submissionRepository.findAll();

        if (submissions.isEmpty()) {
            EasyExcel.write(outputStream, ExportRow.class)
                    .sheet("成绩汇总")
                    .doWrite(Collections.emptyList());
            return;
        }

        // 2. 提取涉及的 studentId，批量查询学生
        Collection<Long> studentIds = submissions.stream()
                .map(WorkSubmission::getStudentId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, Student> studentMap = studentIds.isEmpty()
                ? Collections.emptyMap()
                : studentRepository.findAllById(studentIds).stream()
                        .collect(Collectors.toMap(Student::getId, Function.identity()));

        // 3. 可选：按班级 / 作品类型过滤（内存在已 scope 的小数据集上执行）
        if (classId != null) {
            submissions = submissions.stream()
                    .filter(submission -> {
                        Student student = studentMap.get(submission.getStudentId());
                        return student != null && classId.equals(student.getClassId());
                    })
                    .toList();
        }
        if (workType != null && !workType.isBlank()) {
            submissions = submissions.stream()
                    .filter(s -> workType.equals(s.getWorkType()))
                    .toList();
        }

        // 4. 提取涉及的 submissionId，批量查询评价
        Collection<Long> submissionIds = submissions.stream()
                .map(WorkSubmission::getId)
                .collect(Collectors.toSet());
        Map<Long, EvaluationResult> evalMap = submissionIds.isEmpty()
                ? Collections.emptyMap()
                : evaluationRepository.findBySubmissionIdIn(submissionIds).stream()
                        .collect(Collectors.toMap(EvaluationResult::getSubmissionId, Function.identity(), (a, b) -> a));

        // 5. 流式写出：batch 组装 → ExcelWriter 分批写入，避免全量 ExportRow 驻留内存
        ExcelWriter excelWriter = EasyExcel.write(outputStream, ExportRow.class).build();
        WriteSheet writeSheet = EasyExcel.writerSheet("成绩汇总").build();
        List<ExportRow> batch = new ArrayList<>(WRITE_BATCH_SIZE);

        for (WorkSubmission submission : submissions) {
            batch.add(buildRow(submission, studentMap, evalMap));
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

    private ExportRow buildRow(WorkSubmission submission,
                               Map<Long, Student> studentMap,
                               Map<Long, EvaluationResult> evalMap) {
        ExportRow row = new ExportRow();
        row.setTitle(submission.getTitle());
        row.setWorkType(submission.getWorkType());
        row.setFileName(submission.getFileName());
        row.setStudentName(submission.getStudentName());

        Student student = studentMap.get(submission.getStudentId());
        if (student != null) {
            row.setStudentNo(student.getStudentNo());
            row.setClassName(student.getClassName());
        }

        EvaluationResult eval = evalMap.get(submission.getId());
        if (eval != null) {
            row.setAiScore(eval.getAiScore());
            row.setAiIssues(eval.getAiIssues());
            row.setAiComment(eval.getAiComment());
            row.setDimensionScores(eval.getDimensionScores());
            row.setTeacherScore(eval.getTeacherScore());
            row.setTeacherComment(eval.getTeacherComment());
            row.setStatusText(switch (eval.getStatus()) {
                case 0 -> "未评价";
                case 1 -> "已AI评价";
                default -> "教师已确认";
            });
        } else {
            row.setStatusText("未评价");
        }

        return row;
    }
}
