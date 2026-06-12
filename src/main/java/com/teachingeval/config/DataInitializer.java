package com.teachingeval.config;

import com.teachingeval.entity.Assignment;
import com.teachingeval.entity.AssignmentClass;
import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.SubmissionFile;
import com.teachingeval.entity.TeachingClass;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.repository.AssignmentClassRepository;
import com.teachingeval.repository.AssignmentRepository;
import com.teachingeval.repository.EvaluationRepository;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionFileRepository;
import com.teachingeval.repository.SubmissionRepository;
import com.teachingeval.repository.TeachingClassRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final TeachingClassRepository teachingClassRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentClassRepository assignmentClassRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final EvaluationRepository evaluationRepository;
    private final Path uploadRoot;

    public DataInitializer(StudentRepository studentRepository,
                           TeachingClassRepository teachingClassRepository,
                           AssignmentRepository assignmentRepository,
                           AssignmentClassRepository assignmentClassRepository,
                           SubmissionRepository submissionRepository,
                           SubmissionFileRepository submissionFileRepository,
                           EvaluationRepository evaluationRepository,
                           @Value("${app.upload.root:uploads}") String uploadRoot) {
        this.studentRepository = studentRepository;
        this.teachingClassRepository = teachingClassRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentClassRepository = assignmentClassRepository;
        this.submissionRepository = submissionRepository;
        this.submissionFileRepository = submissionFileRepository;
        this.evaluationRepository = evaluationRepository;
        this.uploadRoot = Paths.get(uploadRoot).normalize().toAbsolutePath().normalize();
    }

    @Override
    public void run(String... args) {
        seedIfEmpty();
    }

    public void seedIfEmpty() {
        if (studentRepository.count() == 0
                && teachingClassRepository.count() == 0
                && assignmentRepository.count() == 0
                && assignmentClassRepository.count() == 0
                && submissionRepository.count() == 0
                && submissionFileRepository.count() == 0
                && evaluationRepository.count() == 0) {
            resetDemoData();
        }
    }

    public void resetDemoData() {
        evaluationRepository.deleteAll();
        submissionFileRepository.deleteAll();
        submissionRepository.deleteAll();
        assignmentClassRepository.deleteAll();
        assignmentRepository.deleteAll();
        studentRepository.deleteAll();
        teachingClassRepository.deleteAll();

        List<TeachingClass> classes = seedClasses();
        List<Student> students = seedStudents(classes);
        List<Assignment> assignments = seedAssignments(classes);
        List<WorkSubmission> submissions = seedSubmissions(students, assignments);
        copySampleFiles(submissions);
        seedEvaluations(submissions);
    }

    private void copySampleFiles(List<WorkSubmission> submissions) {
        for (WorkSubmission s : submissions) {
            if (s.getFileName() == null || s.getFileName().isBlank()) continue;
            String samplePath = "sample-files/" + s.getFileName();
            try {
                var resource = new ClassPathResource(samplePath);
                if (!resource.exists()) continue;
                byte[] bytes = resource.getInputStream().readAllBytes();

                Path submissionDir = uploadRoot.resolve("submissions").resolve(String.valueOf(s.getId()));
                Files.createDirectories(submissionDir);
                Path dest = submissionDir.resolve(s.getFileName());
                Files.write(dest, bytes);

                s.setFilePath(toResponsePath(dest));
                s.setFileSize((long) bytes.length);
                s.setContentType(resolveContentType(s.getFileName()));
                submissionRepository.save(s);
                savePrimaryFile(s);
            } catch (IOException e) {
                // Seed file missing or IO error — submission stays without file
            }
        }
    }

    private String toResponsePath(Path savedPath) {
        Path relative = uploadRoot.relativize(savedPath);
        return uploadRoot.toString().replace('\\', '/')
                + "/" + relative.toString().replace('\\', '/');
    }

    private String resolveContentType(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return switch (ext) {
            case "txt" -> "text/plain";
            case "cpp", "c", "h", "hpp" -> "text/x-c++src";
            case "java" -> "text/x-java-source";
            case "py" -> "text/x-python";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "html" -> "text/html";
            case "css" -> "text/css";
            case "md" -> "text/markdown";
            case "xml" -> "application/xml";
            default -> "application/octet-stream";
        };
    }

    private List<TeachingClass> seedClasses() {
        return teachingClassRepository.saveAll(List.of(
                buildClass("软件 1 班", "2026", "软件工程实训演示班级"),
                buildClass("软件 2 班", "2026", "软件工程实训演示班级"),
                buildClass("计算机科学 1 班", "2026", "计算机科学实训演示班级"),
                buildClass("计算机科学 2 班", "2026", "计算机科学实训演示班级"),
                buildClass("软件 3 班", "2026", "软件工程实训演示班级")
        ));
    }

    private List<Student> seedStudents(List<TeachingClass> classes) {
        return studentRepository.saveAll(List.of(
                buildStudent("2026001", "张三", classes.get(0)),
                buildStudent("2026002", "李四", classes.get(0)),
                buildStudent("2026003", "王五", classes.get(1)),
                buildStudent("2026004", "赵六", classes.get(1)),
                buildStudent("2026005", "孙七", classes.get(2)),
                buildStudent("2026006", "周八", classes.get(3)),
                buildStudent("2026007", "吴九", classes.get(4)),
                buildStudent("2026008", "郑十", classes.get(4))
        ));
    }

    private List<Assignment> seedAssignments(List<TeachingClass> classes) {
        List<Assignment> assignments = assignmentRepository.saveAll(List.of(
                buildAssignment("第二阶段实训报告", "课程论文", classes.get(0), "提交阶段报告、源码和运行截图"),
                buildAssignment("算法设计与分析", "代码作业", classes.get(0), "提交算法实现与测试说明"),
                buildAssignment("数据结构课程设计", "代码作业", classes.get(1), "提交课程设计源码和说明"),
                buildAssignment("数据库课程设计", "课程论文", classes.get(3), "提交 ER 图、SQL 脚本和设计说明"),
                buildAssignment("操作系统实验报告", "实验报告", classes.get(4), "提交实验记录和调度算法分析"),
                buildAssignment("软件工程课程论文", "课程论文", classes.get(4), "提交课程论文和项目总结")
        ));
        assignmentClassRepository.saveAll(assignments.stream()
                .map(assignment -> buildAssignmentClass(assignment, assignment.getClassId(), assignment.getClassName()))
                .toList());
        return assignments;
    }

    private List<WorkSubmission> seedSubmissions(List<Student> students, List<Assignment> assignments) {
        if (students.size() < 8) {
            throw new IllegalStateException("演示学生数据不足，无法初始化作业数据");
        }
        if (assignments.size() < 6) {
            throw new IllegalStateException("演示作业数据不足，无法初始化提交数据");
        }

        WorkSubmission s1 = new WorkSubmission();
        s1.setStudentId(students.get(0).getId());
        s1.setStudentName(students.get(0).getName());
        applyAssignment(s1, assignments.get(0));
        s1.setTitle("第二阶段实训报告");
        s1.setFileName("project-report.txt");
        s1.setWorkType("课程论文");
        s1.setRemark("包含源码和报告");

        WorkSubmission s2 = new WorkSubmission();
        s2.setStudentId(students.get(1).getId());
        s2.setStudentName(students.get(1).getName());
        applyAssignment(s2, assignments.get(1));
        s2.setTitle("算法设计与分析");
        s2.setFileName("binary-search.cpp");
        s2.setWorkType("代码作业");
        s2.setRemark("二分查找算法实现与测试");

        WorkSubmission s3 = new WorkSubmission();
        s3.setStudentId(students.get(2).getId());
        s3.setStudentName(students.get(2).getName());
        applyAssignment(s3, assignments.get(2));
        s3.setTitle("数据结构课程设计");
        s3.setFileName("binary-search.cpp");
        s3.setWorkType("代码作业");
        s3.setRemark("");

        WorkSubmission s4 = new WorkSubmission();
        s4.setStudentId(students.get(5).getId());
        s4.setStudentName(students.get(5).getName());
        applyAssignment(s4, assignments.get(3));
        s4.setTitle("数据库课程设计");
        s4.setFileName("database-design.txt");
        s4.setWorkType("课程论文");
        s4.setRemark("学生选课管理系统 ER 图与 SQL 脚本");

        WorkSubmission s5 = new WorkSubmission();
        s5.setStudentId(students.get(6).getId());
        s5.setStudentName(students.get(6).getName());
        applyAssignment(s5, assignments.get(4));
        s5.setTitle("操作系统实验报告");
        s5.setFileName("os-experiment.txt");
        s5.setWorkType("实验报告");
        s5.setRemark("进程调度算法对比分析");

        WorkSubmission s6 = new WorkSubmission();
        s6.setStudentId(students.get(7).getId());
        s6.setStudentName(students.get(7).getName());
        applyAssignment(s6, assignments.get(5));
        s6.setTitle("软件工程课程论文");
        s6.setFileName("project-report.txt");
        s6.setWorkType("课程论文");
        s6.setRemark("在线教学评价系统开发总结");

        return submissionRepository.saveAll(List.of(s1, s2, s3, s4, s5, s6));
    }

    private void seedEvaluations(List<WorkSubmission> submissions) {
        if (submissions.size() < 6) {
            throw new IllegalStateException("演示作业数据不足，无法初始化评价数据");
        }

        EvaluationResult ai1 = new EvaluationResult();
        ai1.setSubmissionId(submissions.get(3).getId());
        ai1.setAiScore(new BigDecimal("85.50"));
        ai1.setAiIssues("1. ER 图中部分关系未标注基数\n2. 缺少索引优化说明");
        ai1.setAiComment("数据库设计整体规范，ER 图表达较完整，但在关系标注和性能优化方面还有提升空间。");
        ai1.setDimensionScores("[{\"name\":\"代码质量\",\"score\":88,\"comment\":\"SQL 语句规范\"},{\"name\":\"功能完整性\",\"score\":83,\"comment\":\"ER 图基本完整\"},{\"name\":\"文档与说明\",\"score\":86,\"comment\":\"说明清晰\"},{\"name\":\"创新与优化\",\"score\":85,\"comment\":\"有一定优化思考\"}]");
        ai1.setStatus(EvaluationResult.STATUS_AI_REVIEWED);

        EvaluationResult ai2 = new EvaluationResult();
        ai2.setSubmissionId(submissions.get(4).getId());
        ai2.setAiScore(new BigDecimal("78.00"));
        ai2.setAiIssues("1. 进程调度算法对比不够深入\n2. 缺少死锁预防方案的讨论");
        ai2.setAiComment("实验报告内容较全面，但算法对比分析和异常场景讨论方面有待加强。");
        ai2.setDimensionScores("[{\"name\":\"内容完整性\",\"score\":80,\"comment\":\"覆盖主要实验内容\"},{\"name\":\"逻辑与结构\",\"score\":75,\"comment\":\"结构可优化\"},{\"name\":\"格式规范\",\"score\":78,\"comment\":\"格式基本规范\"},{\"name\":\"表达与创新\",\"score\":79,\"comment\":\"分析略浅\"}]");
        ai2.setStatus(EvaluationResult.STATUS_AI_REVIEWED);

        EvaluationResult tc = new EvaluationResult();
        tc.setSubmissionId(submissions.get(5).getId());
        tc.setAiScore(new BigDecimal("90.00"));
        tc.setAiIssues("1. 缺少具体的团队实践数据支撑");
        tc.setAiComment("选题切合实际，论文结构清晰，建议补充更多实际项目数据来增强说服力。");
        tc.setDimensionScores("[{\"name\":\"内容完整性\",\"score\":92,\"comment\":\"内容充实\"},{\"name\":\"逻辑与结构\",\"score\":90,\"comment\":\"逻辑清晰\"},{\"name\":\"格式规范\",\"score\":89,\"comment\":\"格式规范\"},{\"name\":\"表达与创新\",\"score\":88,\"comment\":\"有一定独立见解\"}]");
        tc.setTeacherScore(new BigDecimal("92.00"));
        tc.setTeacherComment("整体完成较好，论述逻辑清晰，建议后续补充量化数据以增强论文说服力。");
        tc.setStatus(EvaluationResult.STATUS_TEACHER_CONFIRMED);

        evaluationRepository.save(ai1);
        evaluationRepository.save(ai2);
        evaluationRepository.save(tc);
    }

    private void savePrimaryFile(WorkSubmission submission) {
        submissionFileRepository.deleteBySubmissionId(submission.getId());
        SubmissionFile file = new SubmissionFile();
        file.setSubmissionId(submission.getId());
        file.setFileName(submission.getFileName());
        file.setFilePath(submission.getFilePath());
        file.setFileSize(submission.getFileSize());
        file.setContentType(submission.getContentType());
        file.setFileRole("PRIMARY");
        file.setPrimaryFile(true);
        file.setSortOrder(0);
        submissionFileRepository.save(file);
    }

    private static TeachingClass buildClass(String name, String grade, String description) {
        TeachingClass teachingClass = new TeachingClass();
        teachingClass.setName(name);
        teachingClass.setGrade(grade);
        teachingClass.setDescription(description);
        return teachingClass;
    }

    private static Assignment buildAssignment(String title,
                                              String workType,
                                              TeachingClass teachingClass,
                                              String description) {
        Assignment assignment = new Assignment();
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setWorkType(workType);
        assignment.setClassId(teachingClass.getId());
        assignment.setClassName(teachingClass.getName());
        return assignment;
    }

    private static AssignmentClass buildAssignmentClass(Assignment assignment, Long classId, String className) {
        AssignmentClass assignmentClass = new AssignmentClass();
        assignmentClass.setAssignmentId(assignment.getId());
        assignmentClass.setClassId(classId);
        assignmentClass.setClassName(className);
        return assignmentClass;
    }

    private static Student buildStudent(String studentNo, String name, TeachingClass teachingClass) {
        Student s = new Student();
        s.setStudentNo(studentNo);
        s.setName(name);
        s.setClassId(teachingClass.getId());
        s.setClassName(teachingClass.getName());
        return s;
    }

    private static void applyAssignment(WorkSubmission submission, Assignment assignment) {
        submission.setAssignmentId(assignment.getId());
        submission.setAssignmentTitle(assignment.getTitle());
    }
}
