package com.teachingeval.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teachingeval.entity.WorkSubmission;

public interface SubmissionRepository extends JpaRepository<WorkSubmission, Long> {

    long countByAssignmentId(Long assignmentId);

    List<WorkSubmission> findByAssignmentId(Long assignmentId);

    List<WorkSubmission> findByStudentIdIn(Collection<Long> studentIds);
}
