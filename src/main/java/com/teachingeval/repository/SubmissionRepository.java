package com.teachingeval.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teachingeval.entity.WorkSubmission;

public interface SubmissionRepository extends JpaRepository<WorkSubmission, Long> {

    long countByAssignmentId(Long assignmentId);
}
