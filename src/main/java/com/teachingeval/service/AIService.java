package com.teachingeval.service;

import com.teachingeval.model.AIEvalRequestDTO;
import com.teachingeval.model.AIEvaluationResult;

public interface AIService {
    AIEvaluationResult evaluate(AIEvalRequestDTO request);
}
