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

### Docker 演示启动（麒麟虚拟机）

最终演示环境建议使用麒麟虚拟机中的 Docker Engine 和 Docker Compose 插件，不需要安装 Docker Desktop。Docker Compose 会同时启动三个服务：`frontend`、`backend`、`ai-service`。没有 DeepSeek Key 时，AI 服务仍可启动，真实评分接口会降级返回默认提示，适合离线演示和验收彩排。

```bash
# 1. 确认基础环境
docker --version
docker compose version
mvn -v
node -v
npm -v

# 2. 拉取最新代码并构建本地产物
git pull origin main
mvn -DskipTests package

cd frontend
npm install
npm run build
cd ..

# 3. 可选：如需真实大模型评分，先配置 Key；没有 Key 可跳过
export DEEPSEEK_API_KEY=你的DeepSeekKey

# 4. 启动容器
docker compose up --build -d

# 5. 检查状态
docker compose ps
curl http://localhost:8080/api/health
curl http://localhost:8000/api/health
```

Docker 演示访问地址：

| 访问位置 | 前端页面 | 后端健康检查 | AI 服务健康检查 |
|---|---|---|---|
| 麒麟虚拟机内部 | http://localhost:5173 | http://localhost:8080/api/health | http://localhost:8000/api/health |
| 宿主机访问虚拟机 | http://虚拟机IP:5173 | http://虚拟机IP:8080/api/health | http://虚拟机IP:8000/api/health |
