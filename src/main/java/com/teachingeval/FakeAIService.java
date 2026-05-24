package com.teachingeval;

import java.math.BigDecimal;

/**
 * [1. 类概述]
 * 模拟 AI 评价服务，在真实 AI 接口就绪前提供固定的评价数据，
 * 用于前端联调与功能验证。
 * <p>
 * [2. 成员变量详解]
 * 当前无成员变量，所有逻辑内聚在 evaluate 方法中。
 * <p>
 * [3. 方法调用指南]
 * - 核心入口 evaluate(String studentName, String fileName)：
 *   接收学生姓名与作品文件名，返回一个填充了固定 aiScore、aiIssues、
 *   aiComment 及 status=1 的 AIEvaluationResult 对象。
 * - 调用方无需任何前置条件，直接构造 FakeAIService 实例即可调用。
 * <p>
 * [4. 继承与实现关系]
 * - 直接继承 java.lang.Object，无实现的接口。
 * - 隶属于系统的服务层 (Service)，后续可替换为真实的 AI 服务实现。
 */
public class FakeAIService {

    public AIEvaluationResult evaluate(String studentName, String fileName) {
        AIEvaluationResult result = new AIEvaluationResult();
        result.setAiScore(new BigDecimal("82.50"));

        result.setAiIssues("""
                1. 结构不够清晰，建议优化段落层次
                2. 缺少核心论点支撑材料
                3. 格式规范性不足，标题层级需统一""");

        result.setAiComment("整体完成度较好，但在结构组织上还有提升空间，建议加强逻辑连贯性。");
        result.setStatus(1);
        return result;
    }
}
