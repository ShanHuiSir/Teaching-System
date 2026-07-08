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

### 麒麟 LoongArch 虚拟机演示启动（验收推荐）

验收要求中写明"LoongArch 架构 + 麒麟高级服务器版"，因此最终演示建议在 LoongArch 麒麟虚拟机内原生启动项目，而不是优先使用 Docker。LoongArch 上部分官方镜像和 Python/OCR 依赖可能没有稳定可用的原生包，演示主流程使用后端 `demo` 配置和内置演示评分，能稳定展示"登录 -> 作业审批 -> 附件预览/下载 -> AI 评价 -> 教师复核 -> 统计/Excel 导出"的完整闭环。

```bash
# 1. 验收录屏先证明运行环境
uname -m
cat /etc/os-release

# 2. 确认基础环境
java -version
mvn -v
node -v
npm -v

# 3. 启动演示环境
chmod +x start-kylin-loongarch-demo.sh
./start-kylin-loongarch-demo.sh

# 4. 检查状态
curl http://localhost:8080/api/health
curl -I http://localhost:5173
```

演示访问地址：

| 访问位置 | 前端页面 | 后端健康检查 | Swagger UI |
|---|---|---|---|
| 麒麟虚拟机内部 | http://localhost:5173 | http://localhost:8080/api/health | http://localhost:8080/swagger-ui/index.html |
| 宿主机访问虚拟机 | http://虚拟机IP:5173 | http://虚拟机IP:8080/api/health | http://虚拟机IP:8080/swagger-ui/index.html |

麒麟虚拟机没有公网 IP 不影响演示。常见调试方式：

```bash
# 查看虚拟机内网 IP
ip addr

# 确认服务端口已经监听
ss -lntp | grep -E '(:5173|:8080)'

# 虚拟机内自测
curl http://localhost:8080/api/health
curl -I http://localhost:5173
```

如果虚拟机内部访问正常，但宿主机访问不通，优先检查：

- 虚拟机网络模式是否为桥接、Host-Only，或 NAT 是否已配置端口转发。
- 麒麟防火墙是否放行 `5173/tcp` 和 `8080/tcp`。
- 宿主机访问地址是否使用虚拟机 IP，而不是 `localhost`。

常用防火墙放行命令（如系统启用 firewalld）：

```bash
sudo firewall-cmd --add-port=5173/tcp --permanent
sudo firewall-cmd --add-port=8080/tcp --permanent
sudo firewall-cmd --reload
```

停止演示环境：

```bash
fuser -k 8080/tcp 5173/tcp
```

完整操作说明见 `docs/部署文档/01-麒麟LoongArch虚拟机部署指南.md`。

### Docker 演示启动（可选）

项目保留 Docker Compose 部署方式，适合普通 Linux 服务器或镜像源已适配 LoongArch 的环境。若在 LoongArch 麒麟虚拟机中 Docker 镜像构建失败，请使用上面的原生启动方案。

```bash
mvn -DskipTests package
docker compose up --build -d
docker compose ps
```

### 访问地址

| 服务 | 地址 |
|---|---|
| 前端页面 | http://localhost:5173 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| 健康检查 | http://localhost:8080/api/health |
| AI 服务健康检查（可选） | http://localhost:8000/api/health |

## 部署文档

详细的部署文档请查看 `docs/部署文档/` 目录：

- [00-部署文档目录.md](docs/部署文档/00-部署文档目录.md) - 部署文档索引
- [01-麒麟LoongArch虚拟机部署指南.md](docs/部署文档/01-麒麟LoongArch虚拟机部署指南.md) - 验收专用部署指南
- [02-部署方案汇总.md](docs/部署文档/部署方案汇总.md) - 4 种部署方式完整指南

## 当前功能

- 登录：固定演示账号默认 `teacher / 123456`，可通过 `TEACHING_EVAL_USERNAME` / `TEACHING_EVAL_PASSWORD` 覆盖；登录成功后后端写入 HttpOnly `auth_token` 会话 Cookie，前端仅保存 `user_name` 用于显示和路由状态。
- 路由：`/` 默认跳转 `/login`；登录后进入 `/dashboard`；业务页无登录状态时跳转 `/forbidden`，后端 API 同步校验会话。
- 仪表盘：展示提交、批改、学生、评分分布和趋势统计。
- 班级管理：入口为 `/classes`，学生管理归入班级和花名册流程，不再设独立学生页。
- 作业管理：入口为 `/assignments`，作业数据和 Excel 导出在作业管理中完成，不再设独立导出页。
- 作业审批：入口为 `/review`，支持作业列表、附件区、AI 评价、教师复核、草稿恢复和分维度评分展示。
- AI 评价：Java 侧默认尝试真实 AI，Python 8000 不可用时降级到 `FakeAIService`；前端支持 `/api/evaluate/stream` 流式评分。
- Py 预处理转发：上传后可调用 AI 服务 `/api/preprocess` 进行文本提取、DOCX 解析、OCR 和压缩包处理，默认关闭。
- Excel 导出：`POST /api/export/excel` 返回 `.xlsx` 文件流，前端在 `/assignments` 中触发下载。
- 安全收口：后端 API 已加轻量认证过滤器、CORS 白名单、写操作跨站请求头校验、AI 调用速率限制和审计日志。
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
│   ├── README.md                      # 文档导航入口
│   ├── 设计期文档/                    # 15 份设计文档
│   ├── 部署文档/                      # 部署指南
│   ├── 开发期文档/                    # 按天划分的开发记录
│   └── 工作清单/                      # 每日任务清单
└── sql/                               # 数据库脚本
```

## 文档导航

详细的项目文档请查看 `docs/` 目录：

- [docs/README.md](docs/README.md) - 文档导航入口
- [docs/设计期文档/](docs/设计期文档/) - 15 份设计文档（项目概述、业务流程、项目结构、数据库设计等）
- [docs/部署文档/](docs/部署文档/) - 部署指南（4 种部署方式、配置说明、健康检查等）
- [docs/开发期文档/](docs/开发期文档/) - 开发过程记录（日进度汇报表、数据库变更、测试记录等）
- [docs/工作清单/](docs/工作清单/) - 任务规划

## 分工

- **队长（ShanHuiSir）**：后端主流程、数据库设计、接口整合、代码合并、进度控制。
- **B 同学（wcr-100618）**：前端页面与样式，Vue 3 + Vite 工程搭建。
- **C 同学（LaT-SKY / 望向天脉）**：前端、AI 服务、工程基建。
