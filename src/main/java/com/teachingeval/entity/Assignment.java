package com.teachingeval.entity;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "assignment")
@Schema(description = "作业实体，定义班级下需要提交和评价的作业")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键，数据库自增", example = "1")
    private Long id;

    @Column(name = "title", nullable = false, length = 128)
    @Schema(description = "作业标题", example = "第二阶段实训报告")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    @Schema(description = "作业说明", example = "提交阶段报告、源码和运行截图")
    private String description;

    @Column(name = "work_type", nullable = false, length = 32)
    @Schema(description = "作品类型", example = "实验报告")
    private String workType;

    @Column(name = "class_id")
    @Schema(description = "所属班级 ID；为空表示跨班级作业", example = "1")
    private Long classId;

    @Column(name = "class_name", length = 64)
    @Schema(description = "班级名称快照", example = "软件 1 班")
    private String className;

    @Column(name = "published_at", nullable = false)
    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;

    @Column(name = "due_at")
    @Schema(description = "截止时间")
    private LocalDateTime dueAt;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (publishedAt == null) {
            publishedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
