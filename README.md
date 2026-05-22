# 教学评价系统

轻量级教师端作品评价系统。当前第一天目标是先跑通最小 Web 骨架，后续再逐步补齐学生管理、作品上传、AI 评价、教师复核和 Excel 导出。

## 当前最小功能

- 固定账号登录：`teacher / 123456`
- 登录页
- 学生管理页占位
- 作品评价页占位
- 报表导出入口占位

## 本地启动

项目当前不依赖 Maven、Tomcat 或数据库，直接使用 JDK 17 启动。

```bash
javac -encoding UTF-8 -d out src/main/java/com/teachingeval/App.java
java -cp out com.teachingeval.App
```

启动后访问：

```text
http://localhost:8080
```

## 第一天分工

- 队长：维护项目结构、登录主流程、后端接口边界、进度控制。
- B 同学：设计 `FakeAIService`，先返回固定评分、问题和评语。
- C 同学：按当前页面结构做 JSP/HTML/CSS 页面草图和样式。

