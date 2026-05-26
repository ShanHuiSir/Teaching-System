package com.teachingeval.service;

import com.teachingeval.dto.AIEvalRequest;
import com.teachingeval.entity.AIEvaluationResult;

public interface AIService {
    AIEvaluationResult evaluate(AIEvalRequest request);
}
