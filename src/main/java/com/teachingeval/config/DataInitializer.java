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
import java.util.Map;
import java.util.LinkedHashMap;

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
    public void run(String... args) { seedIfEmpty(); }

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

    /* ═══════════════════════════════════════════════════════════════════
     * Teachers
     * ═══════════════════════════════════════════════════════════════════ */

    private List<Teacher> seedTeachers() {
        return teacherRepository.saveAll(List.of(
                buildTeacher("teacher", PASSWORD_ENCODER.encode("123456"), "张老师"),
                buildTeacher("temp", PASSWORD_ENCODER.encode("123456"), "李老师")
        ));
    }

    /* ═══════════════════════════════════════════════════════════════════
     * Classes — 4 classes
     * ═══════════════════════════════════════════════════════════════════ */

    private List<TeachingClass> seedClasses(List<Teacher> teachers) {
        Teacher t1 = teachers.get(0);
        return teachingClassRepository.saveAll(List.of(
                buildClass("软件 1 班", "2026", "软件工程实训演示班级", t1.getId()),
                buildClass("软件 2 班", "2026", "软件工程实训演示班级", t1.getId()),
                buildClass("计算机科学 1 班", "2026", "计算机科学实训演示班级", t1.getId()),
                buildClass("大数据 1 班", "2026", "大数据技术实训演示班级", t1.getId())
        ));
    }

    /* ═══════════════════════════════════════════════════════════════════
     * Students — ~10 per class (total ~43)
     * ═══════════════════════════════════════════════════════════════════ */

    private List<Student> seedStudents(List<TeachingClass> classes) {
        return studentRepository.saveAll(List.of(
                // ── 软件 1 班 (10人) ──
                buildStudent("2026001", "张三",     classes.get(0)),
                buildStudent("2026002", "李四",     classes.get(0)),
                buildStudent("2026003", "王小明",   classes.get(0)),
                buildStudent("2026004", "陈小红",   classes.get(0)),
                buildStudent("2026005", "刘大伟",   classes.get(0)),
                buildStudent("2026006", "周琳",     classes.get(0)),
                buildStudent("2026007", "吴强",     classes.get(0)),
                buildStudent("2026008", "郑秀英",   classes.get(0)),
                buildStudent("2026009", "钱一波",   classes.get(0)),
                buildStudent("2026010", "朱明辉",   classes.get(0)),
                buildStudent("2026011", "徐欣然",   classes.get(0)),
                // ── 软件 2 班 (11人) ──
                buildStudent("2026012", "王五",     classes.get(1)),
                buildStudent("2026013", "赵六",     classes.get(1)),
                buildStudent("2026014", "孙七",     classes.get(1)),
                buildStudent("2026015", "杨洋",     classes.get(1)),
                buildStudent("2026016", "黄丽",     classes.get(1)),
                buildStudent("2026017", "马超",     classes.get(1)),
                buildStudent("2026018", "林小芳",   classes.get(1)),
                buildStudent("2026019", "何志远",   classes.get(1)),
                buildStudent("2026020", "罗海燕",   classes.get(1)),
                buildStudent("2026021", "谢天宇",   classes.get(1)),
                buildStudent("2026022", "邓佳琪",   classes.get(1)),
                // ── 计算机科学 1 班 (11人) ──
                buildStudent("2026023", "吴九",     classes.get(2)),
                buildStudent("2026024", "郑十",     classes.get(2)),
                buildStudent("2026025", "钱十一",   classes.get(2)),
                buildStudent("2026026", "朱十二",   classes.get(2)),
                buildStudent("2026027", "徐明",     classes.get(2)),
                buildStudent("2026028", "沈思远",   classes.get(2)),
                buildStudent("2026029", "唐慧敏",   classes.get(2)),
                buildStudent("2026030", "韩飞",     classes.get(2)),
                buildStudent("2026031", "冯静雅",   classes.get(2)),
                buildStudent("2026032", "曾伟豪",   classes.get(2)),
                buildStudent("2026033", "宋晓雯",   classes.get(2)),
                // ── 大数据 1 班 (10人) ──
                buildStudent("2026034", "周八",     classes.get(3)),
                buildStudent("2026035", "黄丽丽",   classes.get(3)),
                buildStudent("2026036", "马文博",   classes.get(3)),
                buildStudent("2026037", "陈思琪",   classes.get(3)),
                buildStudent("2026038", "李浩然",   classes.get(3)),
                buildStudent("2026039", "张雨桐",   classes.get(3)),
                buildStudent("2026040", "王子轩",   classes.get(3)),
                buildStudent("2026041", "赵梦洁",   classes.get(3)),
                buildStudent("2026042", "刘子涵",   classes.get(3)),
                buildStudent("2026043", "杨云飞",   classes.get(3))
        ));
    }

    /* ═══════════════════════════════════════════════════════════════════
     * Assignments — 6 assignments spread across classes
     * ═══════════════════════════════════════════════════════════════════ */

    private List<Assignment> seedAssignments(List<TeachingClass> classes) {
        TeachingClass c1 = classes.get(0); // 软件 1 班
        TeachingClass c2 = classes.get(1); // 软件 2 班
        TeachingClass c3 = classes.get(2); // 计算机科学 1 班
        TeachingClass c4 = classes.get(3); // 大数据 1 班

        Assignment a1 = buildAssignment("第二阶段实训报告", "课程论文", "提交阶段报告、源码和运行截图");
        Assignment a2 = buildAssignment("算法设计与分析", "代码作业", "提交算法实现与测试说明");
        Assignment a3 = buildAssignment("Web 前端开发实战", "代码作业", "提交 HTML/CSS/JS 前端项目源码与设计文档");
        Assignment a4 = buildAssignment("数据库课程设计", "课程论文", "提交 ER 图、SQL 脚本和设计说明");
        Assignment a5 = buildAssignment("机器学习项目", "代码作业", "提交模型代码、训练日志与评估报告");
        Assignment a6 = buildAssignment("操作系统实验报告", "实验报告", "提交实验记录和调度算法分析");

        List<Assignment> saved = assignmentRepository.saveAll(List.of(a1, a2, a3, a4, a5, a6));

        // Assignments → Classes mapping
        Map<Assignment, List<TeachingClass>> map = new LinkedHashMap<>();
        map.put(a1, List.of(c1, c2));                // 实训报告 → 软件1, 软件2
        map.put(a2, List.of(c1, c3));                // 算法设计 → 软件1, 计科1
        map.put(a3, List.of(c2, c3));                // Web前端 → 软件2, 计科1
        map.put(a4, List.of(c3, c4));                // 数据库 → 计科1, 大数据1
        map.put(a5, List.of(c1, c4));                // 机器学习 → 软件1, 大数据1
        map.put(a6, List.of(c2, c4));                // 操作系统 → 软件2, 大数据1

        for (Map.Entry<Assignment, List<TeachingClass>> entry : map.entrySet()) {
            for (TeachingClass c : entry.getValue()) {
                assignmentClassRepository.save(buildAssignmentClass(entry.getKey(), c));
            }
        }

        return saved;
    }

    /* ═══════════════════════════════════════════════════════════════════
     * Submissions — ~70% coverage, diverse file types
     * ═══════════════════════════════════════════════════════════════════ */

    private List<WorkSubmission> seedSubmissions(List<Student> students, List<Assignment> assignments) {
        var s = students;
        var a = assignments;
        // a0=实训报告, a1=算法设计, a2=Web前端, a3=数据库, a4=机器学习, a5=操作系统

        // ── 软件 1 班 (s0-s10) → a0 实训报告, a1 算法, a4 机器学习 ──
        WorkSubmission[] subs = {
                sub(s.get(0),  a.get(0), "第二阶段实训报告", "project-report.md", "课程论文", "包含源码和报告"),
                sub(s.get(1),  a.get(0), "软件工程实训总结", "project-readme.md", "课程论文", "实训项目总结与经验分享"),
                sub(s.get(2),  a.get(0), "第二阶段实训报告", "project-report.md", "课程论文", "实训项目总结与源码"),
                sub(s.get(3),  a.get(0), "实训项目复盘", "thesis-draft.docx", "课程论文", "详细项目复盘与改进意见"),
                sub(s.get(4),  a.get(0), "第二阶段实训报告", "project-report.md", "课程论文", "完整实训报告"),
                sub(s.get(5),  a.get(0), "实训总结文档", "thesis-draft.docx", "课程论文", "含前端技术栈总结"),
                sub(s.get(7),  a.get(0), "实训心得", "project-readme.md", "课程论文", "个人心得体会"),
                sub(s.get(9),  a.get(0), "第二阶段实训报告", "project-report.md", "课程论文", "团队协作与项目管理"),

                sub(s.get(0),  a.get(1), "二分查找算法实现", "binary-search.cpp", "代码作业", "二分查找算法实现与测试"),
                sub(s.get(2),  a.get(1), "动态规划算法分析", "data-analysis.py", "代码作业", "DP 算法复杂度分析"),
                sub(s.get(5),  a.get(1), "排序算法对比", "binary-search.cpp", "代码作业", "归并排序 vs 快排对比"),
                sub(s.get(6),  a.get(1), "图论算法实现", "student-system.java", "代码作业", "Dijkstra 最短路径算法"),
                sub(s.get(8),  a.get(1), "动态规划应用", "binary-search.cpp", "代码作业", "背包问题求解"),
                sub(s.get(10), a.get(1), "算法可视化", "data-analysis.py", "代码作业", "用 Python 实现算法可视化"),

                sub(s.get(1),  a.get(4), "手写数字识别 CNN", "data-analysis.py", "代码作业", "CNN 模型训练与评估"),
                sub(s.get(3),  a.get(4), "情感分析模型", "binary-search.cpp", "代码作业", "NLP 文本情感分类"),
                sub(s.get(4),  a.get(4), "图像分类迁移学习", "student-system.java", "代码作业", "ResNet 迁移识别"),
                sub(s.get(6),  a.get(4), "推荐系统实现", "data-analysis.py", "代码作业", "协同过滤推荐算法"),
                sub(s.get(8),  a.get(4), "强化学习五子棋", "binary-search.cpp", "代码作业", "Q-Learning 五子棋 AI"),
                sub(s.get(10), a.get(4), "LSTM 预测模型", "data-analysis.py", "代码作业", "时间序列预测"),

                // ── 软件 2 班 (s11-s22) → a0 实训报告, a2 Web前端, a5 操作系统 ──
                sub(s.get(11), a.get(0), "第二阶段实训报告", "project-report.md", "课程论文", "实训项目总结"),
                sub(s.get(13), a.get(0), "实训项目复盘", "thesis-draft.docx", "课程论文", "详细复盘"),
                sub(s.get(15), a.get(0), "第二阶段实训报告", "project-report.md", "课程论文", "完整报告"),

                sub(s.get(11), a.get(2), "在线商城前端", "todo-app.vue", "代码作业", "Vue3 商城首页与详情页"),
                sub(s.get(12), a.get(2), "个人主页设计", "web-homework.html", "代码作业", "个人主页 HTML+CSS"),
                sub(s.get(14), a.get(2), "在线答题系统", "todo-app.vue", "代码作业", "Vue3 答题组件实现"),
                sub(s.get(16), a.get(2), "博客前端", "web-homework.html", "代码作业", "响应式博客模板"),
                sub(s.get(17), a.get(2), "Todo App", "todo-app.vue", "代码作业", "Vue3 TodoMVC 实现"),
                sub(s.get(19), a.get(2), "个人主页设计", "web-homework.html", "代码作业", "社交主页"),
                sub(s.get(21), a.get(2), "在线商城前端", "todo-app.vue", "代码作业", "商品列表组件"),

                sub(s.get(12), a.get(5), "进程调度算法分析", "os-experiment.txt", "实验报告", "FCFS/SJF/RR 对比"),
                sub(s.get(14), a.get(5), "死锁预防实验", "os-experiment.txt", "实验报告", "银行家算法实现"),
                sub(s.get(16), a.get(5), "内存管理实验", "project-defense.pptx", "实验报告", "分页管理 PPT 汇报"),
                sub(s.get(18), a.get(5), "进程调度分析", "os-experiment.txt", "实验报告", "调度算法对比实验"),
                sub(s.get(20), a.get(5), "操作系统综合实验", "project-defense.pptx", "实验报告", "文件系统设计"),

                // ── 计科 1 班 (s22-s32) → a1 算法, a2 Web前端, a3 数据库 ──
                sub(s.get(22), a.get(1), "动态规划分析", "binary-search.cpp", "代码作业", "DP 优化解法"),
                sub(s.get(24), a.get(1), "图论算法集合", "student-system.java", "代码作业", "最短路径与 MST"),
                sub(s.get(26), a.get(1), "字符串匹配算法", "binary-search.cpp", "代码作业", "KMP 算法实现"),
                sub(s.get(28), a.get(1), "贪心算法集", "data-analysis.py", "代码作业", "活动选择与哈夫曼"),
                sub(s.get(30), a.get(1), "分治算法应用", "binary-search.cpp", "代码作业", "快速幂与大数乘法"),

                sub(s.get(23), a.get(2), "响应式博客", "web-homework.html", "代码作业", "移动端适配"),
                sub(s.get(25), a.get(2), "音乐播放器界面", "todo-app.vue", "代码作业", "Vue3 音乐组件"),
                sub(s.get(27), a.get(2), "公司官网首页", "web-homework.html", "代码作业", "企业站原型"),
                sub(s.get(29), a.get(2), "在线问卷系统", "todo-app.vue", "代码作业", "Vue3 表单组件"),

                sub(s.get(22), a.get(3), "学生选课系统 ER 图", "database-design.txt", "课程论文", "ER 图+SQL 建表"),
                sub(s.get(24), a.get(3), "电商数据库设计", "database-design.txt", "课程论文", "订单-商品-用户模型"),
                sub(s.get(26), a.get(3), "图书馆管理系统", "sales-report.xlsx", "课程论文", "借还书记录表设计"),
                sub(s.get(28), a.get(3), "社交媒体数据库", "database-design.txt", "课程论文", "用户关系图谱设计"),
                sub(s.get(30), a.get(3), "在线考试系统", "database-design.txt", "课程论文", "题库与试卷模型"),
                sub(s.get(31), a.get(3), "教务管理系统", "project-defense.pptx", "课程论文", "系统架构+数据库 PPT"),

                // ── 大数据 1 班 (s33-s42) → a3 数据库, a4 机器学习, a5 操作系统 ──
                sub(s.get(33), a.get(3), "医院管理系统 ER 图", "database-design.txt", "课程论文", "HIS 系统数据模型"),
                sub(s.get(35), a.get(3), "物流管理系统", "database-design.txt", "课程论文", "仓储物流数据模型"),
                sub(s.get(37), a.get(3), "教务管理数据库", "database-design.txt", "课程论文", "学生成绩与课程管理"),
                sub(s.get(39), a.get(3), "酒店预订系统", "sales-report.xlsx", "课程论文", "客房预定数据模型"),

                sub(s.get(33), a.get(4), "房价预测模型", "data-analysis.py", "代码作业", "线性回归预测模型"),
                sub(s.get(35), a.get(4), "客户分群分析", "data-analysis.py", "代码作业", "K-Means 聚类分析"),
                sub(s.get(37), a.get(4), "异常检测系统", "binary-search.cpp", "代码作业", "孤立森林异常检测"),
                sub(s.get(39), a.get(4), "推荐算法对比", "data-analysis.py", "代码作业", "多算法对比实验"),
                sub(s.get(41), a.get(4), "神经网络预测", "data-analysis.py", "代码作业", "MLP 时间序列预测"),
                sub(s.get(42), a.get(4), "自然语言分类", "student-system.java", "代码作业", "文本分类模型"),

                sub(s.get(34), a.get(5), "进程调度实验", "os-experiment.txt", "实验报告", "调度算法性能测试"),
                sub(s.get(36), a.get(5), "虚拟内存管理", "os-experiment.txt", "实验报告", "页面置换算法对比"),
                sub(s.get(38), a.get(5), "文件系统实现", "project-defense.pptx", "实验报告", "简易文件系统设计"),
                sub(s.get(40), a.get(5), "I/O 设备管理", "os-experiment.txt", "实验报告", "磁盘调度算法"),
                sub(s.get(42), a.get(5), "操作系统综合实验", "os-experiment.txt", "实验报告", "微内核仿真"),

                // ── 多媒体演示 ──
                sub(s.get(6),  a.get(2), "App 界面设计稿", "ui-mockup.png", "设计图", "移动端高保真原型"),
                sub(s.get(18), a.get(4), "项目演示视频", "demo-video.mp4", "视频", "系统功能演示录屏"),
                sub(s.get(32), a.get(5), "项目答辩演示文稿", "project-defense.pptx", "演示文稿", "期末答辩 PPT"),
                sub(s.get(41), a.get(3), "销售数据分析报告", "sales-report.xlsx", "电子表格", "季度销售数据汇总"),
                sub(s.get(7),  a.get(0), "毕业论文初稿", "thesis-draft.docx", "文档", "在线教育平台设计实现"),
                sub(s.get(17), a.get(5), "期末答辩文稿", "project-defense.pptx", "演示文稿", "课程项目展示"),
        };

        List<WorkSubmission> saved = submissionRepository.saveAll(List.of(subs));
        // Vary submittedAt across the past two weeks
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < saved.size(); i++) {
            saved.get(i).setSubmittedAt(now.minusDays(14 - (i % 14)).minusHours(i * 2L).minusMinutes(i * 13L));
        }
        return submissionRepository.saveAll(saved);
    }

    /* ═══════════════════════════════════════════════════════════════════
     * AI Evaluations — ~20 records spread across submissions
     * ═══════════════════════════════════════════════════════════════════ */

    private void seedEvaluations(List<WorkSubmission> submissions) {
        var s = submissions;

        Object[][] evals = {
                // { index, aiScore, issues, comment, dimScores JSON, hasTeacher, teacherScore, teacherComment }
                {0,  "85.50", "1. ER 图中部分关系未标注基数\n2. 缺少索引优化说明",
                        "数据库设计整体规范，建议在索引和关系标注方面加强。",
                        "[{\"name\":\"完成度\",\"score\":88,\"comment\":\"内容充实\"},{\"name\":\"正确性\",\"score\":83,\"comment\":\"基本正确\"},{\"name\":\"规范性\",\"score\":86,\"comment\":\"格式规范\"}]", true, "88.00", "整体完成较好，建议后续补充量化数据以增强论文说服力。"},
                {3,  "78.00", "1. 进程调度算法对比不够深入\n2. 缺少死锁预防方案的讨论",
                        "实验报告内容较全面，但算法对比分析和异常场景讨论方面有待加强。",
                        "[{\"name\":\"完成度\",\"score\":80,\"comment\":\"覆盖主要内容\"},{\"name\":\"正确性\",\"score\":75,\"comment\":\"结构可优化\"},{\"name\":\"规范性\",\"score\":78,\"comment\":\"格式基本规范\"}]", false, null, null},
                {8,  "88.00", "1. 模型准确率未达最优\n2. 缺少数据增强策略说明",
                        "CNN 模型实现正确，训练流程完整，建议增加数据增强和超参数调优内容。",
                        "[{\"name\":\"代码质量\",\"score\":90,\"comment\":\"代码结构清晰\"},{\"name\":\"功能完整性\",\"score\":86,\"comment\":\"架构合理\"},{\"name\":\"文档与说明\",\"score\":84,\"comment\":\"分析较全面\"},{\"name\":\"创新与优化\",\"score\":92,\"comment\":\"文档详实\"}]", false, null, null},
                {11, "90.00", "1. 缺少具体的团队实践数据支撑",
                        "选题切合实际，论文结构清晰，建议补充更多实际项目数据来增强说服力。",
                        "[{\"name\":\"内容完整性\",\"score\":92,\"comment\":\"内容充实\"},{\"name\":\"逻辑与结构\",\"score\":90,\"comment\":\"逻辑清晰\"},{\"name\":\"格式规范\",\"score\":89,\"comment\":\"格式规范\"},{\"name\":\"表达与创新\",\"score\":88,\"comment\":\"有一定独立见解\"}]", true, "92.00", "论述逻辑清晰，建议后续补充量化数据以增强论文说服力。"},
                {15, "82.00", "1. 数据清洗步骤可进一步完善\n2. 可视化图表类型选择可优化",
                        "数据分析流程完整，方法选用合理，但在数据预处理和可视化表达上有提升空间。",
                        "[{\"name\":\"完成度\",\"score\":80,\"comment\":\"数据基本清洗\"},{\"name\":\"正确性\",\"score\":84,\"comment\":\"方法合理\"},{\"name\":\"规范性\",\"score\":78,\"comment\":\"可优化\"}]", true, "84.00", "分析思路清晰，数据处理基本到位。"},
                {20, "76.00", "1. 代码注释不足\n2. 边界条件未覆盖",
                        "功能基本实现，但代码可读性和鲁棒性需要提升。",
                        "[{\"name\":\"代码质量\",\"score\":72,\"comment\":\"注释较少\"},{\"name\":\"功能完整性\",\"score\":78,\"comment\":\"基本功能完成\"},{\"name\":\"文档与说明\",\"score\":76,\"comment\":\"说朗简略\"},{\"name\":\"创新与优化\",\"score\":78,\"comment\":\"无显著优化\"}]", false, null, null},
                {25, "93.00", "1. 缺少 HMR 热更新配置\n2. 异步组件拆分可优化",
                        "Vue 组件设计优秀，响应式逻辑清晰，前端工程化意识良好。",
                        "[{\"name\":\"方案合理性\",\"score\":94,\"comment\":\"架构优秀\"},{\"name\":\"文档规范性\",\"score\":92,\"comment\":\"文档完整\"},{\"name\":\"技术深度\",\"score\":95,\"comment\":\"Vue3 理解深入\"},{\"name\":\"创新与可行\",\"score\":91,\"comment\":\"实用性强\"}]", true, "95.00", "前端能力突出，组件设计具有生产级质量。"},
                {32, "71.00", "1. 实验数据记录不完整\n2. 分析结论缺乏数据支撑",
                        "实验过程完整度不足，建议重新补测数据后完善报告。",
                        "[{\"name\":\"完成度\",\"score\":70,\"comment\":\"数据不全\"},{\"name\":\"正确性\",\"score\":72,\"comment\":\"基本正确\"},{\"name\":\"规范性\",\"score\":70,\"comment\":\"格式需改进\"}]", false, null, null},
                {40, "87.00", "1. SQL 查询可优化\n2. 索引设计需完善",
                        "ER 图设计规范，建表语句正确。查询优化和索引设计可以进一步提升。",
                        "[{\"name\":\"完成度\",\"score\":90,\"comment\":\"设计完整\"},{\"name\":\"正确性\",\"score\":85,\"comment\":\"SQL 基本正确\"},{\"name\":\"规范性\",\"score\":86,\"comment\":\"格式规范\"}]", false, null, null},
                {50, "80.00", "1. 前端适配不完善\n2. 缺少 Loading 状态处理",
                        "整体交互流畅，但细节状态处理和兼容性需要改进。",
                        "[{\"name\":\"方案合理性\",\"score\":82,\"comment\":\"方案合理\"},{\"name\":\"文档规范性\",\"score\":78,\"comment\":\"文档稍简\"},{\"name\":\"技术深度\",\"score\":80,\"comment\":\"技术到位\"},{\"name\":\"创新与可行\",\"score\":80,\"comment\":\"中规中矩\"}]", false, null, null},
                {55, "94.00", "1. 数据预处理流水线可优化\n2. 缺少实时推理方案",
                        "机器学习项目完成度极高，模型效果优异，报告撰写专业。",
                        "[{\"name\":\"代码质量\",\"score\":95,\"comment\":\"Python 代码优雅\"},{\"name\":\"功能完整性\",\"score\":94,\"comment\":\"功能完善\"},{\"name\":\"文档与说明\",\"score\":93,\"comment\":\"文档详细\"},{\"name\":\"创新与优化\",\"score\":94,\"comment\":\"创新突出\"}]", true, "96.00", "极其优秀的作业，模型效果好且文档详实。"},
                {60, "73.00", "1. 调度算法实现有误\n2. 结果未验证",
                        "基础框架正确，但调度算法实现需要修正，建议重新测试验证。",
                        "[{\"name\":\"完成度\",\"score\":70,\"comment\":\"部分完成\"},{\"name\":\"正确性\",\"score\":68,\"comment\":\"有错误\"},{\"name\":\"规范性\",\"score\":80,\"comment\":\"格式尚可\"}]", false, null, null},
        };

        List<EvaluationResult> savedEvals = new java.util.ArrayList<>();
        for (Object[] ev : evals) {
            int idx = (int) ev[0];
            if (idx >= submissions.size()) continue;
            WorkSubmission sub = submissions.get(idx);

            EvaluationResult e = new EvaluationResult();
            e.setSubmissionId(sub.getId());
            e.setAiScore(new BigDecimal((String) ev[1]));
            e.setAiIssues((String) ev[2]);
            e.setAiComment((String) ev[3]);
            e.setDimensionScores((String) ev[4]);
            e.setStatus(EvaluationResult.STATUS_AI_REVIEWED);

            if (Boolean.TRUE.equals(ev[5])) {
                e.setTeacherScore(new BigDecimal((String) ev[6]));
                e.setTeacherComment((String) ev[7]);
                e.setStatus(EvaluationResult.STATUS_TEACHER_CONFIRMED);
            }

            // Vary created/updated timestamps
            LocalDateTime subDate = sub.getSubmittedAt();
            if (subDate != null) {
                e.setCreatedAt(subDate.plusHours(2 + idx % 6));
                e.setUpdatedAt(e.getStatus() >= EvaluationResult.STATUS_TEACHER_CONFIRMED
                        ? subDate.plusDays(1 + idx % 5).plusHours(3)
                        : subDate.plusHours(2 + idx % 6));
            }
            savedEvals.add(e);
        }
        evaluationRepository.saveAll(savedEvals);
    }

    /* ═══════════════════════════════════════════════════════════════════
     * File helpers
     * ═══════════════════════════════════════════════════════════════════ */

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
                // seed file missing — submission stays without file
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
            case "vue" -> "text/x-vue";
            case "md" -> "text/markdown";
            case "xml" -> "application/xml";
            default -> "application/octet-stream";
        };
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

    /** Attach a secondary file to the first thesis-draft.docx submission */
    private void attachSecondaryFiles(List<WorkSubmission> submissions) {
        for (WorkSubmission s : submissions) {
            if (!"thesis-draft.docx".equals(s.getFileName())) continue;
            String secondaryName = "project-report.md";
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
            } catch (IOException e) { /* skip */ }
            break;
        }
    }

    /* ═══════════════════════════════════════════════════════════════════
     * Builders
     * ═══════════════════════════════════════════════════════════════════ */

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    private static Teacher buildTeacher(String username, String password, String displayName) {
        Teacher teacher = new Teacher();
        teacher.setUsername(username);
        teacher.setPassword(password);
        teacher.setDisplayName(displayName);
        return teacher;
    }

    private static TeachingClass buildClass(String name, String grade, String description, Long teacherId) {
        TeachingClass c = new TeachingClass();
        c.setName(name); c.setGrade(grade); c.setDescription(description); c.setTeacherId(teacherId);
        return c;
    }

    private static Student buildStudent(String studentNo, String name, TeachingClass c) {
        Student s = new Student();
        s.setStudentNo(studentNo); s.setName(name);
        s.setClassId(c.getId()); s.setClassName(c.getName());
        return s;
    }

    private static Assignment buildAssignment(String title, String workType, String description) {
        Assignment a = new Assignment();
        a.setTitle(title); a.setDescription(description); a.setWorkType(workType);
        return a;
    }

    private static AssignmentClass buildAssignmentClass(Assignment a, TeachingClass c) {
        AssignmentClass ac = new AssignmentClass();
        ac.setAssignmentId(a.getId()); ac.setClassId(c.getId()); ac.setClassName(c.getName());
        return ac;
    }

    private static WorkSubmission sub(Student student, Assignment assignment,
                                       String title, String fileName, String workType, String remark) {
        WorkSubmission s = new WorkSubmission();
        s.setStudentId(student.getId());
        s.setStudentName(student.getName());
        s.setAssignmentId(assignment.getId());
        s.setAssignmentTitle(assignment.getTitle());
        s.setTitle(title);
        s.setFileName(fileName);
        s.setWorkType(workType);
        s.setRemark(remark);
        return s;
    }
}
