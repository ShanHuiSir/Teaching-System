package com.teachingeval.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.teachingeval.dto.TeachingClassRequest;
import com.teachingeval.entity.TeachingClass;
import com.teachingeval.repository.TeachingClassRepository;

@Service
public class TeachingClassService {

    private final TeachingClassRepository teachingClassRepository;

    public TeachingClassService(TeachingClassRepository teachingClassRepository) {
        this.teachingClassRepository = teachingClassRepository;
    }

    public List<TeachingClass> listClasses() {
        return teachingClassRepository.findAll(Sort.by("id").ascending());
    }

    public TeachingClass createClass(TeachingClassRequest request) {
        String name = request.getName().trim();
        if (teachingClassRepository.existsByName(name)) {
            throw new IllegalArgumentException("班级已存在");
        }

        TeachingClass teachingClass = new TeachingClass();
        teachingClass.setName(name);
        teachingClass.setGrade(normalizeNullable(request.getGrade()));
        teachingClass.setDescription(normalizeNullable(request.getDescription()));
        return teachingClassRepository.save(teachingClass);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
