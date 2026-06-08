package com.teachingeval.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 评价请求 DTO，封装前端提交的评价参数")
public class AIEvalRequest {

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "作品文件名", example = "实验报告.docx")
    private String fileName;

    @Schema(description = "作业主题类型：code / document / design / general", example = "code")
    private String subjectType;

    @Schema(description = "自定义评分维度 JSON 字符串，为空则使用默认维度")
    private String rubric;

    public AIEvalRequest() {}

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getSubjectType() { return subjectType; }
    public void setSubjectType(String subjectType) { this.subjectType = subjectType; }

    public String getRubric() { return rubric; }
    public void setRubric(String rubric) { this.rubric = rubric; }

    @Override
    public String toString() {
        return "AIEvalRequest{" +
                "studentName='" + studentName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", subjectType='" + subjectType + '\'' +
                '}';
    }
}
