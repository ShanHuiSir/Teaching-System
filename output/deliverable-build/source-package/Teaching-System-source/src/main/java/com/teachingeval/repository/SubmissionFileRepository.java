package com.teachingeval.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teachingeval.entity.SubmissionFile;

public interface SubmissionFileRepository extends JpaRepository<SubmissionFile, Long> {

    List<SubmissionFile> findBySubmissionIdOrderBySortOrderAscIdAsc(Long submissionId);

    Optional<SubmissionFile> findFirstBySubmissionIdAndPrimaryFileTrueOrderBySortOrderAscIdAsc(Long submissionId);

    void deleteBySubmissionId(Long submissionId);
}
