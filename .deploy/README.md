# 公网访问部署说明

把「消费者商城 + 管理后台 + 后端 API」收在同一个源下，再通过 Cloudflare 隧道暴露成公网 https 链接。

> 注意：当前用的是**临时隧道（quick tunnel）**，每次重启域名都会变。链接域名是随机分配的。

## 架构

```
https://<随机域名>.trycloudflare.com
        │
        ▼
  Node 网关 127.0.0.1:9000   （.deploy/gateway.js，零依赖）
        ├── /            → frontend-consumer/dist   （消费者商城，SPA fallback）
        ├── /admin/      → frontend-admin/dist      （管理后台，SPA fallback）
        ├── /api/**      → Spring Boot :8080
        ├── /uploads/**  → Spring Boot :8080        （本地图片）
        └── /ws/**       → Spring Boot :8080        （WebSocket 升级转发）
                              │
                              ▼
                        MySQL :3306（本机 expiry_food_db）
```

## 启动（3 个进程）

开三个终端，分别在项目根目录下执行：

```bash
# 1) 后端（端口必须是 8080，前端和网关都指向它）
#    -Xmx384m 是必须的：本机内存紧张，不加堆上限时 JVM 会按物理内存 1/4 预留，
#    实测会因内存不足被系统直接杀掉（连网关和隧道一起被回收）。
cd backend
"D:/tool/jdk-17.0.12/bin/java" -Xms128m -Xmx384m -XX:+UseSerialGC \
  -jar target/expiry-food-backend-1.0.0.jar --server.port=8080

# 2) 网关
cd .deploy
node gateway.js

# 3) 公网隧道
cd .deploy
./cloudflared.exe tunnel --url http://127.0.0.1:9000 --no-autoupdate
# 输出里 「Your quick Tunnel has been created! Visit it at:」 下面那行就是公网地址
```

## 改了前端代码之后

必须重新构建，网关读的是 `dist`：

```bash
cd frontend-admin    && npm run build   # 产物带 /admin/ 前缀
cd frontend-consumer && npm run build   # 产物在根路径
```

## 本地开发（不受影响）

```bash
cd frontend-consumer && npm run dev   # http://localhost:5173
cd frontend-admin    && npm run dev   # http://localhost:5174/admin/
```

管理后台的路由 base 本来就是 `/admin`，所以 dev 地址也带这个后缀。

## 为对外访问所做的改动

| 文件 | 改动 | 原因 |
|------|------|------|
| `frontend-consumer/src/utils/image.js` | `BASE_URL` 由 `http://localhost:8080` 改为 `''` | 原来图片硬编码指向本机，公网访客的浏览器访问不到 |
| `frontend-consumer/src/views/Chat.vue` | WebSocket 地址 `location.hostname:8080` → `location.host` | 同上；且 https 下单用 8080 端口会失败 |
| `frontend-admin/src/views/merchant/MerchantChat.vue` | 同上 | 同上 |
| `frontend-admin/vite.config.js` | 构建时 `base: '/admin/'` | 让产物资源路径带前缀，挂在子路径下不 404 |
| 两个 `vite.config.js` | 增加 `/ws` 代理（`ws: true`） | 配合 WebSocket 改同源，保证本地 dev 也能连 |

**未改动**：后端 jar、数据库、管理后台路由。

## 已知限制

- **临时隧道的通病**：链接只能用「临时」性质，无可用性保证，重启换域名。要固定域名需用 Cloudflare 账号建 named tunnel。
- **必须保持开机**：本机、MySQL、后端、网关、隧道任一关闭，公网即不可访问。
- **上传目录是本地的**：`backend/uploads`，公网访客通过后端上传的图片保存在你本机。已确认现有 127 张图片可正常访问。
- **管理端密码是明文 MD5**：属于原项目既有实现（`DigestUtil.md5Hex`），对外演示够用，别当生产环境。
- **本机内存吃紧**：16GB 内存常驻 IDEA + Edge 时只剩 ~1.8GB。三个进程被系统 OOM 回收过一次。
  已用 `-Xmx384m` 限制后端堆；如果还要更稳，关掉一些 Edge 标签页。
- **本机 DNS 可能刷不出隧道域名**：ISP 的 DNS 缓存对新分配的 `*.trycloudflare.com` 有延迟，
  表现为本机 `curl` 报 `000`／`Non-existent domain`，但公共 DNS（`1.1.1.1`、`8.8.8.8`）能解析，
  **外部访客不受影响**。本机要验证可加 `--resolve <域名>:443:104.16.230.132` 绕过。
