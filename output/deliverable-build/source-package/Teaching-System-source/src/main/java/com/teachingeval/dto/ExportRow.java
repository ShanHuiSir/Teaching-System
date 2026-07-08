package com.teachingeval.dto;

import java.math.BigDecimal;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "成绩导出 Excel 行数据")
public class ExportRow {

    @ExcelProperty("学号")
    @ColumnWidth(12)
    @Schema(description = "学号", example = "2026001")
    private String studentNo;

    @ExcelProperty("姓名")
    @ColumnWidth(10)
    @Schema(description = "学生姓名", example = "张三")
    private String studentName;

    @ExcelProperty("班级")
    @ColumnWidth(14)
    @Schema(description = "班级名称", example = "软件 1 班")
    private String className;

    @ExcelProperty("作品标题")
    @ColumnWidth(22)
    @Schema(description = "作品标题", example = "第二阶段实训报告")
    private String title;

    @ExcelProperty("作品类型")
    @ColumnWidth(12)
    @Schema(description = "作品类型", example = "实验报告")
    private String workType;

    @ExcelProperty("文件名")
    @ColumnWidth(24)
    @Schema(description = "作品文件名", example = "student-work.zip")
    private String fileName;

    @ExcelProperty("AI 评分")
    @ColumnWidth(10)
    @Schema(description = "AI 建议分数", example = "82.50")
    private BigDecimal aiScore;

    @ExcelProperty("AI 发现的问题")
    @ColumnWidth(40)
    @Schema(description = "AI 发现的问题列表")
    private String aiIssues;

    @ExcelProperty("AI 评语")
    @ColumnWidth(40)
    @Schema(description = "AI 综合评语")
    private String aiComment;

    @ExcelProperty("AI 分维度评分")
    @ColumnWidth(50)
    @Schema(description = "AI分维度评分详情")
    private String dimensionScores;

    @ExcelProperty("教师评分")
    @ColumnWidth(10)
    @Schema(description = "教师最终评分", example = "88.00")
    private BigDecimal teacherScore;

    @ExcelProperty("教师评语")
    @ColumnWidth(40)
    @Schema(description = "教师最终评语")
    private String teacherComment;

    @ExcelProperty("评价状态")
    @ColumnWidth(12)
    @Schema(description = "评价状态", example = "教师已确认")
    private String statusText;

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public BigDecimal getAiScore() { return aiScore; }
    public void setAiScore(BigDecimal aiScore) { this.aiScore = aiScore; }

    public String getAiIssues() { return aiIssues; }
    public void setAiIssues(String aiIssues) { this.aiIssues = aiIssues; }

    public String getAiComment() { return aiComment; }
    public void setAiComment(String aiComment) { this.aiComment = aiComment; }

    public String getDimensionScores() { return dimensionScores; }
    public void setDimensionScores(String dimensionScores) { this.dimensionScores = dimensionScores; }

    public BigDecimal getTeacherScore() { return teacherScore; }
    public void setTeacherScore(BigDecimal teacherScore) { this.teacherScore = teacherScore; }

    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
}
