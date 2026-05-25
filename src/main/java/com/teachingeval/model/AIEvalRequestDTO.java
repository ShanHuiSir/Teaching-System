package com.teachingeval.model;

/**
 * [1. 类概述]
 * AI 评价请求的 DTO（数据传输对象），封装前端通过 JSON 请求体提交的评价参数。
 * 将参数收拢到一个对象中，便于后续扩展字段而无需修改方法签名。
 * <p>
 * [2. 成员变量详解]
 * - private String studentName: 学生姓名，不可为 null 或空。
 * - private String fileName:    作品文件名，不可为 null 或空。
 * - private Long submissionId:  关联的作品提交 ID，用于追溯评价对应的提交记录。
 * <p>
 * [3. 方法调用指南]
 * - 无参构造函数用于 JSON 反序列化（Jackson），随后由 Spring 自动填充字段。
 * - Controller 中作为 @RequestBody 参数接收：
 *   {@code @PostMapping("/api/evaluate") public AIEvaluationResult evaluate(@RequestBody AIEvalRequestDTO request)}
 * <p>
 * [4. 继承与实现关系]
 * - 直接继承 java.lang.Object，无实现的接口。
 * - 隶属于系统的模型层 (Model/DTO)，仅作为数据传输载体，不参与持久化。
 */
public class AIEvalRequestDTO {

    private String studentName;
    private String fileName;
    private Long submissionId;

    public AIEvalRequestDTO() {}

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    @Override
    public String toString() {
        return "AIEvalRequestDTO{" +
                "studentName='" + studentName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", submissionId=" + submissionId +
                '}';
    }
}
