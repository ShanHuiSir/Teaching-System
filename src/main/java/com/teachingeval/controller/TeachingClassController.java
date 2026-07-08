package com.teachingeval.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.teachingeval.dto.TeachingClassRequest;
import com.teachingeval.entity.TeachingClass;
import com.teachingeval.service.TeachingClassService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "班级管理")
@RestController
@RequestMapping("/api/classes")
public class TeachingClassController {

    private final TeachingClassService teachingClassService;

    public TeachingClassController(TeachingClassService teachingClassService) {
        this.teachingClassService = teachingClassService;
    }

    @Operation(summary = "查询班级列表", description = "返回当前教师管辖的正式班级表数据。")
    @GetMapping
    public List<TeachingClass> listClasses(HttpServletRequest request) {
        return teachingClassService.listClassesForTeacher(request);
    }

    @Operation(summary = "新增班级", description = "录入班级名称、年级和说明。班级名称不能重复。")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeachingClass createClass(@Valid @RequestBody TeachingClassRequest request,
                                     HttpServletRequest httpRequest) {
        return teachingClassService.createClass(request, httpRequest);
    }

    @Operation(summary = "更新班级", description = "更新班级名称、年级和说明。")
    @PutMapping("/{id}")
    public TeachingClass updateClass(@PathVariable Long id,
                                     @Valid @RequestBody TeachingClassRequest request) {
        return teachingClassService.updateClass(id, request);
    }

    @Operation(summary = "删除班级", description = "删除没有学生和作业关联的班级。")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClass(@PathVariable Long id) {
        teachingClassService.deleteClass(id);
    }
}
