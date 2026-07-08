package com.teachingeval.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.teachingeval.dto.StudentRequest;
import com.teachingeval.dto.StudentRosterImportResponse;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.TeachingClass;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.TeachingClassRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StudentService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 200;
    private static final int MAX_IMPORT_ROWS = 1000;
    private static final long MAX_IMPORT_FILE_SIZE = 5 * 1024 * 1024L;
    private static final int MAX_IMPORT_MESSAGES = 20;

    private final StudentRepository studentRepository;
    private final TeachingClassRepository teachingClassRepository;

    public StudentService(StudentRepository studentRepository,
                          TeachingClassRepository teachingClassRepository) {
        this.studentRepository = studentRepository;
        this.teachingClassRepository = teachingClassRepository;
    }

    public List<Student> listStudents() {
        return studentRepository.findAll(PageRequest.of(0, MAX_PAGE_SIZE, Sort.by("id").ascending()))
                .getContent();
    }

    public List<Student> listStudents(List<Long> teacherClassIds) {
        if (teacherClassIds == null) return listStudents();
        return studentRepository.findByClassIdIn(teacherClassIds);
    }

    public Page<Student> listStudentPage(Integer page, Integer size, String keyword) {
        return listStudentPage(page, size, keyword, null);
    }

    public Page<Student> listStudentPage(Integer page, Integer size, String keyword, List<Long> teacherClassIds) {
        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? DEFAULT_PAGE_SIZE : Math.max(size, 1);
        safeSize = Math.min(safeSize, MAX_PAGE_SIZE);

        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        List<Student> filtered = listStudents(teacherClassIds).stream()
                .filter(s -> normalizedKeyword.isEmpty()
                        || (s.getStudentNo() != null && s.getStudentNo().toLowerCase().contains(normalizedKeyword.toLowerCase()))
                        || (s.getName() != null && s.getName().toLowerCase().contains(normalizedKeyword.toLowerCase())))
                .toList();

        int total = filtered.size();
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / safeSize);
        int fromIndex = Math.min(safePage * safeSize, total);
        int toIndex = Math.min(fromIndex + safeSize, total);
        List<Student> pageContent = filtered.subList(fromIndex, toIndex);

        return new org.springframework.data.domain.PageImpl<>(pageContent,
                org.springframework.data.domain.PageRequest.of(safePage, safeSize), total);
    }

    public Student createStudent(StudentRequest request) {
        if (studentRepository.existsByStudentNo(request.getStudentNo())) {
            throw new IllegalArgumentException("学号已存在");
        }

        Student student = new Student();
        student.setStudentNo(request.getStudentNo());
        student.setName(request.getName());
        TeachingClass teachingClass = resolveTeachingClass(request.getClassId(), request.getClassName());
        student.setClassId(teachingClass.getId());
        student.setClassName(teachingClass.getName());
        return studentRepository.save(student);
    }

    @Transactional
    public StudentRosterImportResponse importRoster(Long classId, MultipartFile file, List<Long> teacherClassIds) {
        if (classId == null) {
            throw new IllegalArgumentException("班级不能为空");
        }
        if (teacherClassIds != null && !teacherClassIds.contains(classId)) {
            throw new IllegalArgumentException("无权导入该班级花名册");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传花名册文件");
        }
        if (file.getSize() > MAX_IMPORT_FILE_SIZE) {
            throw new IllegalArgumentException("花名册文件不能超过5MB");
        }

        TeachingClass teachingClass = teachingClassRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("班级不存在"));
        List<RosterRow> rows = readRosterRows(file);

        int created = 0;
        int updated = 0;
        int skipped = 0;
        List<String> messages = new ArrayList<>();
        Set<String> importedStudentNos = new HashSet<>();

        for (RosterRow row : rows) {
            if (isHeaderRow(row.studentNo(), row.name())) {
                continue;
            }
            String studentNo = normalize(row.studentNo());
            String name = normalize(row.name());
            if (studentNo.isEmpty() && name.isEmpty()) {
                continue;
            }
            if (studentNo.isEmpty() || name.isEmpty()) {
                skipped++;
                addImportMessage(messages, "第" + row.rowNumber() + "行缺少学号或姓名，已跳过");
                continue;
            }
            if (!importedStudentNos.add(studentNo)) {
                skipped++;
                addImportMessage(messages, "第" + row.rowNumber() + "行学号" + studentNo + "在文件中重复，已跳过");
                continue;
            }

            Student student = studentRepository.findByStudentNo(studentNo).orElse(null);
            if (student == null) {
                student = new Student();
                student.setStudentNo(studentNo);
                created++;
            } else {
                updated++;
            }
            student.setName(name);
            student.setClassId(teachingClass.getId());
            student.setClassName(teachingClass.getName());
            studentRepository.save(student);
        }

        return new StudentRosterImportResponse(created + updated, created, updated, skipped, messages);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException("学生不存在");
        }

        studentRepository.deleteById(id);
    }

    private List<RosterRow> readRosterRows(MultipartFile file) {
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        try {
            if (fileName.endsWith(".csv")) {
                return readCsvRows(file);
            }
            if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                return readExcelRows(file);
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("花名册文件读取失败");
        }
        throw new IllegalArgumentException("仅支持 .xlsx、.xls 或 .csv 花名册文件");
    }

    private List<RosterRow> readExcelRows(MultipartFile file) throws IOException {
        List<RosterRow> rows = new ArrayList<>();
        EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                if (rows.size() >= MAX_IMPORT_ROWS) {
                    return;
                }
                int rowNumber = context.readRowHolder().getRowIndex() + 1;
                rows.add(new RosterRow(rowNumber, valueAt(data, 0), valueAt(data, 1)));
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
            }
        }).sheet().headRowNumber(0).doRead();
        return rows;
    }

    private List<RosterRow> readCsvRows(MultipartFile file) throws IOException {
        List<RosterRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowNumber = 0;
            while ((line = reader.readLine()) != null && rows.size() < MAX_IMPORT_ROWS) {
                rowNumber++;
                List<String> columns = splitCsvLine(line);
                rows.add(new RosterRow(rowNumber, valueAt(columns, 0), valueAt(columns, 1)));
            }
        }
        return rows;
    }

    private List<String> splitCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                columns.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        columns.add(current.toString());
        return columns;
    }

    private String valueAt(Map<Integer, String> row, int index) {
        if (row == null) {
            return "";
        }
        String value = row.get(index);
        return value == null ? "" : value;
    }

    private String valueAt(List<String> row, int index) {
        if (row == null || index >= row.size()) {
            return "";
        }
        return row.get(index);
    }

    private boolean isHeaderRow(String first, String second) {
        String studentNo = normalize(first).replace(" ", "");
        String name = normalize(second).replace(" ", "");
        return ("学号".equals(studentNo) || "学生学号".equals(studentNo) || "studentno".equalsIgnoreCase(studentNo))
                && ("姓名".equals(name) || "学生姓名".equals(name) || "name".equalsIgnoreCase(name));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\uFEFF", "").trim();
    }

    private void addImportMessage(List<String> messages, String message) {
        if (messages.size() < MAX_IMPORT_MESSAGES) {
            messages.add(message);
        }
    }

    private TeachingClass resolveTeachingClass(Long classId, String className) {
        if (classId != null) {
            return teachingClassRepository.findById(classId)
                    .orElseThrow(() -> new IllegalArgumentException("班级不存在"));
        }

        String normalizedClassName = className == null ? "" : className.trim();
        if (normalizedClassName.isEmpty()) {
            throw new IllegalArgumentException("班级不能为空");
        }

        return teachingClassRepository.findByName(normalizedClassName)
                .orElseGet(() -> {
                    TeachingClass teachingClass = new TeachingClass();
                    teachingClass.setName(normalizedClassName);
                    teachingClass.setGrade(resolveGrade(normalizedClassName));
                    return teachingClassRepository.save(teachingClass);
                });
    }

    private String resolveGrade(String className) {
        if (className.length() >= 4 && className.substring(0, 4).chars().allMatch(Character::isDigit)) {
            return className.substring(0, 4);
        }
        return "2026";
    }

    private record RosterRow(int rowNumber, String studentNo, String name) {
    }
}
