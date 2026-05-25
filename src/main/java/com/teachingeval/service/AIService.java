package com.teachingeval.service;

import com.teachingeval.model.AIEvaluationResult;
import com.teachingeval.model.AIEvalRequestDTO;

/**
 * [1. 类概述]
 * AI 评价服务接口，定义对学生作品提交进行自动评价的契约。
 * 不同实现提供不同的评价策略（如固定值模拟、真实大模型调用）。
 * 在 Spring 容器中，调用方通过 @Autowired 或构造器注入持有本接口引用，
 * 由 Spring 根据 Bean 配置自动选择合适的实现。
 * <p>
 * [2. 成员变量详解]
 * 接口无成员变量，仅定义方法签名。
 * <p>
 * [3. 方法调用指南]
 * - evaluate(AIEvalRequestDTO request)：
 *   接收 AIEvalRequestDTO 传输对象（包含学生姓名、作品文件名、提交 ID），
 *   返回 AIEvaluationResult 评价结果。
 *   调用方通过 Spring DI 注入 AIService 实例后直接调用，无需手动 new。
 * <p>
 * [4. 继承与实现关系]
 * - 接口，无父接口。
 * - 隶属于系统的服务层 (Service)，由 FakeAIService 等具体类实现。
 * - Spring 容器中通过 @Service 注解的实现类自动注册为 Bean。
 */
public interface AIService {
    AIEvaluationResult evaluate(AIEvalRequestDTO request);
}
