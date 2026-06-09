package com.teachingeval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "作品提交请求")
public class SubmissionRequest {

    @NotNull(message = "学生ID不能为空")
    @Schema(description = "学生 ID", example = "1")
    private Long studentId;

    @Schema(description = "关联作业 ID；传入后后端会写入 assignmentId 和 assignmentTitle", example = "1")
    private Long assignmentId;

    @NotBlank(message = "作品标题不能为空")
    @Schema(description = "作品标题", example = "第二阶段实训报告")
    private String title;

    @NotBlank(message = "文件名不能为空")
    @Schema(description = "作品文件名", example = "student-work.zip")
    private String fileName;

    @NotBlank(message = "作品类型不能为空")
    @Schema(description = "作品类型", example = "实验报告")
    private String workType;

    @Schema(description = "备注说明", example = "包含源码和报告")
    private String remark;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
