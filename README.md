# 教学评价系统

轻量级教师端作品评价系统。当前版本已跑通教师端演示主流程，支持学生管理、真实文件上传、作品提交登记、可配置 Py 预处理转发、AI 模拟评价、教师复核、统计摘要和 Excel 导出。仓库中已包含独立 Vue 前端工程和 Python AI 服务，当前重点是收口三端联调、演示和验收。

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
- AI 模拟评价：默认通过 `FakeAIService` 生成评分、问题和评语
- 真实 AI 调用：可通过配置开启 Java 调 Py 侧 `/api/evaluate/real`，失败时降级到模拟评价
- 教师复核：保存最终分数和评语
- 统计摘要：学生数、作品数、AI 已评价数、教师已确认数、平均分
- Excel 导出：`/api/export/excel` 返回真实 `.xlsx` 文件流
- OpenAPI 接口文档
- 独立前端工程：`frontend/` 已包含 Vue 路由、布局、请求封装和主要页面
- Py 侧 AI 服务：`ai-service/DocxConv` 已包含 FastAPI `/api/preprocess` 和 `/api/evaluate/real`
- 前端状态展示：上传页和评价页已展示预处理状态、warning 和 AI 评价失败提示

## 当前未完成 / 后续计划

- Java 评价主流程默认仍使用 `FakeAIService`，真实 AI 需要配置 `app.ai.real.enabled=true` 并提供可用 DeepSeek API Key。
- Java-Py 预处理已完成第一轮真实联调，后续演示前仍需复测 Py 服务启动和上传预处理链路。
- Py 侧 `/api/preprocess` 已补齐 `renderStatus`、`renderEngine`、`renderWarnings`；当前机器未安装 LibreOffice 时，DOCX 版式渲染会降级但文本提取仍可用。
- Vue 前端工程已完成依赖安装和构建验收；后续只做演示级小修和状态展示复查。
- OCR 当前作为图片/截图辅助预处理，MVP 阶段不承诺高准确率中文识别和批量高速识别。
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
├── requirements.txt
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
