/**
 * 单端口网关：把「消费者商城 + 管理后台 + 后端 API」收在同一个源下，
 * 便于用一个公网域名（Cloudflare Tunnel）对外提供访问。
 *
 *   /            -> frontend-consumer/dist   （SPA fallback）
 *   /admin/**    -> frontend-admin/dist      （SPA fallback，与 vite base '/admin/' 对应）
 *   /api/**      -> Spring Boot :8080
 *   /uploads/**  -> Spring Boot :8080        （本地图片文件）
 *   /ws/**       -> Spring Boot :8080        （WebSocket upgrade 转发）
 *
 * 仅使用 Node 内置模块，无第三方依赖。
 */
const http = require('http')
const fs = require('fs')
const path = require('path')
const zlib = require('zlib')
const { URL } = require('url')

const PORT = Number(process.env.GATEWAY_PORT || 9000)
const BACKEND_HOST = process.env.BACKEND_HOST || '127.0.0.1'
const BACKEND_PORT = Number(process.env.BACKEND_PORT || 8080)
const PROJECT_ROOT = path.resolve(__dirname, '..')

const CONSUMER_DIST = path.join(PROJECT_ROOT, 'frontend-consumer', 'dist')
const ADMIN_DIST = path.join(PROJECT_ROOT, 'frontend-admin', 'dist')

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.webp': 'image/webp',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.txt': 'text/plain; charset=utf-8',
  '.map': 'application/json; charset=utf-8'
}

const COMPRESSIBLE = /^(text\/|application\/(json|javascript)|image\/svg)/

function mimeOf(file) {
  return MIME[path.extname(file).toLowerCase()] || 'application/octet-stream'
}

/** 把 URL 路径安全地解析到 root 目录下，阻止 ../ 越权 */
function safeResolve(root, urlPath) {
  let decoded
  try {
    decoded = decodeURIComponent(urlPath)
  } catch {
    return null
  }
  if (decoded.includes('\0')) return null
  const resolved = path.resolve(root, '.' + decoded)
  if (resolved !== root && !resolved.startsWith(root + path.sep)) return null
  return resolved
}

function sendFile(req, res, file, status = 200) {
  let stat
  try {
    stat = fs.statSync(file)
  } catch {
    return false
  }
  if (!stat.isFile()) return false

  const type = mimeOf(file)
  const headers = { 'Content-Type': type }
  // 文件名带 hash 的资源可长期缓存，index.html 必须每次校验
  const base = path.basename(file)
  headers['Cache-Control'] = base === 'index.html' ? 'no-cache' : 'public, max-age=86400'
  headers['Vary'] = 'Accept-Encoding'

  const wantsGzip = /\bgzip\b/.test(req.headers['accept-encoding'] || '')
  if (wantsGzip && COMPRESSIBLE.test(type) && stat.size > 1024) {
    res.writeHead(status, { ...headers, 'Content-Encoding': 'gzip' })
    fs.createReadStream(file).pipe(zlib.createGzip()).pipe(res)
    return true
  }

  res.writeHead(status, { ...headers, 'Content-Length': stat.size })
  fs.createReadStream(file).pipe(res)
  return true
}

/** 静态目录 + SPA fallback：找不到实体文件且浏览器要 HTML 时回退 index.html */
function serveSpa(req, res, distDir, urlPath) {
  const target = safeResolve(distDir, urlPath)
  if (!target) {
    res.writeHead(403, { 'Content-Type': 'text/plain; charset=utf-8' })
    return res.end('Forbidden')
  }
  if (serveFileOrDir(req, res, target)) return
  if ((req.headers.accept || '').includes('text/html')) {
    return sendFile(req, res, path.join(distDir, 'index.html')) || notFound(res)
  }
  notFound(res)
}

function serveFileOrDir(req, res, target) {
  if (fs.existsSync(target) && fs.statSync(target).isDirectory()) {
    return sendFile(req, res, path.join(target, 'index.html'))
  }
  return sendFile(req, res, target)
}

function notFound(res) {
  res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' })
  res.end('404 Not Found')
}

function proxyHttp(req, res) {
  const options = {
    host: BACKEND_HOST,
    port: BACKEND_PORT,
    path: req.url,
    method: req.method,
    headers: {
      ...req.headers,
      host: `${BACKEND_HOST}:${BACKEND_PORT}`,
      'x-forwarded-proto': req.headers['x-forwarded-proto'] || 'http',
      'x-forwarded-for': req.socket.remoteAddress || ''
    }
  }
  const upstream = http.request(options, (upRes) => {
    res.writeHead(upRes.statusCode, upRes.headers)
    upRes.pipe(res)
  })
  upstream.on('error', (err) => {
    console.error(`[proxy] ${req.method} ${req.url} -> ${err.message}`)
    if (!res.headersSent) {
      res.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' })
      res.end(JSON.stringify({ code: 502, message: '后端服务未启动或不可达' }))
    } else {
      res.end()
    }
  })
  req.pipe(upstream)
}

const server = http.createServer((req, res) => {
  const url = new URL(req.url, 'http://localhost')
  const pathname = url.pathname

  if (pathname === '/_health') {
    res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' })
    return res.end(JSON.stringify({ ok: true, up: Date.now() }))
  }

  if (pathname.startsWith('/api') || pathname.startsWith('/uploads')) {
    return proxyHttp(req, res)
  }

  if (pathname === '/admin' || pathname.startsWith('/admin/')) {
    // 去掉 /admin 前缀后映射到管理后台产物目录
    const stripped = pathname.slice('/admin'.length) || '/'
    return serveSpa(req, res, ADMIN_DIST, stripped)
  }

  return serveSpa(req, res, CONSUMER_DIST, pathname)
})

// WebSocket（客服聊天 /ws/chat）转发：把 upgrade 请求原样转给后端并双向 pipe
server.on('upgrade', (req, socket, head) => {
  const url = new URL(req.url, 'http://localhost')
  if (!url.pathname.startsWith('/ws')) {
    socket.destroy()
    return
  }
  socket.on('error', () => {})

  const upstream = http.request({
    host: BACKEND_HOST,
    port: BACKEND_PORT,
    path: req.url,
    method: req.method,
    headers: {
      ...req.headers,
      host: `${BACKEND_HOST}:${BACKEND_PORT}`,
      'x-forwarded-proto': req.headers['x-forwarded-proto'] || 'http'
    }
  })

  upstream.on('upgrade', (upRes, upSocket, upHead) => {
    socket.write('HTTP/1.1 101 Switching Protocols\r\n')
    for (let i = 0; i < upRes.rawHeaders.length; i += 2) {
      socket.write(`${upRes.rawHeaders[i]}: ${upRes.rawHeaders[i + 1]}\r\n`)
    }
    socket.write('\r\n')
    if (upHead && upHead.length) upSocket.unshift(upHead)
    if (head && head.length) upSocket.write(head)
    upSocket.on('error', () => {})
    upSocket.pipe(socket).pipe(upSocket)
  })

  upstream.on('response', (upRes) => {
    socket.write(`HTTP/1.1 ${upRes.statusCode} ${upRes.statusMessage}\r\n\r\n`)
    socket.destroy()
  })

  upstream.on('error', (err) => {
    console.error(`[ws] ${req.url} -> ${err.message}`)
    socket.destroy()
  })

  upstream.end()
})

for (const [name, dir] of [['消费者商城', CONSUMER_DIST], ['管理后台', ADMIN_DIST]]) {
  if (!fs.existsSync(path.join(dir, 'index.html'))) {
    console.error(`[warn] ${name} 产物缺失：${dir}，请先执行 npm run build`)
  }
}

server.listen(PORT, '127.0.0.1', () => {
  console.log(`网关已启动: http://127.0.0.1:${PORT}`)
  console.log(`  商城     /            -> ${CONSUMER_DIST}`)
  console.log(`  管理后台 /admin/      -> ${ADMIN_DIST}`)
  console.log(`  API      /api,/uploads,/ws -> http://${BACKEND_HOST}:${BACKEND_PORT}`)
})
