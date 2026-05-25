package com.teachingeval.repository;

import com.teachingeval.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByStudentNo(String studentNo);
}
