package com.teachingeval.entity;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "submission_file")
@Schema(description = "提交文件明细，承载一份提交下的一个或多个原始文件")
public class SubmissionFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键，数据库自增", example = "1")
    private Long id;

    @Column(name = "submission_id", nullable = false)
    @Schema(description = "关联提交 ID", example = "1")
    private Long submissionId;

    @Column(name = "file_name", nullable = false, length = 512)
    @Schema(description = "原始文件名", example = "report.docx")
    private String fileName;

    @Column(name = "file_path", length = 1024)
    @Schema(description = "服务器保存路径", example = "uploads/submissions/1/report.docx")
    private String filePath;

    @Column(name = "file_size")
    @Schema(description = "文件大小，单位字节", example = "20480")
    private Long fileSize;

    @Column(name = "content_type", length = 128)
    @Schema(description = "文件 MIME 类型", example = "application/pdf")
    private String contentType;

    @Column(name = "file_role", nullable = false, length = 32)
    @Schema(description = "文件角色，PRIMARY 表示当前主文件", example = "PRIMARY")
    private String fileRole = "PRIMARY";

    @Column(name = "primary_file", nullable = false)
    @Schema(description = "是否为当前提交主文件", example = "true")
    private boolean primaryFile = true;

    @Column(name = "sort_order", nullable = false)
    @Schema(description = "同一提交内文件排序", example = "0")
    private int sortOrder;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (fileRole == null || fileRole.isBlank()) {
            fileRole = "PRIMARY";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileRole() {
        return fileRole;
    }

    public void setFileRole(String fileRole) {
        this.fileRole = fileRole;
    }

    public boolean isPrimaryFile() {
        return primaryFile;
    }

    public void setPrimaryFile(boolean primaryFile) {
        this.primaryFile = primaryFile;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
