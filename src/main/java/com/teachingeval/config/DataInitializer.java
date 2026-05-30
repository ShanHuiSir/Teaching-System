package com.teachingeval.config;

import com.teachingeval.entity.EvaluationResult;
import com.teachingeval.entity.Student;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.repository.EvaluationRepository;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final SubmissionRepository submissionRepository;
    private final EvaluationRepository evaluationRepository;

    public DataInitializer(StudentRepository studentRepository,
                           SubmissionRepository submissionRepository,
                           EvaluationRepository evaluationRepository) {
        this.studentRepository = studentRepository;
        this.submissionRepository = submissionRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Override
    public void run(String... args) {
        seedIfEmpty();
    }

    public void seedIfEmpty() {
        if (studentRepository.count() == 0
                && submissionRepository.count() == 0
                && evaluationRepository.count() == 0) {
            resetDemoData();
        }
    }

    public void resetDemoData() {
        evaluationRepository.deleteAll();
        submissionRepository.deleteAll();
        studentRepository.deleteAll();

        List<Student> students = seedStudents();
        List<WorkSubmission> submissions = seedSubmissions(students);
        seedEvaluations(submissions);
    }

    private List<Student> seedStudents() {
        return studentRepository.saveAll(List.of(
                buildStudent("2026001", "张三", "软件 1 班"),
                buildStudent("2026002", "李四", "软件 1 班"),
                buildStudent("2026003", "王五", "软件 2 班"),
                buildStudent("2026004", "赵六", "软件 2 班"),
                buildStudent("2026005", "孙七", "计算机科学 1 班"),
                buildStudent("2026006", "周八", "计算机科学 2 班"),
                buildStudent("2026007", "吴九", "软件 3 班"),
                buildStudent("2026008", "郑十", "软件 3 班")
        ));
    }

    private List<WorkSubmission> seedSubmissions(List<Student> students) {
        if (students.size() < 8) {
            throw new IllegalStateException("演示学生数据不足，无法初始化作业数据");
        }

        WorkSubmission s1 = new WorkSubmission();
        s1.setStudentId(students.get(0).getId());
        s1.setStudentName(students.get(0).getName());
        s1.setTitle("第二阶段实训报告");
        s1.setFileName("student-work.zip");
        s1.setWorkType("代码压缩包");
        s1.setRemark("包含源码和报告");

        WorkSubmission s2 = new WorkSubmission();
        s2.setStudentId(students.get(1).getId());
        s2.setStudentName(students.get(1).getName());
        s2.setTitle("算法设计与分析");
        s2.setFileName("algorithm-lab.docx");
        s2.setWorkType("实验报告");
        s2.setRemark("包含算法实现与测试数据");

        WorkSubmission s3 = new WorkSubmission();
        s3.setStudentId(students.get(2).getId());
        s3.setStudentName(students.get(2).getName());
        s3.setTitle("数据结构课程设计");
        s3.setFileName("data-structure.zip");
        s3.setWorkType("代码压缩包");
        s3.setRemark("");

        WorkSubmission s4 = new WorkSubmission();
        s4.setStudentId(students.get(5).getId());
        s4.setStudentName(students.get(5).getName());
        s4.setTitle("数据库课程设计");
        s4.setFileName("database-project.zip");
        s4.setWorkType("代码压缩包");
        s4.setRemark("包含 ER 图和 SQL 脚本");

        WorkSubmission s5 = new WorkSubmission();
        s5.setStudentId(students.get(6).getId());
        s5.setStudentName(students.get(6).getName());
        s5.setTitle("操作系统实验报告");
        s5.setFileName("os-lab.docx");
        s5.setWorkType("实验报告");
        s5.setRemark("包含进程调度与内存管理实验");

        WorkSubmission s6 = new WorkSubmission();
        s6.setStudentId(students.get(7).getId());
        s6.setStudentName(students.get(7).getName());
        s6.setTitle("软件工程课程论文");
        s6.setFileName("se-paper.pdf");
        s6.setWorkType("课程论文");
        s6.setRemark("敏捷开发在小型团队中的应用");

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
        ai1.setStatus(EvaluationResult.STATUS_AI_REVIEWED);

        EvaluationResult ai2 = new EvaluationResult();
        ai2.setSubmissionId(submissions.get(4).getId());
        ai2.setAiScore(new BigDecimal("78.00"));
        ai2.setAiIssues("1. 进程调度算法对比不够深入\n2. 缺少死锁预防方案的讨论");
        ai2.setAiComment("实验报告内容较全面，但算法对比分析和异常场景讨论方面有待加强。");
        ai2.setStatus(EvaluationResult.STATUS_AI_REVIEWED);

        EvaluationResult tc = new EvaluationResult();
        tc.setSubmissionId(submissions.get(5).getId());
        tc.setAiScore(new BigDecimal("90.00"));
        tc.setAiIssues("1. 缺少具体的团队实践数据支撑");
        tc.setAiComment("选题切合实际，论文结构清晰，建议补充更多实际项目数据来增强说服力。");
        tc.setTeacherScore(new BigDecimal("92.00"));
        tc.setTeacherComment("整体完成较好，论述逻辑清晰，建议后续补充量化数据以增强论文说服力。");
        tc.setStatus(EvaluationResult.STATUS_TEACHER_CONFIRMED);

        evaluationRepository.save(ai1);
        evaluationRepository.save(ai2);
        evaluationRepository.save(tc);
    }

    private static Student buildStudent(String studentNo, String name, String className) {
        Student s = new Student();
        s.setStudentNo(studentNo);
        s.setName(name);
        s.setClassName(className);
        return s;
    }
}
