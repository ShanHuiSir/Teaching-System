package com.teachingeval.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teachingeval.dto.ExportResponse;
import com.teachingeval.service.ExportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "成绩导出")
@RestController
@RequestMapping("/api")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @Operation(summary = "导出成绩 Excel", description = "导出学生成绩汇总 Excel 文件，返回文件下载地址。")
    @PostMapping("/export/excel")
    public ExportResponse exportExcel() {
        return exportService.exportExcel();
    }
}
