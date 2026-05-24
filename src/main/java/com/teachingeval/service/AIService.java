package com.teachingeval.service;

import com.teachingeval.model.AIEvaluationResult;

/**
 * [1. 类概述]
 * AI 评价服务接口，定义对学生作品提交进行自动评价的契约。
 * 不同实现提供不同的评价策略（如固定值模拟、真实大模型调用）。
 *
 * [2. 成员变量详解]
 * 接口无成员变量，仅定义方法签名。
 *
 * [3. 方法调用指南]
 * - evaluate(String studentName, String fileName)：
 *   接收学生姓名和作品文件名，返回 AIEvaluationResult 评价结果。
 *   调用方通过直接实例化具体实现类获取服务。
 *
 * [4. 继承与实现关系]
 * - 接口，无父接口。
 * - 隶属于系统的服务层 (Service)，由 FakeAIService 等具体类实现。
 */
public interface AIService {
    AIEvaluationResult evaluate(String studentName, String fileName);
}
