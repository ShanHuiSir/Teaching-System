package com.teachingeval.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

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
        return fakeAIService.evaluate(request);
    }

    @Override
    public EvaluationResult evaluate(WorkSubmission submission, AIEvalRequest request) {
        boolean realAiEnabled = environment.getProperty("app.ai.real.enabled", Boolean.class, false);
        if (!realAiEnabled) {
            return fakeAIService.evaluate(request);
        }
        try {
            return realAIService.evaluate(submission, request);
        } catch (RuntimeException exception) {
            log.warn("真实 AI 评价失败，已降级为模拟评价，submissionId={}", submission.getId(), exception);
            return fakeAIService.evaluate(request);
        }
    }
}
