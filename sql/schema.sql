-- 教学评价系统
-- MySQL 8.0+

CREATE TABLE student (
    id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    sno        VARCHAR(32)  NOT NULL                 COMMENT '学号',
    name       VARCHAR(64)  NOT NULL                 COMMENT '姓名',
    major      VARCHAR(128) NOT NULL DEFAULT ''      COMMENT '专业',
    class_name VARCHAR(128) NOT NULL DEFAULT ''      COMMENT '班级',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sno (sno)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

CREATE TABLE submission (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    student_id      BIGINT       NOT NULL                 COMMENT '学生ID',
    original_name   VARCHAR(512) NOT NULL                 COMMENT '原始文件名',
    storage_path    VARCHAR(1024)NOT NULL                 COMMENT '服务器存储位置',
    uploaded_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (id),
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品提交表';

CREATE TABLE evaluation (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    submission_id   BIGINT       NOT NULL                 COMMENT '提交ID',
    ai_score        DECIMAL(5,2)                          COMMENT 'AI建议分数',
    ai_issues       TEXT                                  COMMENT 'AI发现的问题',
    ai_comment      TEXT                                  COMMENT 'AI评语',
    teacher_score   DECIMAL(5,2)                          COMMENT '教师评分',
    teacher_comment TEXT                                  COMMENT '教师评语',
    status          TINYINT      NOT NULL DEFAULT 0       COMMENT '状态：0-未评价，1-AI已评价，2-教师已确认',
    PRIMARY KEY (id),
    INDEX idx_submission_id (submission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';
