package com.teachingeval.service;

import com.teachingeval.dto.AIEvalRequest;
import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.WorkSubmission;

public interface AIService {
    EvaluationResult evaluate(AIEvalRequest request);

    // TODO: 演示结束后移除此 default 方法，强制实现类覆盖
    default EvaluationResult evaluate(WorkSubmission submission, AIEvalRequest request) {
        throw new UnsupportedOperationException("必须由具体实现类覆盖 evaluate(WorkSubmission, AIEvalRequest)");
    }
}
