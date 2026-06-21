package com.teachingeval.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.teachingeval.entity.Assignment;
import com.teachingeval.entity.AssignmentClass;
import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.SubmissionFile;
import com.teachingeval.entity.Teacher;
import com.teachingeval.entity.TeachingClass;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.repository.AssignmentClassRepository;
import com.teachingeval.repository.AssignmentRepository;
import com.teachingeval.repository.EvaluationRepository;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionFileRepository;
import com.teachingeval.repository.SubmissionRepository;
import com.teachingeval.repository.TeacherRepository;
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
import java.time.LocalDateTime;
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
    private final TeacherRepository teacherRepository;
    private final Path uploadRoot;

    public DataInitializer(StudentRepository studentRepository,
                           TeachingClassRepository teachingClassRepository,
                           AssignmentRepository assignmentRepository,
                           AssignmentClassRepository assignmentClassRepository,
                           SubmissionRepository submissionRepository,
                           SubmissionFileRepository submissionFileRepository,
                           EvaluationRepository evaluationRepository,
                           TeacherRepository teacherRepository,
                           @Value("${app.upload.root:uploads}") String uploadRoot) {
        this.studentRepository = studentRepository;
        this.teachingClassRepository = teachingClassRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentClassRepository = assignmentClassRepository;
        this.submissionRepository = submissionRepository;
        this.submissionFileRepository = submissionFileRepository;
        this.evaluationRepository = evaluationRepository;
        this.teacherRepository = teacherRepository;
        this.uploadRoot = Paths.get(uploadRoot).normalize().toAbsolutePath().normalize();
    }

    @Override
    public void run(String... args) {
        seedIfEmpty();
    }

    public void seedIfEmpty() {
        if (studentRepository.count() == 0
                && teachingClassRepository.count() == 0
                && teacherRepository.count() == 0
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
        teacherRepository.deleteAll();

        List<Teacher> teachers = seedTeachers();
        List<TeachingClass> classes = seedClasses(teachers);
        List<Student> students = seedStudents(classes);
        List<Assignment> assignments = seedAssignments(classes);
        List<WorkSubmission> submissions = seedSubmissions(students, assignments);
        copySampleFiles(submissions);
        attachSecondaryFiles(submissions);
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

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private List<Teacher> seedTeachers() {
        return teacherRepository.saveAll(List.of(
                buildTeacher("teacher", PASSWORD_ENCODER.encode("123456"), "张老师"),
                buildTeacher("temp", PASSWORD_ENCODER.encode("123456"), "李老师")
        ));
    }

    private List<TeachingClass> seedClasses(List<Teacher> teachers) {
        Teacher t1 = teachers.get(0);
        Teacher t2 = teachers.get(1);
        return teachingClassRepository.saveAll(List.of(
                buildClass("软件 1 班", "2026", "软件工程实训演示班级", t1.getId()),
                buildClass("软件 2 班", "2026", "软件工程实训演示班级", t1.getId()),
                buildClass("计算机科学 1 班", "2026", "计算机科学实训演示班级", t1.getId()),
                buildClass("计算机科学 2 班", "2026", "计算机科学实训演示班级", t1.getId()),
                buildClass("软件 3 班", "2026", "软件工程实训演示班级", t2.getId()),
                buildClass("大数据 1 班", "2026", "大数据技术实训演示班级", t2.getId())
        ));
    }

    private List<Student> seedStudents(List<TeachingClass> classes) {
        return studentRepository.saveAll(List.of(
                buildStudent("2026001", "张三", classes.get(0)),
                buildStudent("2026002", "李四", classes.get(0)),
                buildStudent("2026003", "王小明", classes.get(0)),
                buildStudent("2026004", "王五", classes.get(1)),
                buildStudent("2026005", "赵六", classes.get(1)),
                buildStudent("2026006", "陈小红", classes.get(1)),
                buildStudent("2026007", "孙七", classes.get(2)),
                buildStudent("2026008", "杨洋", classes.get(2)),
                buildStudent("2026009", "刘大伟", classes.get(2)),
                buildStudent("2026010", "周八", classes.get(3)),
                buildStudent("2026011", "黄丽", classes.get(3)),
                buildStudent("2026012", "马超", classes.get(3)),
                buildStudent("2026013", "吴九", classes.get(4)),
                buildStudent("2026014", "郑十", classes.get(4)),
                buildStudent("2026015", "林小芳", classes.get(4)),
                buildStudent("2026016", "钱十一", classes.get(5)),
                buildStudent("2026017", "朱十二", classes.get(5)),
                buildStudent("2026018", "徐明", classes.get(5))
        ));
    }

    private List<Assignment> seedAssignments(List<TeachingClass> classes) {
        TeachingClass c1 = classes.get(0); // 软件 1 班
        TeachingClass c2 = classes.get(1); // 软件 2 班
        TeachingClass c3 = classes.get(2); // 计算机科学 1 班
        TeachingClass c4 = classes.get(3); // 计算机科学 2 班
        TeachingClass c5 = classes.get(4); // 软件 3 班
        TeachingClass c6 = classes.get(5); // 大数据 1 班

        Assignment a1 = buildAssignment("第二阶段实训报告", "课程论文", c1, "提交阶段报告、源码和运行截图");
        Assignment a2 = buildAssignment("算法设计与分析", "代码作业", c1, "提交算法实现与测试说明");
        Assignment a3 = buildAssignment("数据结构课程设计", "代码作业", c2, "提交课程设计源码和说明");
        Assignment a4 = buildAssignment("数据库课程设计", "课程论文", c4, "提交 ER 图、SQL 脚本和设计说明");
        Assignment a5 = buildAssignment("操作系统实验报告", "实验报告", c5, "提交实验记录和调度算法分析");
        Assignment a6 = buildAssignment("软件工程课程论文", "课程论文", c5, "提交课程论文和项目总结");
        Assignment a7 = buildAssignment("机器学习项目", "代码作业", c3, "提交模型代码、训练日志与评估报告");
        Assignment a8 = buildAssignment("大数据分析报告", "实验报告", c6, "提交数据清洗流程、分析代码与可视化结果");
        Assignment a9 = buildAssignment("Web 前端开发实战", "代码作业", c2, "提交 HTML/CSS/JS 前端项目源码与设计文档");

        List<Assignment> assignments = assignmentRepository.saveAll(
                List.of(a1, a2, a3, a4, a5, a6, a7, a8, a9)
        );

        assignmentClassRepository.saveAll(List.of(
                buildAssignmentClass(a1, c1.getId(), c1.getName()),
                buildAssignmentClass(a2, c1.getId(), c1.getName()),
                buildAssignmentClass(a3, c2.getId(), c2.getName()),
                buildAssignmentClass(a4, c4.getId(), c4.getName()),
                buildAssignmentClass(a5, c5.getId(), c5.getName()),
                buildAssignmentClass(a6, c5.getId(), c5.getName()),
                buildAssignmentClass(a7, c3.getId(), c3.getName()),
                buildAssignmentClass(a8, c6.getId(), c6.getName()),
                buildAssignmentClass(a9, c2.getId(), c2.getName()),
                buildAssignmentClass(a9, c3.getId(), c3.getName())
        ));
        return assignments;
    }

    private List<WorkSubmission> seedSubmissions(List<Student> students, List<Assignment> assignments) {
        // Build convenience: S0=张三, S1=李四, S2=王小明, S3=王五, S4=赵六, S5=陈小红
        // S6=孙七, S7=杨洋, S8=刘大伟, S9=周八, S10=黄丽, S11=马超
        // S12=吴九, S13=郑十, S14=林小芳, S15=钱十一, S16=朱十二, S17=徐明
        var s = students;
        var a = assignments; // a0=第二阶段实训报告, a1=算法设计, a2=数据结构, a3=数据库, a4=操作系统
                             // a5=软件工程, a6=机器学习, a7=大数据分析, a8=Web前端

        WorkSubmission[] subs = {
                sub(s.get(0), a.get(0), "第二阶段实训报告", "project-report.txt", "课程论文",
                        "包含源码和报告"),
                sub(s.get(1), a.get(1), "算法设计与分析", "binary-search.cpp", "代码作业",
                        "二分查找算法实现与测试"),
                sub(s.get(2), a.get(0), "第二阶段实训报告", "project-report.txt", "课程论文",
                        "实训项目总结与源码"),
                sub(s.get(3), a.get(2), "数据结构课程设计", "binary-search.cpp", "代码作业",
                        "二叉搜索树与平衡树对比实现"),
                sub(s.get(4), a.get(3), "数据库课程设计", "database-design.txt", "课程论文",
                        ""),
                sub(s.get(5), a.get(8), "Web 前端开发实战", "database-design.txt", "代码作业",
                        "在线商城前端页面设计与实现"),
                sub(s.get(6), a.get(6), "机器学习项目", "binary-search.cpp", "代码作业",
                        "手写数字识别 CNN 模型"),
                sub(s.get(7), a.get(7), "大数据分析报告", "os-experiment.txt", "实验报告",
                        "电商用户行为数据分析"),
                sub(s.get(8), a.get(6), "机器学习项目", "database-design.txt", "代码作业",
                        "自然语言处理情感分析模型"),
                sub(s.get(9), a.get(3), "数据库课程设计", "database-design.txt", "课程论文",
                        "学生选课管理系统 ER 图与 SQL 脚本"),
                sub(s.get(10), a.get(4), "操作系统实验报告", "os-experiment.txt", "实验报告",
                        ""),
                sub(s.get(11), a.get(5), "软件工程课程论文", "project-report.txt", "课程论文",
                        "敏捷开发实践与项目管理总结"),
                sub(s.get(12), a.get(4), "操作系统实验报告", "os-experiment.txt", "实验报告",
                        "进程调度算法对比分析"),
                sub(s.get(13), a.get(5), "软件工程课程论文", "project-report.txt", "课程论文",
                        "在线教学评价系统开发总结"),
                sub(s.get(14), a.get(7), "大数据分析报告", "database-design.txt", "实验报告",
                        "社交网络用户画像分析"),
                sub(s.get(15), a.get(7), "大数据分析报告", "project-report.txt", "实验报告",
                        "实时流数据处理管道设计"),
                sub(s.get(16), a.get(8), "Web 前端开发实战", "binary-search.cpp", "代码作业",
                        "在线答题系统前端实现"),
                sub(s.get(17), a.get(6), "机器学习项目", "database-design.txt", "代码作业",
                        "图像分类迁移学习实验"),
                // ── 多样文件类型演示数据 ──
                sub(s.get(0), a.get(3), "销售数据分析报告", "sales-report.xlsx", "电子表格",
                        "Q1 季度销售数据汇总与分析"),
                sub(s.get(5), a.get(8), "App 界面设计稿", "ui-mockup.png", "设计图",
                        "移动端主页面高保真原型"),
                sub(s.get(8), a.get(5), "项目答辩演示文稿", "project-defense.pptx", "演示文稿",
                        "期末项目成果展示与答辩 PPT"),
                sub(s.get(10), a.get(6), "项目演示视频", "demo-video.mp4", "视频",
                        "系统功能演示与操作录屏"),
                sub(s.get(4), a.get(5), "毕业论文初稿", "thesis-draft.docx", "文档",
                        "在线教育平台的设计与实现"),
        };

        List<WorkSubmission> saved = submissionRepository.saveAll(List.of(subs));
        // Vary submittedAt across the past two weeks for realistic trend data
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < saved.size(); i++) {
            WorkSubmission sub = saved.get(i);
            sub.setSubmittedAt(now.minusDays(14 - i).minusHours(i * 3L).minusMinutes(i * 17L));
        }
        return submissionRepository.saveAll(saved);
    }

    private WorkSubmission sub(Student student, Assignment assignment,
                                String title, String fileName, String workType, String remark) {
        WorkSubmission s = new WorkSubmission();
        s.setStudentId(student.getId());
        s.setStudentName(student.getName());
        applyAssignment(s, assignment);
        s.setTitle(title);
        s.setFileName(fileName);
        s.setWorkType(workType);
        s.setRemark(remark);
        return s;
    }

    private void seedEvaluations(List<WorkSubmission> submissions) {
        var s = submissions;

        EvaluationResult ai1 = new EvaluationResult();
        ai1.setSubmissionId(s.get(8).getId());
        ai1.setAiScore(new BigDecimal("85.50"));
        ai1.setAiIssues("1. ER 图中部分关系未标注基数\n2. 缺少索引优化说明");
        ai1.setAiComment("数据库设计整体规范，ER 图表达较完整，但在关系标注和性能优化方面还有提升空间。");
        ai1.setDimensionScores("[{\"name\":\"代码质量\",\"score\":88,\"comment\":\"SQL 语句规范\"},{\"name\":\"功能完整性\",\"score\":83,\"comment\":\"ER 图基本完整\"},{\"name\":\"文档与说明\",\"score\":86,\"comment\":\"说明清晰\"},{\"name\":\"创新与优化\",\"score\":85,\"comment\":\"有一定优化思考\"}]");
        ai1.setStatus(EvaluationResult.STATUS_AI_REVIEWED);

        EvaluationResult ai2 = new EvaluationResult();
        ai2.setSubmissionId(s.get(12).getId());
        ai2.setAiScore(new BigDecimal("78.00"));
        ai2.setAiIssues("1. 进程调度算法对比不够深入\n2. 缺少死锁预防方案的讨论");
        ai2.setAiComment("实验报告内容较全面，但算法对比分析和异常场景讨论方面有待加强。");
        ai2.setDimensionScores("[{\"name\":\"内容完整性\",\"score\":80,\"comment\":\"覆盖主要实验内容\"},{\"name\":\"逻辑与结构\",\"score\":75,\"comment\":\"结构可优化\"},{\"name\":\"格式规范\",\"score\":78,\"comment\":\"格式基本规范\"},{\"name\":\"表达与创新\",\"score\":79,\"comment\":\"分析略浅\"}]");
        ai2.setStatus(EvaluationResult.STATUS_AI_REVIEWED);

        EvaluationResult ai3 = new EvaluationResult();
        ai3.setSubmissionId(s.get(6).getId());
        ai3.setAiScore(new BigDecimal("88.00"));
        ai3.setAiIssues("1. 模型准确率未达最优\n2. 缺少数据增强策略说明");
        ai3.setAiComment("CNN 模型实现正确，训练流程完整，建议增加数据增强和超参数调优内容。");
        ai3.setDimensionScores("[{\"name\":\"代码质量\",\"score\":90,\"comment\":\"代码结构清晰\"},{\"name\":\"模型设计\",\"score\":86,\"comment\":\"架构合理\"},{\"name\":\"实验分析\",\"score\":84,\"comment\":\"分析较全面\"},{\"name\":\"文档规范\",\"score\":92,\"comment\":\"文档详实\"}]");
        ai3.setStatus(EvaluationResult.STATUS_AI_REVIEWED);

        EvaluationResult tc1 = new EvaluationResult();
        tc1.setSubmissionId(s.get(13).getId());
        tc1.setAiScore(new BigDecimal("90.00"));
        tc1.setAiIssues("1. 缺少具体的团队实践数据支撑");
        tc1.setAiComment("选题切合实际，论文结构清晰，建议补充更多实际项目数据来增强说服力。");
        tc1.setDimensionScores("[{\"name\":\"内容完整性\",\"score\":92,\"comment\":\"内容充实\"},{\"name\":\"逻辑与结构\",\"score\":90,\"comment\":\"逻辑清晰\"},{\"name\":\"格式规范\",\"score\":89,\"comment\":\"格式规范\"},{\"name\":\"表达与创新\",\"score\":88,\"comment\":\"有一定独立见解\"}]");
        tc1.setTeacherScore(new BigDecimal("92.00"));
        tc1.setTeacherComment("整体完成较好，论述逻辑清晰，建议后续补充量化数据以增强论文说服力。");
        tc1.setStatus(EvaluationResult.STATUS_TEACHER_CONFIRMED);

        EvaluationResult tc2 = new EvaluationResult();
        tc2.setSubmissionId(s.get(7).getId());
        tc2.setAiScore(new BigDecimal("82.00"));
        tc2.setAiIssues("1. 数据清洗步骤可进一步完善\n2. 可视化图表类型选择可优化");
        tc2.setAiComment("数据分析流程完整，方法选用合理，但在数据预处理和可视化表达上有提升空间。");
        tc2.setDimensionScores("[{\"name\":\"数据质量\",\"score\":80,\"comment\":\"数据基本清洗\"},{\"name\":\"分析方法\",\"score\":84,\"comment\":\"方法合理\"},{\"name\":\"可视化效果\",\"score\":78,\"comment\":\"可优化\"},{\"name\":\"报告撰写\",\"score\":86,\"comment\":\"结构清晰\"}]");
        tc2.setTeacherScore(new BigDecimal("84.00"));
        tc2.setTeacherComment("分析思路清晰，数据处理基本到位，建议完善数据清洗环节并丰富可视化呈现。");
        tc2.setStatus(EvaluationResult.STATUS_TEACHER_CONFIRMED);

        List<EvaluationResult> savedEvals = evaluationRepository.saveAll(List.of(ai1, ai2, ai3, tc1, tc2));
        // Vary evaluation timestamps to produce meaningful trend/efficiency data.
        // Confirmed evaluations get updatedAt set a few days after the submission.
        for (EvaluationResult ev : savedEvals) {
            WorkSubmission sub = submissions.stream()
                    .filter(x -> x.getId().equals(ev.getSubmissionId()))
                    .findFirst().orElse(null);
            if (sub == null || sub.getSubmittedAt() == null) continue;
            LocalDateTime subDate = sub.getSubmittedAt();
            ev.setCreatedAt(subDate.plusHours(2));
            if (ev.getStatus() >= EvaluationResult.STATUS_TEACHER_CONFIRMED) {
                ev.setUpdatedAt(subDate.plusDays(1 + ev.getId()).plusHours(5));
            } else {
                ev.setUpdatedAt(subDate.plusHours(2));
            }
        }
        evaluationRepository.saveAll(savedEvals);
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

    /** 演示多文件上传：给 thesis-draft.docx 的提交追加一个次要文件 */
    private void attachSecondaryFiles(List<WorkSubmission> submissions) {
        for (WorkSubmission s : submissions) {
            if (!"thesis-draft.docx".equals(s.getFileName())) continue;
            String secondaryName = "project-report.txt";
            try {
                var resource = new ClassPathResource("sample-files/" + secondaryName);
                if (!resource.exists()) return;
                byte[] bytes = resource.getInputStream().readAllBytes();

                Path submissionDir = uploadRoot.resolve("submissions").resolve(String.valueOf(s.getId()));
                Path dest = submissionDir.resolve(secondaryName);
                Files.write(dest, bytes);

                SubmissionFile secondary = new SubmissionFile();
                secondary.setSubmissionId(s.getId());
                secondary.setFileName(secondaryName);
                secondary.setFilePath(toResponsePath(dest));
                secondary.setFileSize((long) bytes.length);
                secondary.setContentType(resolveContentType(secondaryName));
                secondary.setFileRole("SECONDARY");
                secondary.setPrimaryFile(false);
                secondary.setSortOrder(1);
                submissionFileRepository.save(secondary);
            } catch (IOException e) {
                // skip
            }
            break;
        }
    }

    private static Teacher buildTeacher(String username, String password, String displayName) {
        Teacher teacher = new Teacher();
        teacher.setUsername(username);
        teacher.setPassword(password);
        teacher.setDisplayName(displayName);
        return teacher;
    }

    private static TeachingClass buildClass(String name, String grade, String description, Long teacherId) {
        TeachingClass teachingClass = new TeachingClass();
        teachingClass.setName(name);
        teachingClass.setGrade(grade);
        teachingClass.setDescription(description);
        teachingClass.setTeacherId(teacherId);
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
