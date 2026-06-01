package com.teachingeval.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.teachingeval.dto.SubmissionRequest;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.service.SubmissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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

    @Operation(summary = "新增作品提交", description = "录入学生作品元数据，学生ID、标题、文件名、作品类型均为必填。")
    @PostMapping("/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public WorkSubmission createSubmission(@Valid @RequestBody SubmissionRequest request) {
        return submissionService.createSubmission(request);
    }

    @Operation(summary = "上传并新增作品提交", description = "接收真实作业文件，保存文件并生成作品提交记录。")
    @PostMapping(value = "/submissions/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public WorkSubmission uploadSubmission(@RequestParam Long studentId,
                                           @RequestParam String title,
                                           @RequestParam String workType,
                                           @RequestParam(required = false) String remark,
                                           @RequestParam MultipartFile file) {
        return submissionService.createSubmissionWithFile(studentId, title, workType, remark, file);
    }
}
