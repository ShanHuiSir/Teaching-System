package com.teachingeval.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "student")
@Schema(description = "学生实体，保存教学评价系统中的学生基础信息")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键，数据库自增，新建学生时可不传", example = "1")
    private Long id;

    @Column(name = "student_no", nullable = false, unique = true, length = 32)
    @Schema(description = "学号，学生在系统中的唯一编号", example = "2026001")
    private String studentNo;

    @Column(name = "name", nullable = false, length = 64)
    @Schema(description = "学生姓名", example = "张三")
    private String name;

    @Column(name = "class_name", nullable = false, length = 64)
    @Schema(description = "班级名称", example = "软件 1 班")
    private String className;

    public Student() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
