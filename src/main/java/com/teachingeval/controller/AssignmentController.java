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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.teachingeval.dto.AssignmentRequest;
import com.teachingeval.entity.Assignment;
import com.teachingeval.service.AssignmentService;
import com.teachingeval.service.TeachingClassService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "作业管理")
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final TeachingClassService teachingClassService;

    public AssignmentController(AssignmentService assignmentService,
                                TeachingClassService teachingClassService) {
        this.assignmentService = assignmentService;
        this.teachingClassService = teachingClassService;
    }

    @Operation(summary = "查询作业列表", description = "返回当前教师管辖班级的正式作业表数据，可按班级 ID 筛选。")
    @GetMapping
    public List<Assignment> listAssignments(@RequestParam(required = false) Long classId,
                                            HttpServletRequest request) {
        List<Long> teacherClassIds = teachingClassService.resolveTeacherClassIds(request);
        return assignmentService.listAssignments(classId, teacherClassIds);
    }

    @Operation(summary = "新增作业", description = "录入作业标题、类型、班级归属和截止时间。")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Assignment createAssignment(@Valid @RequestBody AssignmentRequest request) {
        return assignmentService.createAssignment(request);
    }

    @Operation(summary = "更新作业", description = "更新作业标题、类型、班级归属和截止时间。仅允许更新本班作业。")
    @PutMapping("/{id}")
    public Assignment updateAssignment(@PathVariable Long id,
                                       @Valid @RequestBody AssignmentRequest request,
                                       HttpServletRequest httpRequest) {
        verifyAssignmentOwnership(id, httpRequest);
        return assignmentService.updateAssignment(id, request);
    }

    @Operation(summary = "删除作业", description = "删除没有提交记录关联的作业。仅允许删除本班作业。")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAssignment(@PathVariable Long id, HttpServletRequest request) {
        verifyAssignmentOwnership(id, request);
        assignmentService.deleteAssignment(id);
    }

    private void verifyAssignmentOwnership(Long assignmentId, HttpServletRequest request) {
        List<Long> teacherClassIds = teachingClassService.resolveTeacherClassIds(request);
        if (teacherClassIds == null) return;
        assignmentService.verifyClassOwnership(assignmentId, teacherClassIds);
    }
}
