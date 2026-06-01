# 第 8 天 - 真实文件上传与 AI 预处理联调任务

## 今日目标

第 8 天目标不是一次性完成真实 AI，而是先把真实文件上传和 Py 侧预处理服务做成可联调的基础能力：

1. B 同学继续推进前端工程，补作业文件上传入口和 AI 处理状态展示。
2. C 同学完善 `ai-service`，补齐 DOCX 预处理降级机制、统一预处理接口和运行说明。
3. 队长定义 Java-Py 接口契约，准备 Java 侧真实文件上传最小闭环。

## 当前基线

- `main` 已包含学生分页、演示数据重置修复、最小自动化测试和日报补齐。
- `origin/feature/d8-base` 已新增 `ai-service` 初版。
- `ai-service` 当前已有 `DocxConv`、`ArchiveProc`、`ScreenshotProc` 三个子模块。
- `DocxConv` 已支持 DOCX 转 JSON，符合 AI 评价主输入方向。
- 真实文件上传、Java 调 Py 服务、真实 AI 接入尚未完成。

## B 同学任务清单

| 优先级 | 任务 | 具体要求 | 验收方式 |
| --- | --- | --- | --- |
| P0 | 继续推进独立前端工程 | 建立或完善 `frontend/`，配置本地启动命令，默认后端地址指向 `http://localhost:8080` | 前端页面能本地启动并打开 |
| P0 | 建立请求封装 | 封装 `GET`、`POST`、`DELETE` 和后续文件上传请求，统一处理 JSON、HTTP 错误和 `{ "message": "..." }` 错误 | 页面中不重复散写 `fetch` 细节 |
| P0 | 学生管理样板页 | 调用 `GET /api/students` 或 `GET /api/students/page` 展示学生列表 | 页面能看到学生学号、姓名、班级 |
| P0 | 作业文件上传入口 | 在作业提交页预留文件选择控件，展示文件名、大小、上传中、上传成功、上传失败状态 | 页面能选择文件，并能展示上传状态 |
| P1 | AI 评价状态展示 | 在评价页或作业详情区域预留 AI 评价中、评价成功、评价失败状态 | 用户能看懂当前 AI 处理进度 |
| P1 | 预处理 warning 展示 | 后端返回 `renderWarnings` 时，在页面展示提示，例如 LibreOffice 版式可能不准 | `degraded` 或 `failed` 时页面有明确提示 |
| P1 | AI 评价结果展示适配 | 展示 AI 分数、问题、评语，并预留预处理状态展示位置 | AI 结果和 warning 能同时显示 |
| P1 | 错误状态完善 | 后端返回错误、断网、接口失败时展示明确原因 | 失败场景不白屏、不无响应 |

## C 同学任务清单

| 优先级 | 任务 | 具体要求 | 验收方式 |
| --- | --- | --- | --- |
| P0 | 同步最新 `main` | 将 `d8-base` 合并或 rebase 到最新 `main` | 合并后不删除 `2026-05-29` 到 `2026-06-01` 日报 |
| P0 | 补 `renderStatus` | Py 侧 DOCX 预处理结果返回 `renderStatus` | JSON 中包含 `ok`、`degraded` 或 `failed` |
| P0 | 补 `renderEngine` | 返回当前渲染引擎 | JSON 中包含 `libreoffice`、`word`、`onlyoffice` 或 `none` |
| P0 | 补 `renderWarnings` | 返回预处理警告数组 | JSON 中包含 `renderWarnings`，无警告时为空数组 |
| P0 | LibreOffice 成功标记降级 | DOCX 经 LibreOffice 转 PDF/图片成功时，不标记 `ok`，统一标记 `degraded` | 返回 `renderStatus=degraded`、`renderEngine=libreoffice` |
| P0 | 转换失败不阻断主流程 | DOCX 转 PDF/图片失败时仍返回 DOCX JSON 结构化内容 | 接口不整体 500，返回 `renderStatus=failed` 和 warning |
| P0 | 统一预处理总接口 | 新增统一入口，例如 `POST /api/preprocess`，内部调 DOCX JSON、图片提取、OCR、可选渲染 | Java 侧只调用一个接口即可拿到预处理结果 |
| P1 | 补齐依赖和运行说明 | 补 `requirements.txt` 或模块 README，说明 FastAPI、uvicorn、python-docx、PyMuPDF、OCR、压缩包等依赖 | 新环境按文档能启动服务 |
| P1 | 补测试样例结果 | 用简单 DOCX、复杂 DOCX、转换失败场景各跑一次 | 提供返回 JSON 或日志，供队长验收 |
| P1 | 保持 AI 评价契约对齐 | `/api/evaluate` 返回字段继续对齐 Java 当前 `EvaluationResult` | 包含 `aiScore`、`aiIssues`、`aiComment`、`status` |
| P1 | 补 Py 侧接口说明 | 写清接口路径、请求方式、返回字段和错误格式 | B 同学和队长能按说明联调 |

## 队长任务清单

| 优先级 | 任务 | 具体要求 | 产出 |
| --- | --- | --- | --- |
| P0 | 定 Java-Py 接口契约 | 明确 Java 上传什么、Py 返回什么，尤其是 `renderStatus`、`renderEngine`、`renderWarnings` | 接口契约说明 |
| P0 | 控制分支基线 | 检查 `d8-base` 必须同步最新 `main`，不能误删日报和主线文档 | PR 检查结论 |
| P0 | 准备 Java 文件上传最小闭环 | 设计或实现 `multipart/form-data` 上传接口，保存原始文件并关联 `submissionId` | Java 上传接口方案或代码 |
| P0 | 做三类样例验收 | 使用简单 DOCX、复杂 DOCX、转换失败场景验收 Py 侧返回 | 测试记录 |
| P1 | Java 调 Py 服务方案 | 设计 HTTP 客户端，后续替换或并行保留 `FakeAIService` | Java-Py 联调方案 |
| P1 | 更新文档 | 将接口契约、测试结果和遗留问题写进开发期文档 | 开发期文档更新 |

## 建议接口契约草案

### Java 侧真实文件上传

```text
POST /api/submissions/upload
Content-Type: multipart/form-data

studentId: Long
title: String
workType: String
remark: String
file: MultipartFile
```

最小返回字段：

```json
{
  "id": 1,
  "studentId": 1,
  "studentName": "张三",
  "title": "实验报告",
  "fileName": "report.docx",
  "workType": "实验报告",
  "remark": "真实上传测试",
  "submittedAt": "2026-06-01T10:00:00"
}
```

### Py 侧统一预处理接口

```text
POST /api/preprocess
Content-Type: multipart/form-data

file: UploadFile
```

建议返回字段：

```json
{
  "fileType": "docx",
  "content": [],
  "images": [],
  "ocrResults": [],
  "renderStatus": "degraded",
  "renderEngine": "libreoffice",
  "renderWarnings": [
    "DOCX 由 LibreOffice 渲染，复杂排版可能与 Word 存在差异"
  ]
}
```

## 今日最小验收目标

| 顺序 | 验收项 | 负责人 | 通过标准 |
| --- | --- | --- | --- |
| 1 | `d8-base` 同步最新 `main` | C 同学 | 不删除日报和主线文档 |
| 2 | DOCX 转 JSON 可用 | C 同学 | 简单 DOCX 返回结构化 JSON |
| 3 | LibreOffice 渲染成功标记 `degraded` | C 同学 | 返回 `renderStatus=degraded` |
| 4 | 渲染失败不阻断 | C 同学 | 返回结构化内容和 `renderStatus=failed` |
| 5 | 前端文件上传入口可见 | B 同学 | 页面能选择文件并显示状态 |
| 6 | 队长完成接口契约说明 | 队长 | Java-Py 请求和返回字段明确 |

## 群内通知文本

```text
第 8 天任务重点是：真实文件上传启动 + AI 预处理服务可降级改造。

B 同学负责前端：继续推进 frontend、请求封装、学生管理样板页，同时在作业提交页预留文件上传入口，并准备展示 AI 评价状态和 renderWarnings。

C 同学负责 ai-service：先同步最新 main，避免删日报；然后补 renderStatus/renderEngine/renderWarnings，LibreOffice 转换成功标记 degraded，转换失败不能阻断 DOCX JSON 主流程；同时补统一 /api/preprocess 接口、依赖说明和三类测试样例。

队长负责 Java-Py 接口契约、分支基线检查、Java 文件上传最小闭环方案，以及简单 DOCX、复杂 DOCX、失败场景的联调验收。
```
