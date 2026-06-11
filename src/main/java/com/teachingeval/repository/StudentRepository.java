package com.teachingeval.repository;

import com.teachingeval.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByStudentNo(String studentNo);

    long countByClassId(Long classId);

    Page<Student> findByStudentNoContainingIgnoreCaseOrNameContainingIgnoreCase(String studentNo,
                                                                               String name,
                                                                               Pageable pageable);
}
