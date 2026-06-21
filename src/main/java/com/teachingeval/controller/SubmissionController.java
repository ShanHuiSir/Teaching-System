package com.teachingeval.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.teachingeval.repository.SubmissionFileRepository;
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
    private final SubmissionFileRepository submissionFileRepository;

    public SubmissionController(SubmissionService submissionService,
                                SubmissionRepository submissionRepository,
                                StudentRepository studentRepository,
                                TeachingClassRepository teachingClassRepository,
                                TeacherRepository teacherRepository,
                                TeachingClassService teachingClassService,
                                SubmissionFileRepository submissionFileRepository) {
        this.submissionService = submissionService;
        this.submissionRepository = submissionRepository;
        this.studentRepository = studentRepository;
        this.teachingClassRepository = teachingClassRepository;
        this.teacherRepository = teacherRepository;
        this.teachingClassService = teachingClassService;
        this.submissionFileRepository = submissionFileRepository;
    }

    @Operation(summary = "查询作品提交列表", description = "返回当前教师管辖班级学生的作品提交记录。")
    @GetMapping("/submissions")
    public List<WorkSubmission> listSubmissions(HttpServletRequest request) {
        List<Long> classIds = teachingClassService.resolveTeacherClassIds(request);
        if (classIds == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无法获取教师班级范围");
        }
        if (classIds.isEmpty()) return List.of();
        List<Long> studentIds = studentRepository.findByClassIdIn(classIds).stream()
                .map(com.teachingeval.entity.Student::getId)
                .toList();
        if (studentIds.isEmpty()) return List.of();
        return submissionRepository.findByStudentIdIn(studentIds);
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
    public List<SubmissionFile> listSubmissionFiles(@PathVariable Long id, HttpServletRequest request) {
        ensureCurrentTeacherCanAccessSubmission(id, request);
        return submissionService.listSubmissionFiles(id);
    }

    private SubmissionFile resolveFile(Long submissionId, Long fileId) {
        if (fileId != null) {
            return submissionFileRepository.findById(fileId)
                    .filter(f -> f.getSubmissionId().equals(submissionId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在"));
        }
        return submissionService.getPrimaryFile(submissionId);
    }

    @Operation(summary = "下载作品文件", description = "根据提交ID及可选文件ID下载对应的原始作业文件。不传 fileId 时返回主文件。")
    @GetMapping("/submissions/{id}/file")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id,
                                                  @RequestParam(required = false) Long fileId,
                                                  HttpServletRequest request) {
        ensureCurrentTeacherCanAccessSubmission(id, request);

        SubmissionFile file = resolveFile(id, fileId);
        if (file.getFilePath() == null || file.getFilePath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在");
        }
        Path path = Path.of(file.getFilePath());
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在");
        }

        String fileName = file.getFileName() != null ? file.getFileName() : path.getFileName().toString();
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : "";
        String mimeType = resolveContentType(path, ext);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(new FileSystemResource(path));
    }

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp", "gif", "bmp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "mov", "avi");

    private static final Map<String, String> EXT_TO_MIME = Map.ofEntries(
            Map.entry("png", "image/png"), Map.entry("jpg", "image/jpeg"), Map.entry("jpeg", "image/jpeg"),
            Map.entry("webp", "image/webp"), Map.entry("gif", "image/gif"), Map.entry("bmp", "image/bmp"),
            Map.entry("mp4", "video/mp4"), Map.entry("webm", "video/webm"),
            Map.entry("mov", "video/quicktime"), Map.entry("avi", "video/x-msvideo")
    );

    private String resolveContentType(Path path, String ext) {
        try {
            String probed = Files.probeContentType(path);
            if (probed != null) return probed;
        } catch (IOException ignored) {
            // probeContentType 可能因系统不支持而失败，回退到扩展名映射
        }
        return EXT_TO_MIME.getOrDefault(ext, "application/octet-stream");
    }

    @Operation(summary = "预览作品文件", description = "支持图片/视频在线预览。不传 fileId 时预览主文件。")
    @GetMapping("/submissions/{id}/preview")
    public void previewFile(@PathVariable Long id,
                            @RequestParam(required = false) Long fileId,
                            HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        ensureCurrentTeacherCanAccessSubmission(id, request);

        SubmissionFile file = resolveFile(id, fileId);
        if (file.getFilePath() == null || file.getFilePath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在");
        }
        Path path = Path.of(file.getFilePath());
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "文件不存在");
        }

        String fileName = file.getFileName() != null ? file.getFileName() : path.getFileName().toString();
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : "";
        String mimeType = resolveContentType(path, ext);

        if (IMAGE_EXTENSIONS.contains(ext)) {
            response.setContentType(mimeType);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
            response.setContentLengthLong(Files.size(path));
            Files.copy(path, response.getOutputStream());
            return;
        }

        if (VIDEO_EXTENSIONS.contains(ext)) {
            long fileSize = Files.size(path);
            String rangeHeader = request.getHeader("Range");

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                // 解析 Range: "bytes=0-1048575"
                String rangeValue = rangeHeader.substring("bytes=".length());
                String[] parts = rangeValue.split("-", 2);
                long start = Long.parseLong(parts[0]);
                long end = parts.length > 1 && !parts[1].isEmpty()
                        ? Long.parseLong(parts[1])
                        : Math.min(start + 1024 * 1024 - 1, fileSize - 1);
                if (end >= fileSize) end = fileSize - 1;
                long contentLength = end - start + 1;

                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setContentType(mimeType);
                response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize);
                response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
                response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

                try (InputStream in = Files.newInputStream(path);
                     OutputStream out = response.getOutputStream()) {
                    in.skip(start);
                    byte[] buffer = new byte[8192];
                    long remaining = contentLength;
                    while (remaining > 0) {
                        int read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                        if (read == -1) break;
                        out.write(buffer, 0, read);
                        remaining -= read;
                    }
                }
                return;
            }

            // 无 Range 头：返回完整文件
            response.setContentType(mimeType);
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));
            response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
            Files.copy(path, response.getOutputStream());
            return;
        }

        // 不支持在线预览的类型，回退到下载
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "不支持在线预览此文件类型");
    }

    private void ensureCurrentTeacherCanAccessSubmission(Long submissionId, HttpServletRequest request) {
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
