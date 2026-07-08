package com.teachingeval.controller;

import com.teachingeval.dto.AIEvalRequest;
import com.teachingeval.dto.SaveEvaluationRequest;
import com.teachingeval.dto.TeacherReviewRequest;
import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.Teacher;
import com.teachingeval.entity.TeachingClass;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionRepository;
import com.teachingeval.repository.TeacherRepository;
import com.teachingeval.repository.TeachingClassRepository;
import com.teachingeval.service.AuthService;
import com.teachingeval.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "AI 评价")
@RestController
@RequestMapping("/api")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final TeachingClassRepository teachingClassRepository;
    private final TeacherRepository teacherRepository;

    public EvaluationController(EvaluationService evaluationService,
                                SubmissionRepository submissionRepository,
                                StudentRepository studentRepository,
                                TeachingClassRepository teachingClassRepository,
                                TeacherRepository teacherRepository) {
        this.evaluationService = evaluationService;
        this.submissionRepository = submissionRepository;
        this.studentRepository = studentRepository;
        this.teachingClassRepository = teachingClassRepository;
        this.teacherRepository = teacherRepository;
    }

    @Operation(summary = "执行 AI 评价并保存", description = "接收学生作品提交信息，调用 AI 评价服务并保存评分、问题列表和综合评语。")
    @PostMapping("/submissions/{submissionId}/evaluate")
    public EvaluationResult evaluate(@PathVariable Long submissionId,
                                     @RequestBody AIEvalRequest request) {
        return evaluationService.evaluate(submissionId, request);
    }

    @Operation(summary = "保存 AI 评分结果", description = "接收流式评分或外部计算好的 AI 评分结果，持久化到数据库。不触发新的 AI 调用。")
    @PostMapping("/submissions/{submissionId}/evaluation-result")
    public EvaluationResult saveAiResult(@PathVariable Long submissionId,
                                         @Valid @RequestBody SaveEvaluationRequest request) {
        return evaluationService.saveAiResult(submissionId, request);
    }

    @Operation(summary = "查询评价结果", description = "根据作品提交 ID 查询 AI 和教师评价结果。")
    @GetMapping("/submissions/{submissionId}/evaluation")
    public EvaluationResult getEvaluation(@PathVariable Long submissionId) {
        return evaluationService.getBySubmissionId(submissionId);
    }

    @Operation(summary = "查询全部评价结果", description = "分页返回评价结果，默认每页 200 条。")
    @GetMapping("/evaluations")
    public List<EvaluationResult> listEvaluations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "200") int size) {
        return evaluationService.listEvaluations(PageRequest.of(page, size));
    }

    @Operation(summary = "保存教师最终评价", description = "保存教师最终评分（0-100）和评语，并将评价状态改为教师已确认。仅允许当前教师管辖班级的提交。")
    @PostMapping("/submissions/{submissionId}/teacher-review")
    public EvaluationResult saveTeacherReview(@PathVariable Long submissionId,
                                              @Valid @RequestBody TeacherReviewRequest request,
                                              HttpServletRequest httpRequest) {
        ensureTeacherCanAccessSubmission(submissionId, httpRequest);
        return evaluationService.saveTeacherReview(submissionId, request);
    }

    private void ensureTeacherCanAccessSubmission(Long submissionId, HttpServletRequest request) {
        WorkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "提交记录不存在"));
        Student student = studentRepository.findById(submission.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问此提交"));
        if (student.getClassId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问此提交");
        }
        TeachingClass teachingClass = teachingClassRepository.findById(student.getClassId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问此提交"));
        if (teachingClass.getTeacherId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问此提交");
        }
        Teacher teacher = teacherRepository.findById(teachingClass.getTeacherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问此提交"));
        String currentUser = (String) request.getAttribute(AuthService.AUTH_USER_ATTRIBUTE);
        if (currentUser != null && !teacher.getUsername().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问此提交");
        }
    }
}
