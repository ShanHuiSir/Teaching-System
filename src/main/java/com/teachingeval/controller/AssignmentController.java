package com.teachingeval.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.teachingeval.dto.AssignmentRequest;
import com.teachingeval.entity.Assignment;
import com.teachingeval.service.AssignmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "作业管理")
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @Operation(summary = "查询作业列表", description = "返回系统中的正式作业表数据，可按班级 ID 筛选。")
    @GetMapping
    public List<Assignment> listAssignments(@RequestParam(required = false) Long classId) {
        return assignmentService.listAssignments(classId);
    }

    @Operation(summary = "新增作业", description = "录入作业标题、类型、班级归属和截止时间。")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Assignment createAssignment(@Valid @RequestBody AssignmentRequest request) {
        return assignmentService.createAssignment(request);
    }
}
