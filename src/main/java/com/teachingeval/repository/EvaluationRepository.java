package com.teachingeval.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teachingeval.model.AIEvaluationResult;

public interface EvaluationRepository extends JpaRepository<AIEvaluationResult, Long> {
    Optional<AIEvaluationResult> findBySubmissionId(Long submissionId);

    long countByStatusGreaterThanEqual(int status);
}
