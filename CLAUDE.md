# Teaching Evaluation System - AI Coding Standards

本文件专门用于规范 AI 在生成、重构和修改本项目代码时的行为。AI 必须严格遵守以下所有条款。

## 核心禁止行为
- **禁止单文件堆叠**：绝对禁止将新逻辑、新路由直接写入 `App.java`。
- **禁止省略 API 文档注解**：编写新类、新方法时，绝对禁止以"省略..."或"此处保持不变"替代 OpenAPI 注解和代码。

## API 文档规范

本项目使用 OpenAPI 3.0 注解（springdoc-openapi）生成 API 文档，**禁止**使用 Javadoc 多行注释（`/** ... */`）来描述类或字段。所有文档信息必须通过以下注解承载：

### Controller 类

```java
@Tag(name = "学生管理")
@RestController
public class StudentController {

    @Operation(summary = "查询学生列表", description = "返回所有在读学生的基本信息。")
    @GetMapping("/api/students")
    public List<StudentDTO> listStudents() {
        // ...
    }
}
```

### Model / DTO 类

每个字段**必须**标注 `@Schema`。类上也标注 `@Schema` 提供概述。

```java
@Schema(description = "学生信息 DTO")
public class StudentDTO {

    @Schema(description = "学生姓名", example = "张三")
    private String name;

    @Schema(description = "学号", example = "2026001")
    private String studentId;
}
```

### Entity 类

JPA 注解与 `@Schema` 并存，`@Schema` 负责 API 文档，JPA 负责持久化映射。

```java
@Entity
@Table(name = "student")
@Schema(description = "学生实体")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "主键，数据库自增", example = "1")
    private Long id;

    @Column(name = "name")
    @Schema(description = "学生姓名", example = "张三")
    private String name;
}
```

### Service 类

Service 层类不添加 OpenAPI 注解，也不添加 Javadoc。仅用标准 Spring 注解（`@Service`）标注。

## 自主创建 Controller 守则

- **写完 Service 必须补 Controller**：每次新建或修改 Service 接口/实现后，AI **必须**主动检查是否存在对应的 `@RestController`。若不存在，须立即创建 Controller 将 Service 方法暴露为 REST 端点。
- **每个 Service 方法至少一个端点**：不得存在无法通过 HTTP 访问的 Service 方法（内部辅助方法除外）。
- **Controller 模板要求**：
  - 类上标注 `@Tag(name = "模块名")` + `@RestController` + `@RequestMapping("/api")`。
  - 方法上标注 `@Operation(summary = "...", description = "...")` + 对应的 `@PostMapping` / `@GetMapping` 等。
  - 通过构造器注入 Service，禁止 `@Autowired` 字段注入。
  - 确保 Swagger UI (`/swagger-ui/index.html`) 中能直接看到该端点并支持 "Try it out"。

## 自动化编译守则
- **严禁带病交付**: AI 在交付最终代码前，必须确保编译通过，且没有任何 Warning 或 Error。如果编译失败，AI 必须根据编译器报错立即自动修复，直至编译成功后方可向用户汇报。
- **改动必编译**: 每次生成、修改或重构任何 Java 代码后，AI 必须提示用户（或在具备 Tool 权限时自行调用）执行 `mvn compile` 命令。
