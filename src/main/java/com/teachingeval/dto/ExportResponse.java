package com.teachingeval.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "导出文件响应")
public record ExportResponse(
        @Schema(description = "文件下载地址", example = "/files/export/成绩汇总_2026.xlsx")
        String fileUrl
) {
}
