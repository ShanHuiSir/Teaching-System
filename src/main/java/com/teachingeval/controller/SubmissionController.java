package com.teachingeval.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.service.SubmissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "作品提交")
@RestController
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @Operation(summary = "查询作品提交列表", description = "返回系统中已录入的全部作品提交记录。")
    @GetMapping("/submissions")
    public List<WorkSubmission> listSubmissions() {
        return submissionService.listSubmissions();
    }

    @Operation(summary = "新增作品提交", description = "录入学生作品元数据，第二天阶段暂不上传真实文件。")
    @PostMapping("/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkSubmission createSubmission(@RequestBody WorkSubmission submission) {
        return submissionService.createSubmission(submission);
    }
}
