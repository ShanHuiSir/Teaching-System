# 麒麟 LoongArch 虚拟机部署指南

本文用于解决验收要求中的“软件需部署在自主指令系统 LoongArch 架构 + 麒麟高级服务器版上运行”。录制演示视频时，建议先证明运行环境，再演示系统功能。

## 一、验收要求怎么理解

截图里的要求不是普通虚拟机要求，而是两项同时满足：

- 操作系统：麒麟高级服务器版，或学校指定的麒麟服务器系统。
- CPU 架构：LoongArch，系统命令应显示 `loongarch64`。

如果只在 Windows、macOS、x86 Linux 或普通 Ubuntu 虚拟机中运行，通常不能算满足该项要求。

## 二、推荐方案

推荐在 LoongArch 麒麟虚拟机中原生运行项目：

- 后端：Spring Boot，使用 `demo` 配置运行。
- 前端：Vite 开发服务器，代理 `/api` 到后端。
- AI：演示阶段默认走后端内置演示评分，不强依赖 Python AI 服务。

这样做的原因是 LoongArch 上 Docker 官方镜像、Python 二进制依赖、EasyOCR/Torch 依赖可能存在架构兼容问题。主流程演示不依赖这些不稳定因素，更适合验收录屏。

## 三、虚拟机准备

在 LoongArch 麒麟虚拟机中安装基础环境：

```bash
sudo dnf install -y git java-17-openjdk java-17-openjdk-devel maven nodejs npm curl net-tools psmisc
```

如果系统使用 `apt`，则使用：

```bash
sudo apt update
sudo apt install -y git openjdk-17-jdk maven nodejs npm curl net-tools psmisc
```

检查系统架构和工具链：

```bash
uname -m
cat /etc/os-release
java -version
mvn -v
node -v
npm -v
```

验收录屏中建议展示：

```bash
uname -m
cat /etc/os-release
```

其中 `uname -m` 应输出 `loongarch64`。

## 四、部署项目

把项目放到虚拟机中，例如：

```bash
cd ~
git clone <你的项目仓库地址> Teaching-System
cd Teaching-System
```

如果不使用 Git，也可以把整个项目目录复制到虚拟机。

给启动脚本授权：

```bash
chmod +x start-kylin-loongarch-demo.sh
```

启动演示环境：

```bash
./start-kylin-loongarch-demo.sh
```

脚本会启动：

| 服务 | 地址 |
|---|---|
| 前端页面 | http://localhost:5173 |
| 后端健康检查 | http://localhost:8080/api/health |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |

登录账号：

```text
teacher / 123456
```

## 五、录屏推荐顺序

建议按下面顺序录制，能同时证明环境合规和功能完整：

1. 展示系统环境：

```bash
uname -m
cat /etc/os-release
```

2. 启动项目：

```bash
./start-kylin-loongarch-demo.sh
```

3. 检查服务：

```bash
curl http://localhost:8080/api/health
curl -I http://localhost:5173
```

4. 浏览器访问：

```text
http://localhost:5173
```

5. 功能演示主线：

```text
登录 -> 仪表盘 -> 作业审批 -> 查看提交作品 -> 附件预览/下载 -> AI评价 -> 教师复核 -> 作业管理 -> Excel导出
```

6. 可选展示 Swagger：

```text
http://localhost:8080/swagger-ui/index.html
```

## 六、宿主机访问虚拟机

如果要在宿主机浏览器访问虚拟机中的系统，先查看虚拟机 IP：

```bash
ip addr
```

然后在宿主机访问：

```text
http://虚拟机IP:5173
```

如果访问不通，检查虚拟机网络模式和防火墙。

放行端口：

```bash
sudo firewall-cmd --add-port=5173/tcp --permanent
sudo firewall-cmd --add-port=8080/tcp --permanent
sudo firewall-cmd --reload
```

如果系统没有 `firewall-cmd`，可先检查防火墙状态：

```bash
sudo systemctl status firewalld
sudo systemctl status iptables
```

## 七、Docker 方案说明

项目中已有 Docker Compose，但 LoongArch 上不建议作为首选演示方式。原因：

- `node:22-alpine`、`nginx:1.27-alpine`、`python:3.11-slim-bookworm`、`eclipse-temurin:17-jre` 未必都有可用的 LoongArch 官方镜像。
- Python 依赖中的部分包可能需要 LoongArch 原生 wheel 或本地编译。
- OCR 相关依赖会引入大型深度学习包，在 LoongArch 上更容易失败。

因此验收主线建议使用原生启动脚本。需要说明时可以这样表述：

```text
本项目支持 Docker Compose 部署，但为了满足 LoongArch 麒麟环境验收，演示采用虚拟机内原生启动方式。后端、前端均运行在 LoongArch 麒麟系统内，AI 评分在演示环境使用后端内置降级评分，保证核心教学评价闭环可稳定展示。
```

## 八、常见问题

### 1. `uname -m` 不是 `loongarch64`

说明当前不是 LoongArch 架构，不能满足截图中的自主指令系统要求。需要换成 LoongArch 机器或学校提供的 LoongArch 麒麟虚拟机。

### 2. 后端启动失败

查看日志：

```bash
tail -100 logs/kylin-backend.log
```

常见原因是 JDK 版本不对或 Maven 依赖下载失败。

### 3. 前端启动失败

查看日志：

```bash
tail -100 logs/kylin-frontend.log
```

常见原因是 npm 源访问慢。可以配置国内源：

```bash
npm config set registry https://registry.npmmirror.com
```

### 4. 端口被占用

```bash
fuser -k 8080/tcp 5173/tcp
```

然后重新执行：

```bash
./start-kylin-loongarch-demo.sh
```

### 5. AI 服务没有启动会不会影响演示

不会影响主流程。后端 `demo` 配置会关闭真实 AI 依赖，AI 评价按钮会使用演示评分完成“AI评价 -> 教师复核 -> 统计/导出”的闭环。
