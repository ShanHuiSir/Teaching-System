package com.teachingeval.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teachingeval.service.ExportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "成绩导出")
@RestController
@RequestMapping("/api")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @Operation(summary = "导出成绩 Excel", description = "按作业、班级、作品类型过滤导出成绩汇总 Excel 文件。")
    @PostMapping("/export/excel")
    public void exportExcel(@RequestParam(required = false) Long assignmentId,
                            @RequestParam(required = false) Long classId,
                            @RequestParam(required = false) String workType,
                            HttpServletResponse response) throws IOException {
        String filename = "成绩汇总_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        exportService.exportTo(response.getOutputStream(), assignmentId, classId, workType);
    }
}
