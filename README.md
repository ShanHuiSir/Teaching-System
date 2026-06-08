# 教学评价系统

教师端教学评价系统。当前主线为 Vue 3 SPA + Spring Boot REST API + Python FastAPI AI 服务，前端采用双 Rail / MagicBar / MD3 color / Niri 布局。系统围绕班级、作业、AI 评价、教师复核、统计和 Excel 导出完成教师端验收流程。

## 技术栈

- Java 17 / Maven / Spring Boot 3.3
- Spring Data JPA / H2
- springdoc-openapi / Swagger UI
- Vue 3 / Vite / TypeScript / SCSS
- Python / FastAPI
- EasyOCR / PyMuPDF / LibreOffice

## 快速启动

### 一键启动（推荐）

**Windows：** 双击 `start.bat`，三个服务各自在独立窗口运行。

**Linux / macOS：** `./start.sh`，三个服务在同一终端后台运行，Ctrl+C 全部停止。

### 手动启动

```bash
# 1. AI 服务（端口 8000）
cd ai-service
python -m venv .venv && source .venv/bin/activate  # Windows: .venv\Scripts\activate.bat
pip install -r requirements.txt
pip install -e DocxConv -e Evaluator -e ScreenshotProc -e ArchiveProc
python -m docxconv serve --host 0.0.0.0 --port 8000

# 2. Spring Boot 后端（端口 8080）
mvn compile
mvn spring-boot:run

# 3. Vue 前端（端口 5173）
cd frontend
npm install
npm run dev
```

### 访问地址

| 服务 | 地址 |
|---|---|
| 前端页面 | http://localhost:5173 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| 健康检查 | http://localhost:8080/api/health |
| AI 服务健康检查 | http://localhost:8000/api/health |

## 当前功能

- 登录：固定演示账号 `teacher / 123456`，登录后写入 `auth_token` Cookie。
- 路由：`/` 默认跳转 `/login`；登录后进入 `/dashboard`；业务页无 token 时跳转 `/forbidden`。
- 仪表盘：展示提交、批改、学生、评分分布和趋势统计。
- 班级管理：入口为 `/classes`，学生管理归入班级和花名册流程，不再设独立学生页。
- 作业管理：入口为 `/assignments`，作业数据和 Excel 导出在作业管理中完成，不再设独立导出页。
- 作业审批：入口为 `/review`，支持作业列表、附件区、AI 评价、教师复核、草稿恢复和分维度评分展示。
- AI 评价：Java 侧默认尝试真实 AI，Python 8000 不可用时降级到 `FakeAIService`；前端支持 `/api/evaluate/stream` 流式评分。
- Py 预处理转发：上传后可调用 AI 服务 `/api/preprocess` 进行文本提取、DOCX 解析、OCR 和压缩包处理，默认关闭。
- Excel 导出：`POST /api/export/excel` 返回 `.xlsx` 文件流，前端在 `/assignments` 中触发下载。
- 全局体验：MagicBar、双 Rail、主题切换、Cookie 偏好、Snackbar、骨架屏、错误边界和重试机制。

## 项目结构

```
Teaching-System/
├── start.bat                          # Windows 一键启动
├── start.sh                           # Linux/macOS 一键启动
├── pom.xml                            # Maven 配置
├── CLAUDE.md                          # AI 编码规范
├── src/main/java/com/teachingeval/
│   ├── TeachingSystemApplication.java # 应用入口
│   ├── config/                        # 配置（CORS、OpenAPI、数据初始化、响应包装）
│   ├── controller/                    # REST Controller（学生、提交、评价、导出、统计、健康检查等）
│   ├── dto/                           # 请求/响应 DTO
│   ├── entity/                        # JPA 实体
│   ├── repository/                    # Spring Data 仓库
│   └── service/                       # 业务逻辑 + AI 服务接口与实现
├── src/main/resources/
│   └── application.properties         # 应用配置
├── frontend/                          # Vue 3 前端
│   ├── src/
│   │   ├── router/                    # 路由配置
│   │   ├── layouts/                   # 布局组件
│   │   ├── views/                     # 页面（登录、仪表盘、作业审批、班级、作业、错误页）
│   │   ├── components/                # 通用组件（MagicBar、Snackbar、ConfirmDialog、骨架屏等）
│   │   ├── composables/               # 组合式 API（useMagicBar、useSnackbar、useTheme、useDraft 等）
│   │   ├── styles/                    # SCSS mixins
│   │   ├── types.ts                   # TypeScript 类型定义
│   │   └── utils/                     # 工具函数（axios 封装、cookie、文件图标等）
│   └── package.json
├── ai-service/                        # Python AI 服务
│   ├── DocxConv/                      # DOCX 预处理（文本提取、版式渲染、结构标注）
│   ├── Evaluator/                     # DeepSeek 评分引擎
│   ├── ScreenshotProc/                # 截图 OCR（EasyOCR）
│   ├── ArchiveProc/                   # 压缩包解压与文件分类
│   ├── config.py                      # 集中配置
│   └── requirements.txt
├── docs/
│   ├── 设计期文档/                    # 15 份设计文档
│   ├── 开发期文档/                    # 按天划分的开发记录
│   └── 工作清单/                      # 每日任务清单
└── sql/                               # 数据库脚本
```

## 分工

- **队长（ShanHuiSir）**：后端主流程、数据库设计、接口整合、代码合并、进度控制。
- **B 同学（wcr-100618）**：前端页面与样式，Vue 3 + Vite 工程搭建。
- **C 同学（LaT-SKY / 望向天脉）**：前端、AI 服务、工程基建。
