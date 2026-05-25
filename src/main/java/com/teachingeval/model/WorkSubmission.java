package com.teachingeval.model;

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
@Table(name = "submission")
@Schema(description = "作品提交记录，保存学生作品的基础元数据")
public class WorkSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键，数据库自增", example = "1")
    private Long id;

    @Column(name = "student_id", nullable = false)
    @Schema(description = "学生 ID", example = "1")
    private Long studentId;

    @Column(name = "student_name", nullable = false, length = 64)
    @Schema(description = "学生姓名快照", example = "张三")
    private String studentName;

    @Column(name = "title", nullable = false, length = 128)
    @Schema(description = "作品标题", example = "第二阶段实训报告")
    private String title;

    @Column(name = "file_name", nullable = false, length = 512)
    @Schema(description = "作品文件名", example = "student-work.zip")
    private String fileName;

    @Column(name = "work_type", nullable = false, length = 32)
    @Schema(description = "作品类型", example = "实验报告")
    private String workType;

    @Column(name = "remark", columnDefinition = "TEXT")
    @Schema(description = "备注说明", example = "包含源码和报告")
    private String remark;

    @Column(name = "submitted_at", nullable = false)
    @Schema(description = "提交时间")
    private LocalDateTime submittedAt;

    @PrePersist
    void prePersist() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
