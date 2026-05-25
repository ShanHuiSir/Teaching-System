package com.teachingeval.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 评价请求 DTO，封装前端提交的评价参数")
public class AIEvalRequestDTO {

    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @Schema(description = "作品文件名", example = "实验报告.docx")
    private String fileName;

    public AIEvalRequestDTO() {}

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    @Override
    public String toString() {
        return "AIEvalRequestDTO{" +
                "studentName='" + studentName + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
