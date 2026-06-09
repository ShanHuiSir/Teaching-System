package com.teachingeval.service;

import com.teachingeval.dto.StudentRequest;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.TeachingClass;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.TeachingClassRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;

    private final StudentRepository studentRepository;
    private final TeachingClassRepository teachingClassRepository;

    public StudentService(StudentRepository studentRepository,
                          TeachingClassRepository teachingClassRepository) {
        this.studentRepository = studentRepository;
        this.teachingClassRepository = teachingClassRepository;
    }

    public List<Student> listStudents() {
        return studentRepository.findAll(PageRequest.of(0, MAX_PAGE_SIZE, Sort.by("id").ascending()))
                .getContent();
    }

    public Page<Student> listStudentPage(Integer page, Integer size, String keyword) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? DEFAULT_PAGE_SIZE : Math.max(size, 1);
        safeSize = Math.min(safeSize, MAX_PAGE_SIZE);

        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("id").ascending());
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isEmpty()) {
            return studentRepository.findAll(pageRequest);
        }
        return studentRepository.findByStudentNoContainingIgnoreCaseOrNameContainingIgnoreCase(
                normalizedKeyword,
                normalizedKeyword,
                pageRequest
        );
    }

    public Student createStudent(StudentRequest request) {
        if (studentRepository.existsByStudentNo(request.getStudentNo())) {
            throw new IllegalArgumentException("学号已存在");
        }

        Student student = new Student();
        student.setStudentNo(request.getStudentNo());
        student.setName(request.getName());
        TeachingClass teachingClass = resolveTeachingClass(request.getClassId(), request.getClassName());
        student.setClassId(teachingClass.getId());
        student.setClassName(teachingClass.getName());
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException("学生不存在");
        }

        studentRepository.deleteById(id);
    }

    private TeachingClass resolveTeachingClass(Long classId, String className) {
        if (classId != null) {
            return teachingClassRepository.findById(classId)
                    .orElseThrow(() -> new IllegalArgumentException("班级不存在"));
        }

        String normalizedClassName = className == null ? "" : className.trim();
        if (normalizedClassName.isEmpty()) {
            throw new IllegalArgumentException("班级不能为空");
        }

        return teachingClassRepository.findByName(normalizedClassName)
                .orElseGet(() -> {
                    TeachingClass teachingClass = new TeachingClass();
                    teachingClass.setName(normalizedClassName);
                    teachingClass.setGrade(resolveGrade(normalizedClassName));
                    return teachingClassRepository.save(teachingClass);
                });
    }

    private String resolveGrade(String className) {
        if (className.length() >= 4 && className.substring(0, 4).chars().allMatch(Character::isDigit)) {
            return className.substring(0, 4);
        }
        return "2026";
    }
}
