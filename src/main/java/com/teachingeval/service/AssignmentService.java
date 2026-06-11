package com.teachingeval.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.teachingeval.dto.AssignmentRequest;
import com.teachingeval.entity.Assignment;
import com.teachingeval.entity.TeachingClass;
import com.teachingeval.repository.AssignmentRepository;
import com.teachingeval.repository.SubmissionRepository;
import com.teachingeval.repository.TeachingClassRepository;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TeachingClassRepository teachingClassRepository;
    private final SubmissionRepository submissionRepository;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             TeachingClassRepository teachingClassRepository,
                             SubmissionRepository submissionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.teachingClassRepository = teachingClassRepository;
        this.submissionRepository = submissionRepository;
    }

    public List<Assignment> listAssignments(Long classId) {
        if (classId != null) {
            return assignmentRepository.findByClassIdOrderByPublishedAtDesc(classId);
        }
        return assignmentRepository.findAll(Sort.by("publishedAt").descending().and(Sort.by("id").ascending()));
    }

    public Assignment createAssignment(AssignmentRequest request) {
        Assignment assignment = new Assignment();
        applyRequest(assignment, request);
        return assignmentRepository.save(assignment);
    }

    public Assignment updateAssignment(Long id, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("作业不存在"));
        applyRequest(assignment, request);
        return assignmentRepository.save(assignment);
    }

    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new IllegalArgumentException("作业不存在");
        }
        if (submissionRepository.countByAssignmentId(id) > 0) {
            throw new IllegalArgumentException("作业已有提交记录，不能删除");
        }
        assignmentRepository.deleteById(id);
    }

    private void applyRequest(Assignment assignment, AssignmentRequest request) {
        assignment.setTitle(request.getTitle().trim());
        assignment.setDescription(normalizeNullable(request.getDescription()));
        assignment.setWorkType(request.getWorkType().trim());
        assignment.setDueAt(request.getDueAt());

        TeachingClass teachingClass = resolveTeachingClass(request.getClassId(), request.getClassName());
        if (teachingClass == null) {
            assignment.setClassId(null);
            assignment.setClassName(null);
        } else {
            assignment.setClassId(teachingClass.getId());
            assignment.setClassName(teachingClass.getName());
        }
    }

    private TeachingClass resolveTeachingClass(Long classId, String className) {
        if (classId != null) {
            return teachingClassRepository.findById(classId)
                    .orElseThrow(() -> new IllegalArgumentException("班级不存在"));
        }

        String normalizedClassName = className == null ? "" : className.trim();
        if (normalizedClassName.isEmpty()) {
            return null;
        }

        return teachingClassRepository.findByName(normalizedClassName)
                .orElseGet(() -> {
                    TeachingClass teachingClass = new TeachingClass();
                    teachingClass.setName(normalizedClassName);
                    return teachingClassRepository.save(teachingClass);
                });
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
