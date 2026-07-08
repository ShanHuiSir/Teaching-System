# 麒麟 LoongArch 虚拟机部署指南

## 一、验收要求解读

验收要求中明确规定：

> 软件需部署在自主指令系统 LoongArch 架构 + 麒麟高级服务器版上运行

这意味着必须同时满足两个条件：

1. **操作系统**：麒麟高级服务器版（或学校指定的麒麟服务器系统）
2. **CPU 架构**：LoongArch（系统命令应显示 `loongarch64`）

如果只在 Windows、macOS、x86 Linux 或普通 Ubuntu 虚拟机中运行，**不能算满足该项要求**。

---

## 二、推荐方案

**推荐在 LoongArch 麒麟虚拟机中原生运行项目**，而不是使用 Docker。

### 为什么推荐原生启动？

| 原因 | 说明 |
|------|------|
| **架构合规** | 直接在 LoongArch 系统上运行，满足验收要求 |
| **Docker 镜像缺失** | LoongArch 上 `node:22-alpine`、`python:3.11-slim` 等官方镜像可能不存在 |
| **Python 依赖问题** | EasyOCR/Torch 等 AI 依赖在 LoongArch 上可能无法安装 |
| **演示稳定性** | 原生启动不依赖外部镜像源，保证核心功能稳定展示 |
| **录屏清晰度** | 直接在虚拟机内运行，环境证明更直观 |

### 演示环境说明

- **后端**：Spring Boot，使用 `demo` 配置运行（关闭真实 AI 依赖）
- **前端**：Vite 开发服务器，代理 `/api` 到后端
- **AI 服务**：演示阶段使用后端内置 `FakeAIService`，返回演示评分（82.50 分）

---

## 三、虚拟机准备

### 1. 安装基础环境

在 LoongArch 麒麟虚拟机中安装基础工具：

```bash
# 使用 dnf（麒麟系统可能使用 dnf）
sudo dnf install -y git java-17-openjdk java-17-openjdk-devel maven nodejs npm curl net-tools psmisc

# 或使用 apt（如果系统使用 apt）
sudo apt update
sudo apt install -y git openjdk-17-jdk maven nodejs npm curl net-tools psmisc
```

### 2. 检查系统架构和工具链

```bash
# 检查 CPU 架构（必须为 loongarch64）
uname -m

# 检查操作系统信息
cat /etc/os-release

# 检查工具链版本
java -version
mvn -v
node -v
npm -v
```

**验收录屏建议**：在录屏开始时展示以下命令的输出：

```bash
uname -m
cat /etc/os-release
```

其中 `uname -m` 应输出 `loongarch64`，`cat /etc/os-release` 应显示麒麟系统信息。

---

## 四、部署项目

### 1. 获取项目

将项目放到虚拟机中，例如：

```bash
cd ~
git clone <你的项目仓库地址> Teaching-System
cd Teaching-System
```

**替代方案**：如果不使用 Git，也可以将整个项目目录复制到虚拟机。

### 2. 给启动脚本授权

```bash
chmod +x start-kylin-loongarch-demo.sh
```

### 3. 启动演示环境

```bash
./start-kylin-loongarch-demo.sh
```

启动脚本会执行以下操作：

1. 验证系统架构和工具链
2. 停止旧的演示进程（如果有）
3. 启动后端（使用 `demo` 配置）
4. 启动前端（Vite 开发服务器）

### 4. 访问系统

启动成功后，可以通过以下地址访问：

| 服务 | 地址 |
|------|------|
| 前端页面 | `http://localhost:5173` |
| 后端健康检查 | `http://localhost:8080/api/health` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |

**登录账号**：

```text
teacher / 123456
```

---

## 五、录屏推荐顺序

建议按以下顺序录制演示视频，能同时证明环境合规和功能完整：

### 1. 展示系统环境（验收必需）

```bash
uname -m
cat /etc/os-release
```

**说明**：这两条命令的输出证明系统运行在 LoongArch 架构的麒麟服务器上。

### 2. 启动项目

```bash
./start-kylin-loongarch-demo.sh
```

**说明**：展示项目启动过程，等待服务完全启动。

### 3. 检查服务

```bash
curl http://localhost:8080/api/health
curl -I http://localhost:5173
```

**预期输出**：

```bash
# 后端健康检查
{"status":"ok"}

# 前端健康检查
HTTP/1.1 200 OK
...
```

### 4. 浏览器访问

打开浏览器，访问：

```
http://localhost:5173
```

### 5. 功能演示主线

按以下顺序演示功能：

```
登录 → 仪表盘 → 作业审批 → 
查看提交作品 → 附件预览/下载 → 
AI评价 → 教师复核 → 
作业管理 → Excel导出
```

**详细步骤**：

1. **登录**：输入 `teacher / 123456`，点击登录
2. **仪表盘**：展示统计概览、评分分布图表
3. **作业审批**：
   - 查看提交的作业列表
   - 点击某个作业查看详情
   - 查看学生信息和附件
   - 点击"AI评价"按钮（使用演示评分）
   - 点击"教师复核"，修改分数和评语
   - 提交复核结果
4. **作业管理**：
   - 查看作业列表
   - 点击"导出 Excel"按钮
   - 展示下载的成绩表文件

### 6. 可选展示 Swagger

访问：

```
http://localhost:8080/swagger-ui/index.html
```

展示 API 文档，可以尝试 "Try it out" 功能。

---

## 六、宿主机访问虚拟机

如果需要在宿主机浏览器访问虚拟机中的系统：

### 1. 查看虚拟机 IP

```bash
ip addr
```

找到虚拟机的内网 IP 地址（通常是 `192.168.x.x` 或 `10.x.x.x`）。

### 2. 宿主机访问

在宿主机浏览器中访问：

```
http://虚拟机IP:5173
```

### 3. 如果访问不通

#### 检查虚拟机网络模式

- **桥接模式**：虚拟机与宿主机在同一局域网，可以直接访问
- **Host-Only 模式**：只能在宿主机访问虚拟机
- **NAT 模式**：需要配置端口转发

#### 检查防火墙

麒麟系统可能启用了防火墙，需要放行端口：

```bash
# 检查防火墙状态
sudo systemctl status firewalld
sudo systemctl status iptables

# 放行端口（如果使用 firewalld）
sudo firewall-cmd --add-port=5173/tcp --permanent
sudo firewall-cmd --add-port=8080/tcp --permanent
sudo firewall-cmd --reload

# 或使用 iptables
sudo iptables -A INPUT -p tcp --dport 5173 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
sudo service iptables save
```

---

## 七、停止服务

演示结束后，可以停止服务：

```bash
fuser -k 8080/tcp 5173/tcp
```

或查看日志文件位置（脚本会输出）：

```bash
# 后端日志
logs/kylin-backend.log

# 前端日志
logs/kylin-frontend.log
```

---

## 八、Docker 方案说明

虽然项目中已有 Docker Compose 配置，但在 LoongArch 麒麟虚拟机中**不建议使用 Docker 作为首选演示方式**。

### 为什么不推荐？

| 问题 | 说明 |
|------|------|
| **官方镜像缺失** | `node:22-alpine`、`python:3.11-slim`、`eclipse-temurin:17-jre` 等镜像在 LoongArch 上可能不存在 |
| **Python 依赖问题** | EasyOCR/Torch 等深度学习包在 LoongArch 上可能无法编译安装 |
| **构建失败风险** | Dockerfile 中的 `apt-get` 命令可能因架构不匹配而失败 |

### 何时可以使用 Docker？

- 在普通 x86_64 Linux 服务器上部署
- 在有完整 LoongArch 镜像源的环境中
- 作为开发环境的快速启动方式（非验收场景）

### 如果必须使用 Docker

```bash
# 编译后端 JAR
mvn -DskipTests package

# 启动服务
docker compose up --build -d

# 检查状态
docker compose ps
```

---

## 九、常见问题

### 1. `uname -m` 不是 `loongarch64`

**问题**：说明当前不是 LoongArch 架构，不能满足验收要求。

**解决**：需要换成 LoongArch 机器或学校提供的 LoongArch 麒麟虚拟机。

### 2. 后端启动失败

**查看日志**：

```bash
tail -100 logs/kylin-backend.log
```

**常见原因**：
- JDK 版本不对（需要 JDK 17）
- Maven 依赖下载失败
- 端口 8080 被占用

### 3. 前端启动失败

**查看日志**：

```bash
tail -100 logs/kylin-frontend.log
```

**常见原因**：
- npm 源访问慢（可以配置国内源）
- 依赖安装失败

**解决**：

```bash
npm config set registry https://registry.npmmirror.com
rm -rf node_modules package-lock.json
npm install
```

### 4. 端口被占用

**查找占用进程**：

```bash
lsof -ti :8080
ss -tlnp | grep :8080
```

**杀死进程**：

```bash
fuser -k 8080/tcp 5173/tcp
```

然后重新执行：

```bash
./start-kylin-loongarch-demo.sh
```

### 5. AI 服务没有启动会不会影响演示？

**不会影响主流程**。

后端 `demo` 配置会关闭真实 AI 依赖，AI 评价按钮会使用 `FakeAIService` 返回演示评分（82.50 分），保证"AI评价 → 教师复核 → 统计/导出"的闭环可正常展示。

### 6. 登录失败

**检查账号**：

默认账号为 `teacher / 123456`，也可以使用 `temp / 123456`。

**检查 Cookie**：

浏览器需要允许 Cookie，后端通过 `auth_token` Cookie 进行认证。

### 7. 文件上传失败

**检查配置**：

后端默认允许上传 50MB 文件，如果文件过大，需要修改配置：

```properties
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=120MB
```

---

## 十、验收话术建议

在演示时，可以这样说明部署方案：

> "本项目支持 Docker Compose 部署，但为了满足 LoongArch 麒麟环境验收要求，演示采用虚拟机内原生启动方式。后端使用 Spring Boot，前端使用 Vue 3，均运行在 LoongArch 麒麟系统内。AI 评分在演示环境使用后端内置降级评分，保证核心教学评价闭环可稳定展示，无需依赖外部 AI 服务。"

---

## 附录：系统架构图

```
┌─────────────────────────────────────────┐
│      麒麟高级服务器版 (LoongArch)        │
├─────────────────────────────────────────┤
│                                         │
│  ┌─────────────┐    ┌─────────────┐    │
│  │   前端      │    │   后端      │    │
│  │  Vue 3      │    │ Spring Boot │    │
│  │  (5173)     │───▶│  (8080)     │    │
│  └─────────────┘    └──────┬──────┘    │
│                            │            │
│                            ▼            │
│                    ┌─────────────┐     │
│                    │  Fake AI    │     │
│                    │ (演示评分)  │     │
│                    └─────────────┘     │
│                                         │
└─────────────────────────────────────────┘
```

---

## 更新记录

### 2026.7.8

- 从 `docs/演示文档/` 迁移到 `docs/部署文档/`
- 更新为当前项目实际状态
- 补充常见问题和验收话术建议
