# 教学评价系统

轻量级教师端作品评价系统，使用 Spring Boot 与 Maven。当前目标是先跑通最小 Web 骨架，后续逐步补齐学生管理、作品上传、AI 评价、教师复核和 Excel 导出。

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
- 登录页
- 学生管理页占位
- 作品评价页占位
- 报表导出入口占位
- AI 模拟评价服务
- OpenAPI 接口文档

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
│   └── OpenApiConfig.java
├── controller
│   ├── HealthController.java
│   └── PageController.java
├── model
│   └── AIEvaluationResult.java
└── service
    ├── AIService.java
    └── FakeAIService.java

src/main/resources
├── static
│   └── style.css
└── templates
    ├── login.html
    ├── students.html
    ├── evaluation.html
    └── export.html
```

## 分工

- 队长：后端主流程、数据库、接口整合、代码合并、进度控制。
- B 同学：前端页面和样式，主要修改 `src/main/resources/templates` 和 `src/main/resources/static`。
- C 同学：AI 模块与文档，先做 `AIService`、`FakeAIService` 和 `AIEvaluationResult`，后期再接真实 AI。
