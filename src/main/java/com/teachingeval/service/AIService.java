package com.teachingeval.service;

import com.teachingeval.dto.AIEvalRequest;
import com.teachingeval.entity.EvaluationResult;

public interface AIService {
    EvaluationResult evaluate(AIEvalRequest request);
}
