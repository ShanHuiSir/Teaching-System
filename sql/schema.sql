-- 教学评价系统
-- MySQL 8.0+

CREATE TABLE teacher (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    username     VARCHAR(32)  NOT NULL                COMMENT '登录用户名',
    password     VARCHAR(128) NOT NULL                COMMENT '登录密码',
    display_name VARCHAR(64)  NOT NULL                COMMENT '显示名称',
    PRIMARY KEY (id),
    UNIQUE KEY uk_teacher_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师表';

CREATE TABLE teaching_class (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    name        VARCHAR(64)  NOT NULL                COMMENT '班级名称',
    grade       VARCHAR(16)                          COMMENT '年级',
    teacher_id  BIGINT                               COMMENT '所属教师ID',
    description TEXT                                 COMMENT '班级说明',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_teaching_class_name (name),
    INDEX idx_teaching_class_teacher_id (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

CREATE TABLE student (
    id         BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    student_no VARCHAR(32)  NOT NULL                 COMMENT '学号',
    name       VARCHAR(64)  NOT NULL                 COMMENT '姓名',
    class_id   BIGINT                               COMMENT '班级ID',
    class_name VARCHAR(64)  NOT NULL                 COMMENT '班级名称快照',
    PRIMARY KEY (id),
    UNIQUE KEY uk_student_no (student_no),
    INDEX idx_student_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

CREATE TABLE assignment (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title        VARCHAR(128) NOT NULL                COMMENT '作业标题',
    description  TEXT                                COMMENT '作业说明',
    work_type    VARCHAR(32)  NOT NULL                COMMENT '作品类型',
    class_id     BIGINT                              COMMENT '班级ID，空表示跨班级作业',
    class_name   VARCHAR(64)                         COMMENT '班级名称快照',
    published_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    due_at       DATETIME                            COMMENT '截止时间',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_assignment_class_id (class_id),
    INDEX idx_assignment_published_at (published_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业表';

CREATE TABLE assignment_class (
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    assignment_id BIGINT      NOT NULL                COMMENT '作业ID',
    class_id      BIGINT      NOT NULL                COMMENT '班级ID',
    class_name    VARCHAR(64) NOT NULL                COMMENT '班级名称快照',
    PRIMARY KEY (id),
    UNIQUE KEY uk_assignment_class_assignment_class (assignment_id, class_id),
    INDEX idx_assignment_class_assignment_id (assignment_id),
    INDEX idx_assignment_class_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作业受理班级关联表';

CREATE TABLE submission (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    student_id      BIGINT       NOT NULL                 COMMENT '学生ID',
    student_name    VARCHAR(64)  NOT NULL                 COMMENT '学生姓名快照',
    assignment_id   BIGINT                                COMMENT '作业ID',
    assignment_title VARCHAR(128)                         COMMENT '作业标题快照',
    title           VARCHAR(128) NOT NULL                 COMMENT '作品标题',
    file_name       VARCHAR(512) NOT NULL                 COMMENT '主文件名快照',
    file_path       VARCHAR(1024)                         COMMENT '主文件保存路径快照',
    file_size       BIGINT                                COMMENT '主文件大小，单位字节',
    content_type    VARCHAR(128)                          COMMENT '主文件MIME类型',
    preprocess_status  VARCHAR(32)                        COMMENT 'Py预处理状态',
    preprocess_message VARCHAR(512)                       COMMENT 'Py预处理状态说明',
    preprocess_result  LONGTEXT                           COMMENT 'Py预处理原始响应JSON',
    work_type       VARCHAR(32)  NOT NULL                 COMMENT '作品类型',
    remark          TEXT                                  COMMENT '备注',
    submitted_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_submission_student_id (student_id),
    INDEX idx_submission_assignment_id (assignment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品提交表';

CREATE TABLE submission_file (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    submission_id BIGINT       NOT NULL                COMMENT '提交ID',
    file_name     VARCHAR(512) NOT NULL                COMMENT '原始文件名',
    file_path     VARCHAR(1024)                        COMMENT '服务器保存路径',
    file_size     BIGINT                              COMMENT '文件大小，单位字节',
    content_type  VARCHAR(128)                        COMMENT '文件MIME类型',
    file_role     VARCHAR(32)  NOT NULL DEFAULT 'PRIMARY' COMMENT '文件角色',
    primary_file  BOOLEAN      NOT NULL DEFAULT TRUE   COMMENT '是否主文件',
    sort_order    INT          NOT NULL DEFAULT 0      COMMENT '排序',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_submission_file_submission_id (submission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提交文件明细表';

CREATE TABLE evaluation (
    id              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    submission_id   BIGINT       NOT NULL                 COMMENT '提交ID',
    ai_score        DECIMAL(5,2)                          COMMENT 'AI建议分数',
    ai_issues       TEXT                                  COMMENT 'AI发现的问题',
    ai_comment        TEXT                                COMMENT 'AI评语',
    dimension_scores  TEXT                                COMMENT 'AI分维度评分JSON',
    teacher_score     DECIMAL(5,2)                        COMMENT '教师评分',
    teacher_comment TEXT                                  COMMENT '教师评语',
    status          TINYINT      NOT NULL DEFAULT 0       COMMENT '状态：0-未评价，1-AI已评价，2-教师已确认',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_evaluation_submission_id (submission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';
