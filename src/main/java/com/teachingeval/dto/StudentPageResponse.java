package com.teachingeval.dto;

import com.teachingeval.entity.Student;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "学生分页查询响应")
public record StudentPageResponse(
        @Schema(description = "当前页学生列表")
        List<Student> content,

        @Schema(description = "当前页页码，从 0 开始", example = "0")
        int page,

        @Schema(description = "每页数量", example = "20")
        int size,

        @Schema(description = "总学生数", example = "8")
        long totalElements,

        @Schema(description = "总页数", example = "1")
        int totalPages,

        @Schema(description = "是否存在上一页", example = "false")
        boolean hasPrevious,

        @Schema(description = "是否存在下一页", example = "false")
        boolean hasNext
) {
    public static StudentPageResponse from(Page<Student> page) {
        return new StudentPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasPrevious(),
                page.hasNext()
        );
    }
}
