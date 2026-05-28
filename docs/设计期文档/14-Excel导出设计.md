# 14-Excel 导出设计

## 输出文件

| 项目 | 说明 |
|---|---|
| 文件名 | `成绩汇总_{yyyyMMdd}.xlsx`（如 `成绩汇总_20260528.xlsx`） |
| Sheet 名 | `成绩汇总` |
| 生成方式 | 服务端流式写出（Alibaba EasyExcel），浏览器触发 Blob 下载 |

## 列定义

| 序号 | 列标题 | 数据来源 | 类型 | 宽度 | 示例值 |
|---|---|---|---|---|---|
| 1 | 学号 | `Student.studentNo` | 文本 | 12 | `2026001` |
| 2 | 姓名 | `WorkSubmission.studentName`（快照） | 文本 | 10 | `张三` |
| 3 | 班级 | `Student.className` | 文本 | 14 | `软件 1 班` |
| 4 | 作品标题 | `WorkSubmission.title` | 文本 | 22 | `第二阶段实训报告` |
| 5 | 作品类型 | `WorkSubmission.workType` | 文本 | 12 | `实验报告` |
| 6 | 文件名 | `WorkSubmission.fileName` | 文本 | 24 | `student-work.zip` |
| 7 | AI 评分 | `EvaluationResult.aiScore` | 数值(2位小数) | 10 | `82.50` |
| 8 | AI 发现的问题 | `EvaluationResult.aiIssues` | 文本 | 40 | `1. 结构不够清晰...` |
| 9 | AI 评语 | `EvaluationResult.aiComment` | 文本 | 40 | `整体完成度较好...` |
| 10 | 教师评分 | `EvaluationResult.teacherScore` | 数值(2位小数) | 10 | `88.00` |
| 11 | 教师评语 | `EvaluationResult.teacherComment` | 文本 | 40 | `整体完成较好...` |
| 12 | 评价状态 | `EvaluationResult.status` 映射 | 文本 | 12 | `教师已确认` |

## 评价状态映射

| status 值 | 导出文本 |
|---|---|
| 0 或 无评价记录 | `未评价` |
| 1 | `已AI评价` |
| 2+ | `教师已确认` |

## 数据拼接逻辑

1. 以 `WorkSubmission`（作品提交）为主表，遍历所有提交记录
2. 通过 `WorkSubmission.studentId` 关联 `Student`，获取学号和班级
3. 通过 `WorkSubmission.id` 关联 `EvaluationResult.submissionId`，获取评价数据
4. 未关联到评价记录的提交，AI/教师字段留空，状态显示"未评价"

## 对应代码

| 层 | 文件 |
|---|---|
| DTO（列定义） | `src/main/java/com/teachingeval/dto/ExportRow.java` |
| Service | `src/main/java/com/teachingeval/service/ExportService.java` |
| Controller | `src/main/java/com/teachingeval/controller/ExportController.java` |
| 前端 | `src/main/resources/templates/export.html` |
