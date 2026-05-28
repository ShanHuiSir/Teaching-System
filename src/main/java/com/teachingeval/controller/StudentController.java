package com.teachingeval.controller;

import com.teachingeval.dto.StudentRequest;
import com.teachingeval.entity.Student;
import com.teachingeval.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "学生管理")
@RestController
@RequestMapping("/api")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Operation(summary = "查询学生列表", description = "返回系统中已录入的全部学生基础信息。")
    @GetMapping("/students")
    public List<Student> listStudents() {
        return studentService.listStudents();
    }

    @Operation(summary = "新增学生", description = "录入学生学号、姓名和班级信息，学号不能为空且不能重复。")
    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public Student createStudent(@Valid @RequestBody StudentRequest request) {
        return studentService.createStudent(request);
    }

    @Operation(summary = "删除学生", description = "根据学生主键 ID 删除指定学生。")
    @DeleteMapping("/students/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
    }
}
