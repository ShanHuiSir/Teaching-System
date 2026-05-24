# AI 评价模块

## 架构概览

```
AIService (接口)
  └── FakeAIService (模拟实现)
```

分层设计：调用方持有 `AIService`，通过 `new FakeAIService()` 获取实例。后续接入真实 AI 时只需新增一个实现类，调用方无需修改。

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

## AIEvaluationResult — AI 评价结果类

`com.teachingeval.model.AIEvaluationResult`

### 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `Long` | 主键，新建对象时为 null |
| `submissionId` | `Long` | 关联的提交 ID |
| `aiScore` | `BigDecimal` | AI 建议分数 (0.00~100.00) |
| `aiIssues` | `String` | AI 发现的问题，多条目以换行分隔 |
| `aiComment` | `String` | AI 综合评语 |
| `status` | `int` | 0-未评价，1-AI 已评价 |

### 方法

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `isAiEvaluated()` | `boolean` | `status >= 1` 时返回 `true` |

---

## FakeAIService — 模拟 AI 评价服务

`com.teachingeval.service.FakeAIService`，实现 `AIService`。

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
AIService aiService = new FakeAIService();
AIEvaluationResult result = aiService.evaluate("张三", "实验报告.docx");

System.out.println(result.getAiScore());    // 82.50
System.out.println(result.isAiEvaluated()); // true
```
