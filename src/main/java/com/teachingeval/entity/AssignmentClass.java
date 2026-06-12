package com.teachingeval.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "assignment_class",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_assignment_class_assignment_class",
                columnNames = {"assignment_id", "class_id"}
        ),
        indexes = {
                @Index(name = "idx_assignment_class_assignment_id", columnList = "assignment_id"),
                @Index(name = "idx_assignment_class_class_id", columnList = "class_id")
        }
)
@Schema(description = "作业与受理班级关联")
public class AssignmentClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键，数据库自增", example = "1")
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    @Schema(description = "作业 ID", example = "1")
    private Long assignmentId;

    @Column(name = "class_id", nullable = false)
    @Schema(description = "班级 ID", example = "1")
    private Long classId;

    @Column(name = "class_name", nullable = false, length = 64)
    @Schema(description = "班级名称快照", example = "软件 1 班")
    private String className;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
