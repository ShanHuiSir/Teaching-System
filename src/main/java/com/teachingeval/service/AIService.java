package com.teachingeval.service;

import com.teachingeval.dto.AIEvalRequest;
import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.WorkSubmission;

public interface AIService {
    EvaluationResult evaluate(AIEvalRequest request);

    default EvaluationResult evaluate(WorkSubmission submission, AIEvalRequest request) {
        return evaluate(request);
    }
}
