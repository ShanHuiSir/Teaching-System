package com.teachingeval.service.impl;

import java.math.BigDecimal;

import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.service.AIService;
import org.springframework.stereotype.Service;

import com.teachingeval.dto.AIEvalRequest;

@Service
public class FakeAIService implements AIService {

    @Override
    public EvaluationResult evaluate(AIEvalRequest request) {
        EvaluationResult result = new EvaluationResult();
        result.setAiScore(new BigDecimal("82.50"));

        result.setAiIssues("""
                1. 结构不够清晰，建议优化段落层次
                2. 缺少核心论点支撑材料
                3. 格式规范性不足，标题层级需统一""");

        result.setAiComment("整体完成度较好，但在结构组织上还有提升空间，建议加强逻辑连贯性。");
        result.setStatus(EvaluationResult.STATUS_AI_REVIEWED);
        return result;
    }
}
