package com.teachingeval.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teachingeval.entity.TeachingClass;

public interface TeachingClassRepository extends JpaRepository<TeachingClass, Long> {

    boolean existsByName(String name);

    Optional<TeachingClass> findByName(String name);

    List<TeachingClass> findByTeacherId(Long teacherId);
}
