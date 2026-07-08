package com.teachingeval.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.teachingeval.dto.TeachingClassRequest;
import com.teachingeval.entity.Teacher;
import com.teachingeval.entity.TeachingClass;
import com.teachingeval.repository.AssignmentClassRepository;
import com.teachingeval.repository.AssignmentRepository;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.TeacherRepository;
import com.teachingeval.repository.TeachingClassRepository;

@Service
public class TeachingClassService {

    private final TeachingClassRepository teachingClassRepository;
    private final StudentRepository studentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentClassRepository assignmentClassRepository;
    private final TeacherRepository teacherRepository;
    private final boolean authEnabled;

    public TeachingClassService(TeachingClassRepository teachingClassRepository,
                                StudentRepository studentRepository,
                                AssignmentRepository assignmentRepository,
                                AssignmentClassRepository assignmentClassRepository,
                                TeacherRepository teacherRepository,
                                @Value("${app.auth.enabled:true}") boolean authEnabled) {
        this.teachingClassRepository = teachingClassRepository;
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentClassRepository = assignmentClassRepository;
        this.teacherRepository = teacherRepository;
        this.authEnabled = authEnabled;
    }

    public List<TeachingClass> listClasses() {
        return teachingClassRepository.findAll(Sort.by("id").ascending());
    }

    public List<TeachingClass> listClassesForTeacher(HttpServletRequest request) {
        List<Long> ids = resolveTeacherClassIds(request);
        if (ids == null) return listClasses();
        return teachingClassRepository.findAllById(ids).stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .toList();
    }

    public List<Long> resolveTeacherClassIds(HttpServletRequest request) {
        String username = (String) request.getAttribute(AuthService.AUTH_USER_ATTRIBUTE);
        if (username == null) {
            // 认证禁用时（测试 / 演示环境）返回全部班级 ID，保持与其他端点一致的行为
            if (!authEnabled) {
                return teachingClassRepository.findAll().stream().map(TeachingClass::getId).toList();
            }
            return null;
        }
        Teacher teacher = teacherRepository.findByUsername(username).orElse(null);
        if (teacher == null) return List.of();
        List<TeachingClass> classes = teachingClassRepository.findByTeacherId(teacher.getId());
        List<Long> ids = new ArrayList<>();
        for (TeachingClass c : classes) {
            ids.add(c.getId());
        }
        return ids;
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

    public TeachingClass updateClass(Long id, TeachingClassRequest request) {
        TeachingClass teachingClass = teachingClassRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("班级不存在"));
        String name = request.getName().trim();
        if (!teachingClass.getName().equals(name) && teachingClassRepository.existsByName(name)) {
            throw new IllegalArgumentException("班级已存在");
        }

        teachingClass.setName(name);
        teachingClass.setGrade(normalizeNullable(request.getGrade()));
        teachingClass.setDescription(normalizeNullable(request.getDescription()));
        return teachingClassRepository.save(teachingClass);
    }

    public void deleteClass(Long id) {
        if (!teachingClassRepository.existsById(id)) {
            throw new IllegalArgumentException("班级不存在");
        }
        if (studentRepository.countByClassId(id) > 0
                || assignmentRepository.countByClassId(id) > 0
                || assignmentClassRepository.countByClassId(id) > 0) {
            throw new IllegalArgumentException("班级已有学生或作业关联，不能删除");
        }
        teachingClassRepository.deleteById(id);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
