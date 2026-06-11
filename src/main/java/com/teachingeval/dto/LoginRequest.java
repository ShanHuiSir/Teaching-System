package com.teachingeval.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "教师登录请求")
public class LoginRequest {

    @NotBlank(message = "账户名不能为空")
    @Schema(description = "账户名", example = "teacher")
    private String username;

    @NotBlank(message = "密钥不能为空")
    @Schema(description = "登录密钥", example = "123456")
    private String password;

    @Schema(description = "是否记住登录状态", example = "true")
    private boolean rememberMe;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
