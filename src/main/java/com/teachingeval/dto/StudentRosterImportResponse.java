package com.teachingeval.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "学生花名册导入结果")
public record StudentRosterImportResponse(
        @Schema(description = "成功读取的有效行数", example = "42")
        int total,
        @Schema(description = "新增学生数", example = "38")
        int created,
        @Schema(description = "更新学生数", example = "4")
        int updated,
        @Schema(description = "跳过行数", example = "1")
        int skipped,
        @Schema(description = "跳过或错误说明")
        List<String> messages
) {
}
