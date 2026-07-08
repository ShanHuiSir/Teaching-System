package com.teachingeval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "班级创建请求")
public class TeachingClassRequest {

    @NotBlank(message = "班级名称不能为空")
    @Schema(description = "班级名称", example = "软件 1 班")
    private String name;

    @Schema(description = "年级", example = "2026")
    private String grade;

    @Schema(description = "班级说明", example = "软件工程实训演示班级")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
