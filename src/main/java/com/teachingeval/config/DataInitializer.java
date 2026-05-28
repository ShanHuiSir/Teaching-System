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
        if (studentRepository.count() == 0) {
            seedStudents();
            seedSubmissions();
        }
        if (evaluationRepository.count() == 0) {
            seedEvaluations();
        }
    }

    public void resetDemoData() {
        evaluationRepository.deleteAll();
        submissionRepository.deleteAll();
        studentRepository.deleteAll();

        seedStudents();
        seedSubmissions();
        seedEvaluations();
    }

    private void seedStudents() {
        studentRepository.save(buildStudent("2026001", "张三", "软件 1 班"));
        studentRepository.save(buildStudent("2026002", "李四", "软件 1 班"));
        studentRepository.save(buildStudent("2026003", "王五", "软件 2 班"));
        studentRepository.save(buildStudent("2026004", "赵六", "软件 2 班"));
        studentRepository.save(buildStudent("2026005", "孙七", "计算机科学 1 班"));
        studentRepository.save(buildStudent("2026006", "周八", "计算机科学 2 班"));
        studentRepository.save(buildStudent("2026007", "吴九", "软件 3 班"));
        studentRepository.save(buildStudent("2026008", "郑十", "软件 3 班"));
    }

    private void seedSubmissions() {
        WorkSubmission s1 = new WorkSubmission();
        s1.setStudentId(1L);
        s1.setStudentName("张三");
        s1.setTitle("第二阶段实训报告");
        s1.setFileName("student-work.zip");
        s1.setWorkType("代码压缩包");
        s1.setRemark("包含源码和报告");

        WorkSubmission s2 = new WorkSubmission();
        s2.setStudentId(2L);
        s2.setStudentName("李四");
        s2.setTitle("算法设计与分析");
        s2.setFileName("algorithm-lab.docx");
        s2.setWorkType("实验报告");
        s2.setRemark("包含算法实现与测试数据");

        WorkSubmission s3 = new WorkSubmission();
        s3.setStudentId(3L);
        s3.setStudentName("王五");
        s3.setTitle("数据结构课程设计");
        s3.setFileName("data-structure.zip");
        s3.setWorkType("代码压缩包");
        s3.setRemark("");

        submissionRepository.save(s1);
        submissionRepository.save(s2);
        submissionRepository.save(s3);

        WorkSubmission s4 = new WorkSubmission();
        s4.setStudentId(6L);
        s4.setStudentName("周八");
        s4.setTitle("数据库课程设计");
        s4.setFileName("database-project.zip");
        s4.setWorkType("代码压缩包");
        s4.setRemark("包含 ER 图和 SQL 脚本");

        WorkSubmission s5 = new WorkSubmission();
        s5.setStudentId(7L);
        s5.setStudentName("吴九");
        s5.setTitle("操作系统实验报告");
        s5.setFileName("os-lab.docx");
        s5.setWorkType("实验报告");
        s5.setRemark("包含进程调度与内存管理实验");

        WorkSubmission s6 = new WorkSubmission();
        s6.setStudentId(8L);
        s6.setStudentName("郑十");
        s6.setTitle("软件工程课程论文");
        s6.setFileName("se-paper.pdf");
        s6.setWorkType("课程论文");
        s6.setRemark("敏捷开发在小型团队中的应用");

        submissionRepository.save(s4);
        submissionRepository.save(s5);
        submissionRepository.save(s6);
    }

    private void seedEvaluations() {
        EvaluationResult ai1 = new EvaluationResult();
        ai1.setSubmissionId(4L);
        ai1.setAiScore(new BigDecimal("85.50"));
        ai1.setAiIssues("1. ER 图中部分关系未标注基数\n2. 缺少索引优化说明");
        ai1.setAiComment("数据库设计整体规范，ER 图表达较完整，但在关系标注和性能优化方面还有提升空间。");
        ai1.setStatus(EvaluationResult.STATUS_AI_REVIEWED);

        EvaluationResult ai2 = new EvaluationResult();
        ai2.setSubmissionId(5L);
        ai2.setAiScore(new BigDecimal("78.00"));
        ai2.setAiIssues("1. 进程调度算法对比不够深入\n2. 缺少死锁预防方案的讨论");
        ai2.setAiComment("实验报告内容较全面，但算法对比分析和异常场景讨论方面有待加强。");
        ai2.setStatus(EvaluationResult.STATUS_AI_REVIEWED);

        EvaluationResult tc = new EvaluationResult();
        tc.setSubmissionId(6L);
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
