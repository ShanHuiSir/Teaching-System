package com.teachingeval;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class App {
    private static final int PORT = 8080;
    private static final String USERNAME = "teacher";
    private static final String PASSWORD = "123456";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", App::handleHome);
        server.createContext("/login", App::handleLogin);
        server.createContext("/students", App::handleStudents);
        server.createContext("/evaluation", App::handleEvaluation);
        server.createContext("/export", App::handleExport);
        server.createContext("/style.css", App::handleStyle);
        server.start();

        System.out.println("教学评价系统已启动：http://localhost:" + PORT);
        System.out.println("固定账号：" + USERNAME + " / " + PASSWORD);
    }

    private static void handleHome(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "text/plain; charset=UTF-8", "Method Not Allowed");
            return;
        }
        send(exchange, 200, "text/html; charset=UTF-8", loginPage(null));
    }

    private static void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            redirect(exchange, "/");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseForm(body);
        String username = form.getOrDefault("username", "");
        String password = form.getOrDefault("password", "");

        if (USERNAME.equals(username) && PASSWORD.equals(password)) {
            redirect(exchange, "/students");
            return;
        }

        send(exchange, 401, "text/html; charset=UTF-8", loginPage("账号或密码错误，请使用 teacher / 123456"));
    }

    private static void handleStudents(HttpExchange exchange) throws IOException {
        String html = layout("学生管理", """
                <section class="panel">
                  <div class="panel-header">
                    <h2>学生列表</h2>
                    <button type="button">新增学生</button>
                  </div>
                  <table>
                    <thead>
                      <tr>
                        <th>学号</th>
                        <th>姓名</th>
                        <th>班级</th>
                        <th>操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td>2026001</td>
                        <td>张三</td>
                        <td>软件 1 班</td>
                        <td><a href="/evaluation">进入评价</a></td>
                      </tr>
                    </tbody>
                  </table>
                </section>
                """);
        send(exchange, 200, "text/html; charset=UTF-8", html);
    }

    private static void handleEvaluation(HttpExchange exchange) throws IOException {
        String html = layout("作品评价", """
                <section class="panel">
                  <h2>作品评价</h2>
                  <form class="form-grid">
                    <label>
                      学生
                      <input value="张三" readonly>
                    </label>
                    <label>
                      作品文件名
                      <input value="student-work.zip" readonly>
                    </label>
                    <button type="button">点击 AI 评价</button>
                  </form>
                </section>
                <section class="panel">
                  <h2>AI 评价结果</h2>
                  <p><strong>评分建议：</strong>85</p>
                  <p><strong>主要问题：</strong>作品结构完整，但说明文档还不够清楚。</p>
                  <p><strong>评语草稿：</strong>整体完成度较好，建议继续完善功能说明和测试截图。</p>
                </section>
                <section class="panel">
                  <h2>教师复核</h2>
                  <form class="form-grid">
                    <label>
                      最终分数
                      <input value="85">
                    </label>
                    <label>
                      最终评语
                      <textarea>整体完成度较好，继续优化细节。</textarea>
                    </label>
                    <button type="button">保存最终评价</button>
                  </form>
                </section>
                """);
        send(exchange, 200, "text/html; charset=UTF-8", html);
    }

    private static void handleExport(HttpExchange exchange) throws IOException {
        String html = layout("导出成绩", """
                <section class="panel">
                  <h2>导出成绩</h2>
                  <p>这里后续放 Excel 导出功能。</p>
                  <button type="button">导出 Excel</button>
                </section>
                """);
        send(exchange, 200, "text/html; charset=UTF-8", html);
    }

    private static String loginPage(String error) {
        String errorHtml = error == null ? "" : "<p class=\"error\">" + escapeHtml(error) + "</p>";
        return """
                <!doctype html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>教学评价系统 - 登录</title>
                  <link rel="stylesheet" href="/style.css">
                </head>
                <body class="login-page">
                  <main class="login-card">
                    <h1>教学评价系统</h1>
                    <p>教师端作品评价与成绩导出</p>
                    <form method="post" action="/login">
                      <label>
                        账号
                        <input name="username" value="teacher" autocomplete="username">
                      </label>
                      <label>
                        密码
                        <input name="password" type="password" value="123456" autocomplete="current-password">
                      </label>
                      %s
                      <button type="submit">登录</button>
                    </form>
                  </main>
                </body>
                </html>
                """.formatted(errorHtml);
    }

    private static String layout(String title, String content) {
        return """
                <!doctype html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>教学评价系统 - %s</title>
                  <link rel="stylesheet" href="/style.css">
                </head>
                <body>
                  <header class="topbar">
                    <h1>教学评价系统</h1>
                    <nav>
                      <a href="/students">学生管理</a>
                      <a href="/evaluation">作品评价</a>
                      <a href="/export">导出成绩</a>
                      <a href="/">退出</a>
                    </nav>
                  </header>
                  <main class="container">
                    %s
                  </main>
                </body>
                </html>
                """.formatted(escapeHtml(title), content);
    }

    private static void handleStyle(HttpExchange exchange) throws IOException {
        String css = """
                * {
                  box-sizing: border-box;
                }

                body {
                  margin: 0;
                  color: #1f2937;
                  background: #f4f6f8;
                  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Microsoft YaHei", sans-serif;
                }

                a {
                  color: #2563eb;
                  text-decoration: none;
                }

                button {
                  min-height: 38px;
                  padding: 0 16px;
                  border: 0;
                  border-radius: 6px;
                  color: #fff;
                  background: #2563eb;
                  cursor: pointer;
                  font-size: 14px;
                }

                input,
                textarea {
                  width: 100%;
                  min-height: 38px;
                  padding: 8px 10px;
                  border: 1px solid #cbd5e1;
                  border-radius: 6px;
                  font: inherit;
                }

                textarea {
                  min-height: 88px;
                  resize: vertical;
                }

                label {
                  display: grid;
                  gap: 6px;
                  color: #475569;
                  font-size: 14px;
                }

                .login-page {
                  min-height: 100vh;
                  display: grid;
                  place-items: center;
                  padding: 24px;
                  background: #eef2f7;
                }

                .login-card {
                  width: min(420px, 100%);
                  padding: 28px;
                  border-radius: 8px;
                  background: #fff;
                  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.12);
                }

                .login-card h1 {
                  margin: 0 0 8px;
                  font-size: 28px;
                }

                .login-card p {
                  margin: 0 0 22px;
                  color: #64748b;
                }

                .login-card form,
                .form-grid {
                  display: grid;
                  gap: 16px;
                }

                .error {
                  margin: 0;
                  color: #b91c1c;
                  font-size: 14px;
                }

                .topbar {
                  display: flex;
                  align-items: center;
                  justify-content: space-between;
                  gap: 24px;
                  min-height: 64px;
                  padding: 0 32px;
                  background: #fff;
                  border-bottom: 1px solid #e2e8f0;
                }

                .topbar h1 {
                  margin: 0;
                  font-size: 20px;
                }

                .topbar nav {
                  display: flex;
                  gap: 18px;
                  flex-wrap: wrap;
                }

                .container {
                  width: min(1040px, calc(100% - 32px));
                  margin: 24px auto;
                  display: grid;
                  gap: 18px;
                }

                .panel {
                  padding: 22px;
                  border-radius: 8px;
                  background: #fff;
                  border: 1px solid #e2e8f0;
                }

                .panel-header {
                  display: flex;
                  align-items: center;
                  justify-content: space-between;
                  gap: 16px;
                  margin-bottom: 14px;
                }

                .panel h2 {
                  margin: 0 0 14px;
                  font-size: 20px;
                }

                .panel-header h2 {
                  margin: 0;
                }

                table {
                  width: 100%;
                  border-collapse: collapse;
                  background: #fff;
                }

                th,
                td {
                  padding: 12px;
                  border-bottom: 1px solid #e2e8f0;
                  text-align: left;
                }

                th {
                  color: #475569;
                  background: #f8fafc;
                  font-weight: 600;
                }

                @media (max-width: 640px) {
                  .topbar {
                    align-items: flex-start;
                    flex-direction: column;
                    padding: 16px;
                  }
                }
                """;
        send(exchange, 200, "text/css; charset=UTF-8", css);
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> form = new HashMap<>();
        if (body == null || body.isBlank()) {
            return form;
        }

        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            String key = decode(keyValue[0]);
            String value = keyValue.length > 1 ? decode(keyValue[1]) : "";
            form.put(key, value);
        }
        return form;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
