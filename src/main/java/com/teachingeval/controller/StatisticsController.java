package com.teachingeval.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teachingeval.dto.StatisticsSummaryResponse;
import com.teachingeval.service.StatisticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "成绩统计")
@RestController
@RequestMapping("/api")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Operation(summary = "查询成绩统计摘要", description = "返回学生数、作品数、AI 已评价数、教师已确认数和平均分。")
    @GetMapping("/statistics/summary")
    public StatisticsSummaryResponse getSummary() {
        return statisticsService.getSummary();
    }
}
