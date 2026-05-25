# Teaching Evaluation System - AI Coding Standards

本文件专门用于规范 AI 在生成、重构和修改本项目代码时的行为。AI 必须严格遵守以下所有条款。

## 核心禁止行为
- **禁止单文件堆叠**：绝对禁止将新逻辑、新路由直接写入 `App.java`。
- **禁止省略注释**：编写新类、新方法时，绝对禁止以“省略...”或“此处保持不变”替代代码和注释。

## 代码注释规范

每个新创建或修改的类、接口，**必须**在最上方包含完整的多行注释（Javadoc 格式），明确列出以下四大要素：
1. **类/接口概述**：解释该类的核心职责与业务意义。
2. **成员变量详解**：逐一说明每个成员变量的值、作用与取值范围。
3. **方法调用指南**：说明核心方法的调用入口、前置条件与预期行为。
4. **继承与实现关系**：明确说明其父类、实现的接口以及在架构中的位置。

### 注释示例模板：
```java
/**
 * [1. 类概述] 
 * 学生数据管理 Handler，负责解析 /students 路由下的 GET 请求并渲染学生列表。
 * <p> 
 * [2. 成员变量详解]
 * - private final StudentService studentService: 学生业务逻辑组件，用于持久化和查询，不可为 null。
 * - private final TemplateEngine templateEngine: 静态 HTML 字符串模版拼接引擎。
 * <p>
 * [3. 方法调用指南]
 * - 外部通过 HttpServer 的 createContext("/students", new StudentHandler()) 进行绑定和调用。
 * - 核心入口 handle(HttpExchange exchange) 会自动拦截并处理请求，处理完毕后必须显式关闭 exchange.close()。
 * <p>
 * [4. 继承与实现关系]
 * - 实现了 com.sun.net.httpserver.HttpHandler 接口。
 * - 隶属于系统的控制层 (Controller/Handler 架构层)。
 */
public class StudentHandler implements HttpHandler {
    // 代码实现...
}
```

## 自动化编译守则
- **严禁带病交付**: AI 在交付最终代码前，必须确保编译通过，且没有任何 Warning 或 Error。如果编译失败，AI 必须根据编译器报错立即自动修复，直至编译成功后方可向用户汇报。
- **改动必编译**: 每次生成、修改或重构任何 Java 代码后，AI 必须提示用户（或在具备 Tool 权限时自行调用）执行 `mvn compile` 命令。
