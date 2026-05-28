package com.teachingeval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "学生创建/更新请求")
public class StudentRequest {

    @NotBlank(message = "学号不能为空")
    @Schema(description = "学号", example = "2026001")
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "学生姓名", example = "张三")
    private String name;

    @NotBlank(message = "班级不能为空")
    @Schema(description = "班级名称", example = "软件 1 班")
    private String className;

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
