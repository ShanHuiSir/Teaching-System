package com.teachingeval.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.teachingeval.dto.AIEvalRequest;
import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.service.AIService;

@Service
public class DelegatingAIService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(DelegatingAIService.class);

    private final FakeAIService fakeAIService;
    private final RealAIService realAIService;
    private final Environment environment;

    public DelegatingAIService(FakeAIService fakeAIService,
                               RealAIService realAIService,
                               Environment environment) {
        this.fakeAIService = fakeAIService;
        this.realAIService = realAIService;
        this.environment = environment;
    }

    @Override
    public EvaluationResult evaluate(AIEvalRequest request) {
        // TODO: 演示结束后移除此方法，统一使用两参数 evaluate(WorkSubmission, AIEvalRequest)
        boolean realAiEnabled = environment.getProperty("app.ai.real.enabled", Boolean.class, false);
        if (realAiEnabled) {
            throw new IllegalStateException("真实 AI 模式不支持单参数 evaluate 接口，请提供 WorkSubmission 后再调用");
        }
        EvaluationResult result = fakeAIService.evaluate(request);
        result.setAiSource(EvaluationResult.AI_SOURCE_FAKE);
        return result;
    }

    @Override
    public EvaluationResult evaluate(WorkSubmission submission, AIEvalRequest request) {
        boolean realAiEnabled = environment.getProperty("app.ai.real.enabled", Boolean.class, false);
        if (!realAiEnabled) {
            EvaluationResult result = fakeAIService.evaluate(request);
            result.setAiSource(EvaluationResult.AI_SOURCE_FAKE);
            return result;
        }
        try {
            EvaluationResult result = realAIService.evaluate(submission, request);
            result.setAiSource(EvaluationResult.AI_SOURCE_REAL);
            return result;
        } catch (RestClientException | IllegalStateException | IllegalArgumentException exception) {
            // TODO: 演示结束后移除此 try-catch 回退，让真实 AI 异常直接向上传播
            log.warn("真实 AI 评价失败，已降级为模拟评价，submissionId={}", submission.getId(), exception);
            EvaluationResult result = fakeAIService.evaluate(request);
            result.setAiSource(EvaluationResult.AI_SOURCE_FAKE_FALLBACK);
            return result;
        }
    }
}
