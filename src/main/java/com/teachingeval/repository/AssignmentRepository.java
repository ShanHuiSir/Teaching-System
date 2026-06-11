package com.teachingeval.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teachingeval.entity.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByClassIdOrderByPublishedAtDesc(Long classId);

    long countByClassId(Long classId);
}
