package com.teachingeval.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.teachingeval.entity.AssignmentClass;

public interface AssignmentClassRepository extends JpaRepository<AssignmentClass, Long> {

    List<AssignmentClass> findByAssignmentIdOrderByIdAsc(Long assignmentId);

    List<AssignmentClass> findByAssignmentIdInOrderByAssignmentIdAscIdAsc(Collection<Long> assignmentIds);

    long countByClassId(Long classId);

    void deleteByAssignmentId(Long assignmentId);

    @Query("select distinct ac.assignmentId from AssignmentClass ac where ac.classId = :classId")
    List<Long> findAssignmentIdsByClassId(@Param("classId") Long classId);
}
