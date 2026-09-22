# 技术栈说明（GoodsManage / 临期食品销售管理平台）

> 本文件由项目分析整理生成，记录了该项目涉及到的全部技术栈。

## 总体架构

前后端分离的**单体应用**（非微服务），由 3 个部分组成：

| 模块 | 目录 | 说明 |
|------|------|------|
| 后端 | `backend/` | Spring Boot 服务，提供 REST API、WebSocket、文件上传 |
| 管理后台 | `frontend-admin/` | 管理端 SPA（管理员 / 商家），端口 **5174** |
| 消费者商城 | `frontend-consumer/` | 面向用户购物端 SPA，端口 **5173** |

开发模式下，前端通过 Vite 代理 `/api`、`/uploads` 到后端（目标 `http://localhost:8080`）。
数据库使用单库 MySQL：`expiry_food_db`。

---

## 后端 backend（Java）

### 运行环境
- Java **JDK 17.0.12** LTS
- 构建工具：**Maven** 3.9.11（项目内无 `mvnw`，使用 IDEA 自带 Maven）
- 应用框架：**Spring Boot 4.0.6**，内嵌 Tomcat 11.0.21
  - 配置端口：`8081`（`application.yml`）；为与前端代理一致，实际以 `--server.port=8080` 覆盖启动

### 主要依赖 / 技术
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot Starter Web | 4.0.6 | Web MVC / REST |
| Spring Boot Starter WebSocket | 4.0.6 | WebSocket 支持 |
| Spring Boot Starter Validation | 4.0.6 | Bean Validation 参数校验 |
| **MyBatis-Plus** | 3.5.16 | ORM；含 `jsqlparser`、`LambdaQueryWrapper`、逻辑删除、自动主键 |
| MySQL Connector/J | (runtime) | 数据库驱动，连接 MySQL 8.0 |
| **jjwt** (JWT) | 0.12.6 | token 生成与校验 |
| Hutool | 5.8.16 | 通用工具集 |
| Jackson (jackson-databind) | 2.21.2 | JSON 序列化 |
| Lombok | 1.18.38 | 简化实体代码 |

### 框架能力
- **鉴权**：JWT + 自定义 `JwtInterceptor`（`HandlerInterceptor` 实现，未使用 Spring Security）
- **实时通信**：原生 `@ServerEndpoint` 客服聊天 `ChatWebSocket`（`ServerEndpointExporter` 注册，握手用 token 鉴权）
- **定时任务**：Spring `@EnableScheduling` + `@Scheduled`，`ExpiryCheckTask` 做临期/过期商品检测
- **跨域**：`CorsConfig`（CorsFilter）
- **异常处理**：`GlobalExceptionHandler`（`@RestControllerAdvice`）
- **文件上传**：Multipart，本地存储至 `./uploads`，URL 前缀 `/uploads/`
- **数据配置**：MyBatis-Plus 开启驼峰映射、逻辑删除字段 `deleted`

### 分层结构
`controller / service / mapper / entity / common / config / interceptor / task / utils / websocket`（共 82 个 Java 文件）

---

## 前端（两个 Vite 项目）

两个前端基础技术栈一致，均使用 Vue 3 组合式 API（`<script setup>`）：

| 技术 | 版本 | 用途 |
|------|------|------|
| **Vue 3** | 3.4.21 | 核心框架 |
| **Vite** | 5.1.4（实际运行 5.4.21）| 构建 / 开发服务器 |
| **Vue Router** | 4.3.0 | 路由 |
| **Pinia** | 2.1.7 | 状态管理（登录态 / token）|
| **Axios** | 1.6.7 | HTTP 封装（拦截器统一携带 JWT）|
| **Element Plus** | 2.5.6 | 组件库 |
| @vitejs/plugin-vue | 5.0.4 | Vue SFC 编译插件 |
| unplugin-auto-import | 0.17.5 | API 自动导入 |
| unplugin-vue-components | 0.26.0 | Element Plus 按需自动注册 |

### 两端差异
- **frontend-consumer**（商城，5173）：仅基础栈，面向消费者。
- **frontend-admin**（管理后台，5174）：额外引入 **ECharts 6.0**（数据统计图表）、**@element-plus/icons-vue** 2.3.1（图标）。

---

## 数据库

- **MySQL 8.0.46**（Windows 本地，`localhost:3306`，账号 `root`）
- 库：`expiry_food_db`（17 张表）
- 表清单：`user / admin / merchant / product / category / orders / order_item / cart / address / favorite / banner / announcement / review / after_sale / complaint / chat_message / browse_history`
- 商品图片：本地 `/uploads` 存储，部分外链 picsum.photos 占位图

---

## 开发运行命令

```bash
# 后端（需 JDK 17 与 Maven；本机无 mvn 时可用 IDEA 自带 Maven）
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
# 或：java -jar target/expiry-food-backend-1.0.0.jar --server.port=8080

# 管理后台前端
cd frontend-admin
npm run dev          # http://localhost:5174

# 消费者商城前端
cd frontend-consumer
npm run dev          # http://localhost:5173
```

### 端口约定（重要）
| 服务 | 端口 |
|------|------|
| 后端 API | **8080**（前端代理、WebSocket `ws://…:8080/ws/chat`、图片地址均指向 8080）|
| 管理后台 | 5174 |
| 消费者商城 | 5173 |
| MySQL | 3306 |

> 注意：后端 `application.yml` 中配置端口为 `8081`，与前端代理目标 `8080` 不一致。若要长期运行，建议将配置文件端口改为 8080，否则需每次以 `--server.port=8080` 覆盖。

---

## 一句话概括

> 后端：「Spring Boot 4 + MyBatis-Plus + MySQL + JWT + WebSocket + @Scheduled」的 Java Web 单体；
> 前端：「Vue 3 + Vite + Element Plus + Pinia + Vue Router + Axios」的两套独立 SPA（管理后台带 ECharts 图表）。
