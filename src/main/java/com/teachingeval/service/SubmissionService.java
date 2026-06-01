package com.teachingeval.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.teachingeval.dto.SubmissionRequest;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionRepository;

@Service
public class SubmissionService {

    private static final long MAX_UPLOAD_SIZE = 50L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "doc", "docx", "pdf", "zip", "rar", "7z", "tar", "gz",
            "txt", "java", "py", "png", "jpg", "jpeg", "webp"
    );

    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final Path uploadRoot;
    private final String uploadRootForResponse;

    public SubmissionService(SubmissionRepository submissionRepository,
                             StudentRepository studentRepository,
                             @Value("${app.upload.root:uploads}") String uploadRoot) {
        this.submissionRepository = submissionRepository;
        this.studentRepository = studentRepository;
        Path configuredUploadRoot = Paths.get(uploadRoot).normalize();
        this.uploadRoot = configuredUploadRoot.toAbsolutePath().normalize();
        this.uploadRootForResponse = configuredUploadRoot.toString().replace('\\', '/');
    }

    public List<WorkSubmission> listSubmissions() {
        return submissionRepository.findAll();
    }

    public WorkSubmission createSubmission(SubmissionRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));

        WorkSubmission submission = new WorkSubmission();
        submission.setStudentId(request.getStudentId());
        submission.setStudentName(student.getName());
        submission.setTitle(request.getTitle());
        submission.setFileName(request.getFileName());
        submission.setWorkType(request.getWorkType());
        submission.setRemark(request.getRemark());
        return submissionRepository.save(submission);
    }

    @Transactional
    public WorkSubmission createSubmissionWithFile(Long studentId,
                                                   String title,
                                                   String workType,
                                                   String remark,
                                                   MultipartFile file) {
        validateUploadRequest(studentId, title, workType, file);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));

        String originalFileName = cleanFileName(file);
        WorkSubmission submission = new WorkSubmission();
        submission.setStudentId(studentId);
        submission.setStudentName(student.getName());
        submission.setTitle(title.trim());
        submission.setFileName(originalFileName);
        submission.setWorkType(workType.trim());
        submission.setRemark(remark);
        submission.setFileSize(file.getSize());
        submission.setContentType(resolveContentType(file));

        WorkSubmission saved = submissionRepository.save(submission);
        Path savedPath = storeFile(saved.getId(), originalFileName, file);
        saved.setFilePath(toResponsePath(savedPath));
        return submissionRepository.save(saved);
    }

    private void validateUploadRequest(Long studentId, String title, String workType, MultipartFile file) {
        if (studentId == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("作品标题不能为空");
        }
        if (workType == null || workType.trim().isEmpty()) {
            throw new IllegalArgumentException("作品类型不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > MAX_UPLOAD_SIZE) {
            throw new IllegalArgumentException("上传文件不能超过50MB");
        }

        String originalFileName = cleanFileName(file);
        if (originalFileName.isBlank()
                || originalFileName.contains("..")
                || originalFileName.contains("/")
                || originalFileName.contains("\\")) {
            throw new IllegalArgumentException("文件名不合法");
        }
        String extension = getExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型");
        }
    }

    private Path storeFile(Long submissionId, String fileName, MultipartFile file) {
        Path submissionDir = uploadRoot.resolve("submissions").resolve(String.valueOf(submissionId)).normalize();
        Path destination = submissionDir.resolve(fileName).normalize();
        if (!destination.startsWith(submissionDir)) {
            throw new IllegalArgumentException("文件名不合法");
        }

        try {
            Files.createDirectories(submissionDir);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return destination;
        } catch (IOException exception) {
            throw new IllegalStateException("文件保存失败");
        }
    }

    private String resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }

    private String cleanFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        return originalFileName == null ? "" : StringUtils.cleanPath(originalFileName);
    }

    private String toResponsePath(Path savedPath) {
        Path relativePath = uploadRoot.relativize(savedPath);
        Path responsePath = Paths.get(uploadRootForResponse).resolve(relativePath).normalize();
        return responsePath.toString().replace('\\', '/');
    }

    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase();
    }
}
