# 教学评价系统

轻量级教师端作品评价系统。当前版本已跑通教师端演示主流程，支持学生管理、真实文件上传、作品提交登记、可配置 Py 预处理转发、AI 模拟评价、教师复核、统计摘要和 Excel 导出。仓库中已包含独立 Vue 前端工程和 Python AI 服务雏形，下一阶段重点是三端真实联调。

## 技术栈

- Java 17
- Maven
- Spring Boot
- Thymeleaf
- Spring Data JPA
- H2
- springdoc-openapi / Swagger UI
- Thymeleaf / HTML / CSS
- Vue 3 / Vite / TypeScript
- Python / FastAPI

## 当前最小功能

- 固定账号登录：`teacher / 123456`
- 学生管理：分页查询、关键字搜索、新增、删除、重置演示数据
- 作品提交：选择学生并登记作品标题、类型、文件名和备注
- 真实文件上传：`POST /api/submissions/upload` 保存原始文件并记录路径、大小和 MIME 类型
- Py 预处理转发：上传成功后可配置调用 Py 侧 `/api/preprocess`，默认关闭
- 作业状态：未审批、AI 已审批、已完成三类列表
- AI 模拟评价：通过 `FakeAIService` 生成评分、问题和评语
- 教师复核：保存最终分数和评语
- 统计摘要：学生数、作品数、AI 已评价数、教师已确认数、平均分
- Excel 导出：`/api/export/excel` 返回真实 `.xlsx` 文件流
- OpenAPI 接口文档
- 独立前端工程：`frontend/` 已包含 Vue 路由、布局、请求封装和主要页面
- Py 侧 AI 服务：`ai-service/DocxConv` 已包含 FastAPI `/api/preprocess` 和 `/api/evaluate/real`

## 当前未完成 / 后续计划

- Java 评价主流程仍使用 `FakeAIService`，尚未调用 Py 侧 `/api/evaluate/real`。
- Py 预处理接口已具备统一入口，但 Java 侧仍需开启 `app.preprocess.enabled=true` 做真实联调。
- Py 侧 `/api/preprocess` 已补齐 `renderStatus`、`renderEngine`、`renderWarnings`，仍需实际启动服务做真实联调验收。
- `ai-service/app.py` 仍是旧 Flask 模拟服务，后续需要统一或清理启动入口。
- Vue 前端工程已落地，但仍需安装依赖、构建验收，并继续补齐预处理状态展示。
- 登录仍是演示级固定账号，尚未接入完整认证和权限体系。
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
    ├── PreprocessClient.java
    ├── StatisticsService.java
    ├── StudentService.java
    ├── PreprocessResult.java
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

frontend
├── package.json
├── vite.config.ts
└── src
    ├── router/
    ├── layouts/
    ├── views/
    ├── components/
    ├── styles/
    ├── types/
    └── utils/

ai-service
├── start.sh
├── start.bat
├── config.py
├── app.py
├── DocxConv/
├── Evaluator/
├── ScreenshotProc/
└── ArchiveProc/
```

## 分工

- 队长：后端主流程、数据库、接口整合、代码合并、进度控制。
- B 同学：前端页面和样式，当前重点在 `frontend/`，旧 Thymeleaf 页面作为演示兜底保留。
- C 同学：AI 服务与文档，当前重点在 `ai-service/DocxConv`、`ai-service/Evaluator` 和 Java-Py 联调契约。

第 3 天进入实现阶段时，按 `docs/工作清单/第3天-实现阶段队长工作包.md` 执行：队长先统一页面流程、接口字段、文件范围和验收标准，B 负责学生管理与作品提交，C 负责 AI 评价与统计展示。
