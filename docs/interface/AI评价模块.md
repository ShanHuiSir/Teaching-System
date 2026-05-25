# AI 评价模块

## 架构概览

```
AIEvalRequestDTO (JSON 请求体)
       │
       ▼
AIService (接口)
  └── FakeAIService (@Service 模拟实现)
       │
       ▼
AIEvaluationResult (@Entity 评价结果，保存 AI 和教师评价)
```

分层设计：Controller 接收前端 JSON → 反序列化为 `AIEvalRequestDTO` → 调用 `EvaluationService` → 内部使用 `AIService` 生成模拟评价 → 保存并返回 `AIEvaluationResult`。后续参数扩展只需在 DTO 中加字段，接口签名不变。

---

## AIEvalRequestDTO — AI 评价请求 DTO

`com.teachingeval.model.AIEvalRequestDTO`

前端通过 `@RequestBody` 以 JSON 格式发送，例如：

```json
{
  "studentName": "张三",
  "fileName": "实验报告.docx",
  "submissionId": 1001
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `studentName` | `String` | 学生姓名 |
| `fileName` | `String` | 作品文件名 |
| `submissionId` | `Long` | 关联的提交 ID |

---

## AIService — AI 评价服务接口

`com.teachingeval.service.AIService`

```java
public interface AIService {
    AIEvaluationResult evaluate(AIEvalRequestDTO request);
}
```

### evaluate

| 项 | 说明 |
|----|------|
| 参数 `request` | `AIEvalRequestDTO`，包含学生姓名、作品文件名、提交 ID |
| 返回值 | `AIEvaluationResult`，包含 submissionId、AI 评分、问题、评语、教师评分、教师评语、状态 |

---

## AIEvaluationResult — AI 评价结果实体

`com.teachingeval.model.AIEvaluationResult`，JPA `@Entity`，映射至 `ai_evaluation_result` 表。第二天开始，`POST /api/evaluate` 会把 FakeAIService 的结果保存到数据库。

### 字段

| 字段 | 类型 | 列名 | 说明 |
|------|------|------|------|
| `id` | `Long` | `id` | 主键，`@GeneratedValue(IDENTITY)` 自增 |
| `submissionId` | `Long` | `submission_id` | 关联的提交 ID |
| `aiScore` | `BigDecimal` | `ai_score` | AI 建议分数 (precision=5, scale=2) |
| `aiIssues` | `String` | `ai_issues` | AI 发现的问题，TEXT 类型 |
| `aiComment` | `String` | `ai_comment` | AI 综合评语，TEXT 类型 |
| `teacherScore` | `BigDecimal` | `teacher_score` | 教师最终评分 |
| `teacherComment` | `String` | `teacher_comment` | 教师最终评语 |
| `status` | `int` | `status` | 0-未评价，1-AI 已评价，2-教师已确认 |

### 方法

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `isAiEvaluated()` | `boolean` | `status >= 1` 时返回 `true` |

---

## FakeAIService — 模拟 AI 评价服务

`com.teachingeval.service.FakeAIService`，标注 `@Service`，实现 `AIService`。
由 Spring 容器自动扫描并管理，无需手动 `new`。

### evaluate

接收 `AIEvalRequestDTO`，将 `submissionId` 回填至结果对象，其余字段返回固定值：

| 字段 | 固定值 |
|------|--------|
| `submissionId` | 取自 `request.getSubmissionId()` |
| `aiScore` | `82.50` |
| `aiIssues` | `1. 结构不够清晰，建议优化段落层次`<br>`2. 缺少核心论点支撑材料`<br>`3. 格式规范性不足，标题层级需统一` |
| `aiComment` | `整体完成度较好，但在结构组织上还有提升空间，建议加强逻辑连贯性。` |
| `status` | `1` |

### 使用示例

```java
@RestController
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/api/evaluate")
    public AIEvaluationResult evaluate(@RequestBody AIEvalRequestDTO request) {
        return evaluationService.evaluateAndSave(request);
    }
}
```

前端请求示例：

```http
POST /api/evaluate
Content-Type: application/json

{
  "studentName": "张三",
  "fileName": "实验报告.docx",
  "submissionId": 1001
}
```

## 教师最终评价保存接口

```http
POST /api/submissions/1/teacher-review
Content-Type: application/json

{
  "teacherScore": 88.0,
  "teacherComment": "整体完成较好，建议继续完善代码注释。"
}
```

保存成功后返回同一个 `AIEvaluationResult`，其中 `status` 为 `2`。
