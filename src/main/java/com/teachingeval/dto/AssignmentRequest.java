package com.teachingeval.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "作业创建请求")
public class AssignmentRequest {

    @NotBlank(message = "作业标题不能为空")
    @Schema(description = "作业标题", example = "第二阶段实训报告")
    private String title;

    @Schema(description = "作业说明", example = "提交阶段报告、源码和运行截图")
    private String description;

    @NotBlank(message = "作品类型不能为空")
    @Schema(description = "作品类型", example = "实验报告")
    private String workType;

    @Schema(description = "所属班级 ID；为空表示跨班级作业", example = "1")
    private Long classId;

    @Schema(description = "班级名称；classId 为空时可用于创建/匹配班级", example = "软件 1 班")
    private String className;

    @Schema(description = "截止时间", example = "2026-06-15T23:59:59")
    private LocalDateTime dueAt;

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

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }
}
