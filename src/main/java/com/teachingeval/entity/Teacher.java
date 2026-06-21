package com.teachingeval.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "teacher")
@Schema(description = "教师实体")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键，数据库自增", example = "1")
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 32)
    @Schema(description = "登录用户名", example = "teacher")
    private String username;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "password", nullable = false, length = 128)
    @Schema(description = "登录密码（BCrypt 哈希）", example = "$2a$10$...")
    private String password;

    @Column(name = "display_name", nullable = false, length = 64)
    @Schema(description = "显示名称", example = "张老师")
    private String displayName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
