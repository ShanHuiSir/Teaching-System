package com.teachingeval.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.teachingeval.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByStudentNo(String studentNo);

    Optional<Student> findByStudentNo(String studentNo);

    long countByClassId(Long classId);

    List<Student> findByClassIdIn(Collection<Long> classIds);

    Page<Student> findByStudentNoContainingIgnoreCaseOrNameContainingIgnoreCase(String studentNo,
                                                                               String name,
                                                                               Pageable pageable);
}
