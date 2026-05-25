-- 教学评价系统
-- MySQL 8.0+

CREATE TABLE student (
    id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    student_no VARCHAR(32)  NOT NULL                 COMMENT '学号',
    name       VARCHAR(64)  NOT NULL                 COMMENT '姓名',
    class_name VARCHAR(64)  NOT NULL                 COMMENT '班级',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

CREATE TABLE submission (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    student_id      BIGINT       NOT NULL                 COMMENT '学生ID',
    student_name    VARCHAR(64)  NOT NULL                 COMMENT '学生姓名快照',
    title           VARCHAR(128) NOT NULL                 COMMENT '作品标题',
    file_name       VARCHAR(512) NOT NULL                 COMMENT '作品文件名',
    work_type       VARCHAR(32)  NOT NULL                 COMMENT '作品类型',
    remark          TEXT                                  COMMENT '备注',
    submitted_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
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
