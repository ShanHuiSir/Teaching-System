package com.teachingeval.service;

import com.teachingeval.entity.Student;
import com.teachingeval.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> listStudents() {
        return listStudentsPage(0, MAX_PAGE_SIZE).getContent();
    }

    public Page<Student> listStudentsPage(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return studentRepository.findAll(pageable);
    }

    public Page<Student> listStudentsPage(Integer page, Integer size) {
        return listStudentsPage(
                page == null ? 0 : page,
                size == null ? DEFAULT_PAGE_SIZE : size
        );
    }

    public Student createStudent(Student student) {
        if (studentRepository.existsByStudentNo(student.getStudentNo())) {
            throw new IllegalArgumentException("学号已存在");
        }

        student.setId(null);
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException("学生不存在");
        }

        studentRepository.deleteById(id);
    }
}
