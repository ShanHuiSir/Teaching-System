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
import com.teachingeval.entity.Assignment;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.SubmissionFile;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.repository.AssignmentRepository;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionFileRepository;
import com.teachingeval.repository.SubmissionRepository;

@Service
public class SubmissionService {

    private static final long MAX_UPLOAD_SIZE = 50L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "doc", "docx", "pdf", "zip", "rar", "7z", "tar", "gz",
            "txt", "java", "py", "png", "jpg", "jpeg", "webp",
            "cpp", "c", "h", "hpp", "js", "ts", "html", "css",
            "md", "json", "xml", "yaml", "yml", "sql", "sh", "csv", "log"
    );

    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final PreprocessClient preprocessClient;
    private final Path uploadRoot;
    private final String uploadRootForResponse;

    public SubmissionService(SubmissionRepository submissionRepository,
                             StudentRepository studentRepository,
                             AssignmentRepository assignmentRepository,
                             SubmissionFileRepository submissionFileRepository,
                             PreprocessClient preprocessClient,
                             @Value("${app.upload.root:uploads}") String uploadRoot) {
        this.submissionRepository = submissionRepository;
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionFileRepository = submissionFileRepository;
        this.preprocessClient = preprocessClient;
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
        Assignment assignment = resolveAssignment(request.getAssignmentId());

        WorkSubmission submission = new WorkSubmission();
        submission.setStudentId(request.getStudentId());
        submission.setStudentName(student.getName());
        applyAssignmentSnapshot(submission, assignment);
        submission.setTitle(request.getTitle());
        submission.setFileName(request.getFileName());
        submission.setWorkType(request.getWorkType());
        submission.setRemark(request.getRemark());
        WorkSubmission saved = submissionRepository.save(submission);
        savePrimaryFileRecord(saved, saved.getFileName(), saved.getFilePath(), saved.getFileSize(), saved.getContentType());
        return saved;
    }

    @Transactional
    public WorkSubmission createSubmissionWithFile(Long studentId,
                                                   Long assignmentId,
                                                   String title,
                                                   String workType,
                                                   String remark,
                                                   MultipartFile file) {
        validateUploadRequest(studentId, title, workType, file);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        Assignment assignment = resolveAssignment(assignmentId);

        String originalFileName = cleanFileName(file);
        WorkSubmission submission = new WorkSubmission();
        submission.setStudentId(studentId);
        submission.setStudentName(student.getName());
        applyAssignmentSnapshot(submission, assignment);
        submission.setTitle(title.trim());
        submission.setFileName(originalFileName);
        submission.setWorkType(workType.trim());
        submission.setRemark(remark);
        submission.setFileSize(file.getSize());
        submission.setContentType(resolveContentType(file));

        WorkSubmission saved = submissionRepository.save(submission);
        Path savedPath = storeFile(saved.getId(), originalFileName, file);
        saved.setFilePath(toResponsePath(savedPath));
        savePrimaryFileRecord(saved, originalFileName, saved.getFilePath(), saved.getFileSize(), saved.getContentType());
        PreprocessResult preprocessResult = preprocessClient.submit(
                saved.getId(),
                saved.getStudentId(),
                saved.getTitle(),
                saved.getWorkType(),
                saved.getFileName(),
                saved.getContentType(),
                savedPath
        );
        saved.setPreprocessStatus(preprocessResult.getStatus());
        saved.setPreprocessMessage(preprocessResult.getMessage());
        saved.setPreprocessResult(preprocessResult.getRawResponse());
        return submissionRepository.save(saved);
    }

    @Transactional(readOnly = true)
    public List<SubmissionFile> listSubmissionFiles(Long submissionId) {
        ensureSubmissionExists(submissionId);
        return submissionFileRepository.findBySubmissionIdOrderBySortOrderAscIdAsc(submissionId);
    }

    public SubmissionFile getPrimaryFile(Long submissionId) {
        WorkSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("提交记录不存在"));
        return submissionFileRepository.findFirstBySubmissionIdAndPrimaryFileTrueOrderBySortOrderAscIdAsc(submissionId)
                .orElseGet(() -> buildLegacyPrimaryFile(submission));
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

    private Assignment resolveAssignment(Long assignmentId) {
        if (assignmentId == null) {
            return null;
        }
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("作业不存在"));
    }

    private void applyAssignmentSnapshot(WorkSubmission submission, Assignment assignment) {
        if (assignment == null) {
            return;
        }
        submission.setAssignmentId(assignment.getId());
        submission.setAssignmentTitle(assignment.getTitle());
        if (submission.getWorkType() == null || submission.getWorkType().isBlank()) {
            submission.setWorkType(assignment.getWorkType());
        }
    }

    private void savePrimaryFileRecord(WorkSubmission submission,
                                       String fileName,
                                       String filePath,
                                       Long fileSize,
                                       String contentType) {
        if (submission.getId() == null || fileName == null || fileName.isBlank()) {
            return;
        }

        submissionFileRepository.findFirstBySubmissionIdAndPrimaryFileTrueOrderBySortOrderAscIdAsc(submission.getId())
                .ifPresentOrElse(file -> {
                    file.setFileName(fileName);
                    file.setFilePath(filePath);
                    file.setFileSize(fileSize);
                    file.setContentType(contentType);
                    file.setFileRole("PRIMARY");
                    file.setPrimaryFile(true);
                    file.setSortOrder(0);
                    submissionFileRepository.save(file);
                }, () -> {
                    SubmissionFile file = new SubmissionFile();
                    file.setSubmissionId(submission.getId());
                    file.setFileName(fileName);
                    file.setFilePath(filePath);
                    file.setFileSize(fileSize);
                    file.setContentType(contentType);
                    file.setFileRole("PRIMARY");
                    file.setPrimaryFile(true);
                    file.setSortOrder(0);
                    submissionFileRepository.save(file);
                });
    }

    private void ensureSubmissionExists(Long submissionId) {
        if (!submissionRepository.existsById(submissionId)) {
            throw new IllegalArgumentException("提交记录不存在");
        }
    }

    private SubmissionFile buildLegacyPrimaryFile(WorkSubmission submission) {
        if (submission.getFileName() == null || submission.getFileName().isBlank()) {
            throw new IllegalArgumentException("提交记录没有可下载文件");
        }
        SubmissionFile file = new SubmissionFile();
        file.setSubmissionId(submission.getId());
        file.setFileName(submission.getFileName());
        file.setFilePath(submission.getFilePath());
        file.setFileSize(submission.getFileSize());
        file.setContentType(submission.getContentType());
        file.setFileRole("PRIMARY");
        file.setPrimaryFile(true);
        file.setSortOrder(0);
        return file;
    }
}
