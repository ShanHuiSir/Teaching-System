package com.teachingeval.config;

import com.teachingeval.entity.Student;
import com.teachingeval.entity.WorkSubmission;
import com.teachingeval.repository.StudentRepository;
import com.teachingeval.repository.SubmissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final SubmissionRepository submissionRepository;

    public DataInitializer(StudentRepository studentRepository,
                           SubmissionRepository submissionRepository) {
        this.studentRepository = studentRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    public void run(String... args) {
        if (studentRepository.count() > 0) {
            return;
        }
        seedStudents();
        seedSubmissions();
    }

    private void seedStudents() {
        studentRepository.save(buildStudent("2026001", "张三", "软件 1 班"));
        studentRepository.save(buildStudent("2026002", "李四", "软件 1 班"));
        studentRepository.save(buildStudent("2026003", "王五", "软件 2 班"));
        studentRepository.save(buildStudent("2026004", "赵六", "软件 2 班"));
        studentRepository.save(buildStudent("2026005", "孙七", "计算机科学 1 班"));
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
    }

    private static Student buildStudent(String studentNo, String name, String className) {
        Student s = new Student();
        s.setStudentNo(studentNo);
        s.setName(name);
        s.setClassName(className);
        return s;
    }
}
