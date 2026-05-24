# AI 评价模块

## 架构概览

```
AIService (接口)
  └── FakeAIService (@Service 模拟实现)
```

分层设计：调用方通过 Spring DI（`@Autowired`）持有 `AIService` 接口引用，Spring 容器根据激活的 profile 或 `@Primary`/`@Qualifier` 注入对应实现。后续接入真实 AI 时只需新增一个 `@Service` 实现类并调整 Bean 优先级，调用方无需修改。

---

## AIEvaluationResult — AI 评价结果实体

`com.teachingeval.model.AIEvaluationResult`，JPA `@Entity`，映射至 `ai_evaluation_result` 表。

### 字段

| 字段 | 类型 | 列名 | 说明 |
|------|------|------|------|
| `id` | `Long` | `id` | 主键，`@GeneratedValue(IDENTITY)` 自增 |
| `submissionId` | `Long` | `submission_id` | 关联的提交 ID |
| `aiScore` | `BigDecimal` | `ai_score` | AI 建议分数 (precision=5, scale=2) |
| `aiIssues` | `String` | `ai_issues` | AI 发现的问题，TEXT 类型 |
| `aiComment` | `String` | `ai_comment` | AI 综合评语，TEXT 类型 |
| `status` | `int` | `status` | 0-未评价，1-AI 已评价 |

### 方法

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `isAiEvaluated()` | `boolean` | `status >= 1` 时返回 `true` |

---

## AIService — AI 评价服务接口

`com.teachingeval.service.AIService`

```java
public interface AIService {
    AIEvaluationResult evaluate(String studentName, String fileName);
}
```

### evaluate

| 项 | 说明 |
|----|------|
| 参数 `studentName` | `String`，学生姓名 |
| 参数 `fileName` | `String`，作品文件名 |
| 返回值 | `AIEvaluationResult`，包含评分、问题、评语、状态 |

---

## FakeAIService — 模拟 AI 评价服务

`com.teachingeval.service.FakeAIService`，标注 `@Service`，实现 `AIService`。
由 Spring 容器自动扫描并管理，无需手动 `new`。

### evaluate

接收学生姓名和文件名，返回固定的模拟评价结果：

| AI 字段 | 固定值 |
|---------|--------|
| `aiScore` | `82.50` |
| `aiIssues` | `1. 结构不够清晰，建议优化段落层次`<br>`2. 缺少核心论点支撑材料`<br>`3. 格式规范性不足，标题层级需统一` |
| `aiComment` | `整体完成度较好，但在结构组织上还有提升空间，建议加强逻辑连贯性。` |
| `status` | `1` |

### 使用示例

```java
@RestController
public class EvaluationController {

    private final AIService aiService;

    public EvaluationController(AIService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/api/evaluate")
    public AIEvaluationResult evaluate() {
        return aiService.evaluate("张三", "实验报告.docx");
    }
}
```
