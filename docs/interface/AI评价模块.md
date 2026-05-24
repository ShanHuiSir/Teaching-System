# AI 评价模块

## AIEvaluationResult — AI 评价结果类

`com.teachingeval.AIEvaluationResult`

### 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `Long` | 主键 |
| `submissionId` | `Long` | 关联的提交 ID |
| `aiScore` | `BigDecimal` | AI 建议分数 |
| `aiIssues` | `String` | AI 发现的问题 |
| `aiComment` | `String` | AI 评语 |
| `status` | `int` | 0-未评价, 1-AI 已评价 |

### 方法

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `isAiEvaluated()` | `boolean` | status >= 1 时返回 true |

---

## FakeAIService — 模拟 AI 评价服务

`com.teachingeval.FakeAIService`

### evaluate

```java
public AIEvaluationResult evaluate(String studentName, String fileName)
```

调用后返回一个固定的模拟评价结果。

**参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| `studentName` | `String` | 学生姓名 |
| `fileName` | `String` | 作品文件名 |

**返回值：**

| AI 字段 | 固定内容 |
|---------|----------|
| `aiScore` | `82.50` |
| `aiIssues` | `1. 结构不够清晰，建议优化段落层次`<br>`2. 缺少核心论点支撑材料`<br>`3. 格式规范性不足，标题层级需统一` |
| `aiComment` | `整体完成度较好，但在结构组织上还有提升空间，建议加强逻辑连贯性。` |
| `status` | `1`（AI 已评价） |

### 使用示例

```java
FakeAIService aiService = new FakeAIService();
AIEvaluationResult result = aiService.evaluate("张三", "实验报告.docx");

System.out.println(result.getAiScore());    // 82.50
System.out.println(result.getAiIssues());   // 三条问题
System.out.println(result.isAiEvaluated()); // true
```
