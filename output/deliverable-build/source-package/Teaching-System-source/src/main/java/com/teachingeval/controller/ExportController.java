package com.teachingeval.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.teachingeval.service.ExportService;
import com.teachingeval.service.TeachingClassService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "成绩导出")
@RestController
@RequestMapping("/api")
public class ExportController {

    private final ExportService exportService;
    private final TeachingClassService teachingClassService;

    public ExportController(ExportService exportService,
                            TeachingClassService teachingClassService) {
        this.exportService = exportService;
        this.teachingClassService = teachingClassService;
    }

    @Operation(summary = "导出成绩 Excel", description = "按作业、班级、作品类型过滤导出成绩汇总 Excel 文件。仅允许导出当前教师管辖班级的数据。")
    @PostMapping("/export/excel")
    public void exportExcel(@RequestParam(required = false) Long assignmentId,
                            @RequestParam(required = false) Long classId,
                            @RequestParam(required = false) String workType,
                            HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        List<Long> teacherClassIds = teachingClassService.resolveTeacherClassIds(request);
        if (teacherClassIds == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无法获取教师班级范围");
        }
        if (classId != null && !teacherClassIds.contains(classId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权导出该班级的数据");
        }

        String filename = "成绩汇总_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        exportService.exportTo(response.getOutputStream(), assignmentId, classId, workType);
    }
}
