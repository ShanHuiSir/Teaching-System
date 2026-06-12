package com.teachingeval.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teachingeval.dto.AssignmentRequest;
import com.teachingeval.entity.Assignment;
import com.teachingeval.entity.AssignmentClass;
import com.teachingeval.entity.TeachingClass;
import com.teachingeval.repository.AssignmentClassRepository;
import com.teachingeval.repository.AssignmentRepository;
import com.teachingeval.repository.SubmissionRepository;
import com.teachingeval.repository.TeachingClassRepository;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentClassRepository assignmentClassRepository;
    private final TeachingClassRepository teachingClassRepository;
    private final SubmissionRepository submissionRepository;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             AssignmentClassRepository assignmentClassRepository,
                             TeachingClassRepository teachingClassRepository,
                             SubmissionRepository submissionRepository) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentClassRepository = assignmentClassRepository;
        this.teachingClassRepository = teachingClassRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional(readOnly = true)
    public List<Assignment> listAssignments(Long classId) {
        return listAssignments(classId, null);
    }

    @Transactional(readOnly = true)
    public List<Assignment> listAssignments(Long classId, List<Long> teacherClassIds) {
        List<Assignment> assignments;
        if (classId != null) {
            List<Long> assignmentIds = assignmentClassRepository.findAssignmentIdsByClassId(classId);
            Map<Long, Assignment> assignmentMap = new LinkedHashMap<>();
            assignmentRepository.findByClassIdOrderByPublishedAtDesc(classId)
                    .forEach(assignment -> assignmentMap.putIfAbsent(assignment.getId(), assignment));
            assignmentRepository.findAllById(assignmentIds)
                    .forEach(assignment -> assignmentMap.putIfAbsent(assignment.getId(), assignment));
            assignments = assignmentMap.values().stream()
                    .sorted((left, right) -> {
                        int byPublishedAt = nullSafeDate(right.getPublishedAt()).compareTo(nullSafeDate(left.getPublishedAt()));
                        return byPublishedAt != 0 ? byPublishedAt : nullSafeLong(left.getId()).compareTo(nullSafeLong(right.getId()));
                    })
                    .toList();
        } else {
            assignments = assignmentRepository.findAll(Sort.by("publishedAt").descending().and(Sort.by("id").ascending()));
        }
        if (teacherClassIds != null) {
            assignments = assignments.stream()
                    .filter(a -> assignmentBelongsToTeacher(a, teacherClassIds))
                    .toList();
        }
        attachClassLists(assignments);
        return assignments;
    }

    public void verifyClassOwnership(Long assignmentId, List<Long> teacherClassIds) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("作业不存在"));
        if (!assignmentBelongsToTeacher(assignment, teacherClassIds)) {
            throw new IllegalArgumentException("无权操作此作业");
        }
    }

    private boolean assignmentBelongsToTeacher(Assignment assignment, List<Long> teacherClassIds) {
        List<AssignmentClass> rows = assignmentClassRepository.findByAssignmentIdOrderByIdAsc(assignment.getId());
        if (rows.isEmpty()) {
            Long legacyClassId = assignment.getClassId();
            return legacyClassId != null && teacherClassIds.contains(legacyClassId);
        }
        return rows.stream().anyMatch(row -> teacherClassIds.contains(row.getClassId()));
    }

    @Transactional
    public Assignment createAssignment(AssignmentRequest request) {
        Assignment assignment = new Assignment();
        applyRequest(assignment, request);
        Assignment saved = assignmentRepository.save(assignment);
        saveAssignmentClasses(saved, request);
        return attachClassLists(saved);
    }

    @Transactional
    public Assignment updateAssignment(Long id, AssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("作业不存在"));
        applyRequest(assignment, request);
        Assignment saved = assignmentRepository.save(assignment);
        saveAssignmentClasses(saved, request);
        return attachClassLists(saved);
    }

    @Transactional
    public void deleteAssignment(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new IllegalArgumentException("作业不存在");
        }
        if (submissionRepository.countByAssignmentId(id) > 0) {
            throw new IllegalArgumentException("作业已有提交记录，不能删除");
        }
        assignmentClassRepository.deleteByAssignmentId(id);
        assignmentRepository.deleteById(id);
    }

    private void applyRequest(Assignment assignment, AssignmentRequest request) {
        assignment.setTitle(request.getTitle().trim());
        assignment.setDescription(normalizeNullable(request.getDescription()));
        assignment.setWorkType(request.getWorkType().trim());
        assignment.setDueAt(request.getDueAt());

        List<TeachingClass> teachingClasses = resolveTeachingClasses(request);
        TeachingClass teachingClass = teachingClasses.isEmpty() ? null : teachingClasses.get(0);
        if (teachingClass == null) {
            assignment.setClassId(null);
            assignment.setClassName(null);
        } else {
            assignment.setClassId(teachingClass.getId());
            assignment.setClassName(teachingClass.getName());
        }
    }

    private void saveAssignmentClasses(Assignment assignment, AssignmentRequest request) {
        assignmentClassRepository.deleteByAssignmentId(assignment.getId());
        assignmentClassRepository.flush();
        List<AssignmentClass> rows = resolveTeachingClasses(request).stream()
                .map(teachingClass -> {
                    AssignmentClass row = new AssignmentClass();
                    row.setAssignmentId(assignment.getId());
                    row.setClassId(teachingClass.getId());
                    row.setClassName(teachingClass.getName());
                    return row;
                })
                .toList();
        assignmentClassRepository.saveAll(rows);
        attachClassLists(assignment, rows);
    }

    private List<TeachingClass> resolveTeachingClasses(AssignmentRequest request) {
        Map<Long, TeachingClass> byId = new LinkedHashMap<>();
        if (request.getClassIds() != null) {
            for (Long classId : request.getClassIds()) {
                if (classId == null) {
                    continue;
                }
                TeachingClass teachingClass = teachingClassRepository.findById(classId)
                        .orElseThrow(() -> new IllegalArgumentException("班级不存在"));
                byId.putIfAbsent(teachingClass.getId(), teachingClass);
            }
        }

        if (!byId.isEmpty()) {
            return new ArrayList<>(byId.values());
        }

        Map<String, TeachingClass> byName = new LinkedHashMap<>();
        if (request.getClassNames() != null) {
            for (String className : request.getClassNames()) {
                TeachingClass teachingClass = resolveTeachingClass(null, className);
                if (teachingClass != null) {
                    byName.putIfAbsent(teachingClass.getName(), teachingClass);
                }
            }
        }

        if (!byName.isEmpty()) {
            return new ArrayList<>(byName.values());
        }

        TeachingClass legacyClass = resolveTeachingClass(request.getClassId(), request.getClassName());
        return legacyClass == null ? List.of() : List.of(legacyClass);
    }

    private Assignment attachClassLists(Assignment assignment) {
        return attachClassLists(assignment, assignmentClassRepository.findByAssignmentIdOrderByIdAsc(assignment.getId()));
    }

    private Assignment attachClassLists(Assignment assignment, List<AssignmentClass> rows) {
        List<Long> classIds = rows.stream().map(AssignmentClass::getClassId).toList();
        List<String> classNames = rows.stream().map(AssignmentClass::getClassName).toList();
        assignment.setClassIds(classIds);
        assignment.setClassNames(classNames);
        if (classIds.isEmpty() && assignment.getClassId() != null) {
            assignment.setClassIds(List.of(assignment.getClassId()));
            assignment.setClassNames(assignment.getClassName() == null ? List.of() : List.of(assignment.getClassName()));
        }
        return assignment;
    }

    private void attachClassLists(List<Assignment> assignments) {
        if (assignments.isEmpty()) {
            return;
        }
        Map<Long, Assignment> assignmentMap = assignments.stream()
                .collect(Collectors.toMap(Assignment::getId, Function.identity()));
        assignmentClassRepository.findByAssignmentIdInOrderByAssignmentIdAscIdAsc(assignmentMap.keySet())
                .stream()
                .collect(Collectors.groupingBy(AssignmentClass::getAssignmentId, LinkedHashMap::new, Collectors.toList()))
                .forEach((assignmentId, rows) -> {
                    Assignment assignment = assignmentMap.get(assignmentId);
                    if (assignment != null) {
                        attachClassLists(assignment, rows);
                    }
                });
        assignments.forEach(this::ensureLegacyClassLists);
    }

    private void ensureLegacyClassLists(Assignment assignment) {
        if ((assignment.getClassIds() == null || assignment.getClassIds().isEmpty()) && assignment.getClassId() != null) {
            assignment.setClassIds(List.of(assignment.getClassId()));
            assignment.setClassNames(assignment.getClassName() == null ? List.of() : List.of(assignment.getClassName()));
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

    private java.time.LocalDateTime nullSafeDate(java.time.LocalDateTime value) {
        return value == null ? java.time.LocalDateTime.MIN : value;
    }

    private Long nullSafeLong(Long value) {
        return value == null ? Long.MIN_VALUE : value;
    }
}
