package com.teachingeval.controller;

import com.teachingeval.config.DataInitializer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "开发数据工具")
@RestController
@RequestMapping("/api/dev")
@Profile("!prod")
public class DevDataController {

    private final DataInitializer dataInitializer;

    public DevDataController(DataInitializer dataInitializer) {
        this.dataInitializer = dataInitializer;
    }

    @Operation(summary = "重置演示数据", description = "开发环境使用：清空学生、作品和评价数据，并恢复初始化测试数据。")
    @PostMapping("/reset-demo-data")
    public Map<String, String> resetDemoData() {
        dataInitializer.resetDemoData();
        return Map.of("message", "演示数据已重置");
    }
}
