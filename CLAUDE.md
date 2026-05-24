# CLAUDE.md

## 项目概述
AI + 教师实训教学评价系统。教师登录后管理学生、AI 辅助评价学生作品、导出成绩。

## 技术栈
- Java 17 + Maven
- `com.sun.net.httpserver.HttpServer` 内置 HTTP 服务器
- 纯 HTML/CSS 服务端渲染，无前端框架
- 端口：`8080`

## 项目结构
```
src/main/java/com/teachingeval/App.java   # 唯一源文件，包含所有路由与页面
target/                                    # Maven 编译输出目录
docs/                                      # 设计文档
```

## 编译与运行
```bash
# 编译
mvn compile

# 运行
mvn exec:java -Dexec.mainClass="com.teachingeval.App"
```

## 路由
| 路径 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 登录页 |
| `/login` | POST | 登录表单提交 |
| `/students` | GET | 学生管理页 |
| `/evaluation` | GET | AI 评价页 |
| `/export` | GET | 成绩导出页 |
| `/style.css` | GET | 全局样式 |

## 固定账号
- 用户名：`teacher`
- 密码：`123456`
