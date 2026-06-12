package com.teachingeval.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teachingeval.dto.StatisticsSummaryResponse;
import com.teachingeval.service.StatisticsService;
import com.teachingeval.service.TeachingClassService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "成绩统计")
@RestController
@RequestMapping("/api")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final TeachingClassService teachingClassService;

    public StatisticsController(StatisticsService statisticsService,
                                TeachingClassService teachingClassService) {
        this.statisticsService = statisticsService;
        this.teachingClassService = teachingClassService;
    }

    @Operation(summary = "查询成绩统计摘要", description = "返回当前教师管辖班级的学生数、作品数、AI 已评价数、教师已确认数和平均分。")
    @GetMapping("/statistics/summary")
    public StatisticsSummaryResponse getSummary(@RequestParam(required = false) Long assignmentId,
                                                @RequestParam(required = false) Long classId,
                                                HttpServletRequest request) {
        return statisticsService.getSummary(assignmentId, classId,
                teachingClassService.resolveTeacherClassIds(request));
    }
}
