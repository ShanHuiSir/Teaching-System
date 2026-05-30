# 教学评价系统

轻量级教师端作品评价系统，使用 Spring Boot 与 Maven。当前版本已跑通教师端演示主流程，支持学生管理、作品提交登记、AI 模拟评价、教师复核、统计摘要和 Excel 导出。真实文件上传、真实大模型接入和独立前端工程仍在后续阶段推进。

## 技术栈

- Java 17
- Maven
- Spring Boot
- Thymeleaf
- Spring Data JPA
- H2
- springdoc-openapi / Swagger UI
- HTML / CSS

## 当前最小功能

- 固定账号登录：`teacher / 123456`
- 学生管理：分页查询、关键字搜索、新增、删除、重置演示数据
- 作品提交：选择学生并登记作品标题、类型、文件名和备注
- 作业状态：未审批、AI 已审批、已完成三类列表
- AI 模拟评价：通过 `FakeAIService` 生成评分、问题和评语
- 教师复核：保存最终分数和评语
- 统计摘要：学生数、作品数、AI 已评价数、教师已确认数、平均分
- Excel 导出：`/api/export/excel` 返回真实 `.xlsx` 文件流
- OpenAPI 接口文档

## 当前未完成 / 后续计划

- 真实文件上传尚未实现，目前保存作品元数据和文件名。
- 真实大模型尚未接入，目前使用 `FakeAIService` 跑通流程。
- 登录仍是演示级固定账号，尚未接入完整认证和权限体系。
- 独立前端工程尚未落地，目前仍以 Thymeleaf 页面作为可演示版本。
- 自动化测试已覆盖最小主流程，后续仍需扩展更多异常场景和页面回归。

## 编译与运行

```bash
# 编译
mvn compile

# 运行
mvn spring-boot:run
```

启动后访问：

```text
http://localhost:8080
```

接口文档：

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON：

```text
http://localhost:8080/v3/api-docs
```

健康检查接口：

```text
http://localhost:8080/api/health
```

如果 8080 端口已经被占用，可以临时换端口：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

## 项目结构

```text
src/main/java/com/teachingeval
├── TeachingSystemApplication.java
├── config
│   ├── ApiResponseAdvice.java
│   ├── CorsConfig.java
│   ├── DataInitializer.java
│   └── OpenApiConfig.java
├── controller
│   ├── ApiExceptionHandler.java
│   ├── DevDataController.java
│   ├── EvaluationController.java
│   ├── ExportController.java
│   ├── HealthController.java
│   ├── PageController.java
│   ├── StatisticsController.java
│   ├── StudentController.java
│   └── SubmissionController.java
├── dto
├── entity
├── repository
└── service
    ├── AIService.java
    ├── EvaluationService.java
    ├── ExportService.java
    ├── StatisticsService.java
    ├── StudentService.java
    ├── SubmissionService.java
    └── impl/FakeAIService.java

src/main/resources
├── static
│   ├── style.css
│   └── js/
└── templates
    ├── login.html
    ├── students.html
    ├── submit.html
    ├── works.html
    ├── pending.html
    ├── ai-reviewed.html
    ├── completed.html
    ├── evaluation.html
    └── export.html
```

## 分工

- 队长：后端主流程、数据库、接口整合、代码合并、进度控制。
- B 同学：前端页面和样式，主要修改 `src/main/resources/templates` 和 `src/main/resources/static`。
- C 同学：AI 模块与文档，先做 `AIService`、`FakeAIService` 和 `EvaluationResult`，后期再接真实 AI。

第 3 天进入实现阶段时，按 `docs/工作清单/第3天-实现阶段队长工作包.md` 执行：队长先统一页面流程、接口字段、文件范围和验收标准，B 负责学生管理与作品提交，C 负责 AI 评价与统计展示。
