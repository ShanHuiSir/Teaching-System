package com.teachingeval.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 评价请求 DTO，封装前端提交的评价参数")
public class AIEvalRequest {

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "作品文件名", example = "实验报告.docx")
    private String fileName;

    public AIEvalRequest() {}

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    @Override
    public String toString() {
        return "AIEvalRequest{" +
                "studentName='" + studentName + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
