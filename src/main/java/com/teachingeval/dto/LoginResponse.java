package com.teachingeval.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "教师登录响应")
public record LoginResponse(
        @Schema(description = "登录账户名", example = "teacher")
        String username,
        @Schema(description = "会话有效期秒数", example = "43200")
        long expiresInSeconds
) {
}
