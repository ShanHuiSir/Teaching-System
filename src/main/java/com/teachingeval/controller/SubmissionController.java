package com.teachingeval.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.teachingeval.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.teachingeval.dto.SubmissionRequest;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.SubmissionFile;
import com.teachingeval.entity.Teacher;
import com.teachingeval.entity.TeachingClass;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionRepository;
import com.teachingeval.repository.TeacherRepository;
import com.teachingeval.repository.TeachingClassRepository;
import com.teachingeval.service.AuthService;
import com.teachingeval.service.SubmissionService;
import com.teachingeval.service.TeachingClassService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "作品提交")
@RestController
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final TeachingClassRepository teachingClassRepository;
    private final TeacherRepository teacherRepository;
    private final TeachingClassService teachingClassService;

    public SubmissionController(SubmissionService submissionService,
                                SubmissionRepository submissionRepository,
                                StudentRepository studentRepository,
                                TeachingClassRepository teachingClassRepository,
                                TeacherRepository teacherRepository,
                                TeachingClassService teachingClassService) {
        this.submissionService = submissionService;
        this.submissionRepository = submissionRepository;
        this.studentRepository = studentRepository;
        this.teachingClassRepository = teachingClassRepository;
        this.teacherRepository = teacherRepository;
        this.teachingClassService = teachingClassService;
    }

    @Operation(summary = "查询作品提交列表", description = "返回当前教师管辖班级学生的作品提交记录。")
    @GetMapping("/submissions")
    public List<WorkSubmission> listSubmissions(HttpServletRequest request) {
        List<Long> classIds = teachingClassService.resolveTeacherClassIds(request);
        if (classIds == null) return submissionService.listSubmissions();
        List<Long> studentIds = studentRepository.findAll().stream()
                .filter(s -> s.getClassId() != null && classIds.contains(s.getClassId()))
                .map(com.teachingeval.entity.Student::getId)
                .toList();
        return submissionService.listSubmissions().stream()
                .filter(s -> studentIds.contains(s.getStudentId()))
                .toList();
    }

    @Operation(summary = "新增作品提交", description = "录入学生作品元数据，学生ID、标题、文件名、作品类型均为必填。")
    @PostMapping("/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkSubmission createSubmission(@Valid @RequestBody SubmissionRequest request) {
        return submissionService.createSubmission(request);
    }

    @Operation(summary = "上传并新增作品提交", description = "接收真实作业文件，保存文件并生成作品提交记录。")
    @PostMapping(value = "/submissions/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public WorkSubmission uploadSubmission(@RequestParam Long studentId,
                                           @RequestParam(required = false) Long assignmentId,
                                           @RequestParam String title,
                                           @RequestParam String workType,
                                           @RequestParam(required = false) String remark,
                                           @RequestParam MultipartFile file) {
        return submissionService.createSubmissionWithFile(studentId, assignmentId, title, workType, remark, file);
    }

    @Operation(summary = "查询提交文件列表", description = "返回指定提交下的文件明细，当前上传接口会写入一条主文件记录。")
    @GetMapping("/submissions/{id}/files")
    public List<SubmissionFile> listSubmissionFiles(@PathVariable Long id) {
        return submissionService.listSubmissionFiles(id);
    }

    @Operation(summary = "下载作品文件", description = "根据提交ID下载对应的原始作业文件。仅允许访问本班学生的提交。")
    @GetMapping("/submissions/{id}/file")
    public FileSystemResource downloadFile(@PathVariable Long id, HttpServletRequest request) {
        WorkSubmission submission = submissionRepository.findById(id)
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

        SubmissionFile file = submissionService.getPrimaryFile(id);
        if (file.getFilePath() == null || file.getFilePath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在");
        }
        Path path = Path.of(file.getFilePath());
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在");
        }
        return new FileSystemResource(path);
    }
}
