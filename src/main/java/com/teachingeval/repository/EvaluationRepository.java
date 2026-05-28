package com.teachingeval.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teachingeval.entity.EvaluationResult;

public interface EvaluationRepository extends JpaRepository<EvaluationResult, Long> {
    Optional<EvaluationResult> findBySubmissionId(Long submissionId);

    long countByStatusGreaterThanEqual(int status);
}
