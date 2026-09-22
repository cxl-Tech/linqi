<template>
  <div class="consumer-layout">
    <!-- 顶部工具栏 -->
    <div class="top-bar">
      <div class="top-bar-inner">
        <span class="top-welcome">欢迎来到临期优品</span>
        <div class="top-links">
          <template v-if="userStore.isLoggedIn()">
            <router-link to="/orders">我的订单</router-link>
            <span class="divider">|</span>
            <router-link to="/chat" class="msg-link">消息<span v-if="unreadCount > 0" class="top-badge">{{ unreadCount }}</span></router-link>
            <span class="divider">|</span>
            <router-link to="/profile">个人中心</router-link>
            <span class="divider">|</span>
            <a @click="handleCmd('logout')" style="cursor:pointer">退出</a>
          </template>
          <template v-else>
            <router-link to="/login">登录</router-link>
            <span class="divider">|</span>
            <router-link to="/register">免费注册</router-link>
          </template>
        </div>
      </div>
    </div>

    <!-- 主导航 -->
    <header class="header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/')">
          <svg class="logo-icon" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="10" fill="#C8956C"/>
            <path d="M12 28V16C12 13.2 14.2 11 17 11H23C25.8 11 28 13.2 28 16V28" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
            <path d="M8 28H32" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
            <circle cx="20" cy="20" r="4" stroke="#fff" stroke-width="2" fill="none"/>
            <path d="M20 17V20L22 22" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <div class="logo-text">
            <span class="brand-name">临期优品</span>
            <span class="brand-slogan">品质生活 · 聪明之选</span>
          </div>
        </div>

        <div class="search-bar">
          <input v-model="searchKeyword" type="text" placeholder="搜索商品、品牌、品类..." @keyup.enter="doSearch" />
          <button class="search-btn" @click="doSearch">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          </button>
        </div>

        <div class="header-actions">
          <router-link to="/cart" class="action-item cart-action">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg>
            <span>购物车</span>
          </router-link>
          <router-link to="/profile" class="action-item" v-if="userStore.isLoggedIn()">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            <span>{{ userStore.userInfo.nickname || '我的' }}</span>
          </router-link>
        </div>
      </div>
    </header>

    <!-- 导航条 -->
    <nav class="nav-bar">
      <div class="nav-bar-inner">
        <router-link to="/" class="nav-link" :class="{ active: $route.path === '/' }">首页</router-link>
        <router-link to="/products" class="nav-link" :class="{ active: $route.path === '/products' && !$route.query.expiryStatus && !$route.query.sortField }">全部商品</router-link>
        <router-link to="/products?expiryStatus=1" class="nav-link" :class="{ active: $route.query.expiryStatus === '1' }">临期特惠</router-link>
        <router-link to="/products?sortField=sales&sortOrder=desc" class="nav-link" :class="{ active: $route.query.sortField === 'sales' }">热销排行</router-link>
      </div>
    </nav>

    <main class="main-content">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-col">
          <h4>关于我们</h4>
          <p>临期优品致力于减少食品浪费，为消费者提供安全、实惠的临期食品购买渠道。</p>
        </div>
        <div class="footer-col">
          <h4>服务保障</h4>
          <p>正品保障 · 品质检测</p>
          <p>7天无理由退换</p>
          <p>安全支付</p>
        </div>
        <div class="footer-col">
          <h4>帮助中心</h4>
          <p>常见问题</p>
          <p>配送说明</p>
          <p>售后政策</p>
        </div>
        <div class="footer-col">
          <h4>联系我们</h4>
          <p>客服热线：400-888-0000</p>
          <p>工作时间：9:00 - 21:00</p>
        </div>
      </div>
      <div class="footer-bottom">
        <p>© 2024 临期优品平台 · 减少浪费，优享生活 · 严选品质好货</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const userStore = useUserStore()
const router = useRouter()
const searchKeyword = ref('')
const unreadCount = ref(0)

async function loadUnread() {
  if (!userStore.isLoggedIn()) return
  try { const r = await request.get('/api/chat/unread'); unreadCount.value = r.data || 0 } catch {}
}
onMounted(() => { loadUnread(); setInterval(loadUnread, 30000) })

function handleCmd(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/profile')
  }
}

function doSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/products', query: { keyword: searchKeyword.value.trim() } })
  }
}
</script>

<style scoped>
.consumer-layout { min-height: 100vh; display: flex; flex-direction: column; }

/* ===== 固定顶部区域 ===== */
.top-bar, .header, .nav-bar {
  position: sticky; z-index: 100;
}
.top-bar { top: 0; background: var(--color-primary-dark); z-index: 102; }
.header { top: 36px; background: var(--bg-white); z-index: 101; }
.nav-bar { top: 116px; z-index: 100; }

.top-bar-inner {
  max-width: 100%; margin: 0 auto; padding: 0 32px;
  display: flex; justify-content: space-between; align-items: center;
  height: 36px; font-size: 12px; color: rgba(255,255,255,0.8);
}
.top-links { display: flex; align-items: center; gap: 0; }
.top-links a { color: rgba(255,255,255,0.85); transition: color 0.2s; padding: 0 8px; }
.top-links a:hover { color: #fff; }
.divider { color: rgba(255,255,255,0.3); margin: 0 2px; }
.msg-link { position: relative; }
.top-badge {
  position: absolute; top: -8px; right: -12px;
  min-width: 16px; height: 16px; border-radius: 8px;
  background: #E53935; color: #fff; font-size: 10px;
  display: flex; align-items: center; justify-content: center; padding: 0 4px;
}

/* ===== 主 Header ===== */
.header { border-bottom: 1px solid var(--border-light); }
.header-inner {
  max-width: 100%; margin: 0 auto; padding: 0 32px;
  display: flex; align-items: center; height: 80px; gap: 40px;
}

.logo { display: flex; align-items: center; gap: 12px; cursor: pointer; flex-shrink: 0; }
.logo-icon { width: 44px; height: 44px; }
.logo-text { display: flex; flex-direction: column; }
.brand-name { font-size: 22px; font-weight: 800; color: var(--color-primary-dark); letter-spacing: 2px; line-height: 1.2; }
.brand-slogan { font-size: 11px; color: var(--text-secondary); letter-spacing: 1px; margin-top: 2px; }

.search-bar {
  flex: 1; max-width: 600px; display: flex; align-items: center;
  border: 2px solid var(--color-primary); border-radius: 24px;
  overflow: hidden; background: var(--bg-white); transition: box-shadow 0.3s;
}
.search-bar:focus-within { box-shadow: 0 0 0 3px rgba(200,149,108,0.15); }
.search-bar input {
  flex: 1; border: none; outline: none; padding: 10px 20px;
  font-size: 14px; color: var(--text-primary); background: transparent;
}
.search-bar input::placeholder { color: var(--text-placeholder); }
.search-btn {
  background: var(--color-primary); border: none; color: #fff; cursor: pointer;
  padding: 10px 20px; display: flex; align-items: center; justify-content: center;
  transition: background 0.2s;
}
.search-btn:hover { background: var(--color-primary-dark); }

.header-actions { display: flex; align-items: center; gap: 24px; flex-shrink: 0; }
.action-item {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  color: var(--text-regular); font-size: 12px; cursor: pointer; transition: color 0.2s;
}
.action-item:hover { color: var(--color-primary); }
.action-item svg { stroke: currentColor; }

/* ===== 导航条 ===== */
.nav-bar { background: var(--bg-white); border-bottom: 2px solid var(--border-light); box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.nav-bar-inner {
  max-width: 100%; margin: 0 auto; padding: 0 32px;
  display: flex; align-items: center; height: 44px; gap: 0;
}
.nav-link {
  padding: 0 28px; height: 44px; display: flex; align-items: center;
  font-size: 15px; font-weight: 500; color: var(--text-regular);
  border-bottom: 2px solid transparent; transition: all 0.2s;
  margin-bottom: -2px; white-space: nowrap;
}
.nav-link:hover { color: var(--color-primary-dark); }
.nav-link.active {
  color: var(--color-primary-dark); border-bottom-color: var(--color-primary);
  font-weight: 600;
}

/* ===== 主内容 ===== */
.main-content { flex: 1; width: 100%; }

/* ===== 页脚 ===== */
.footer { background: #2C2017; color: rgba(255,255,255,0.7); margin-top: 40px; }
.footer-inner {
  max-width: 100%; margin: 0 auto; padding: 40px 32px;
  display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; gap: 40px;
}
.footer-col h4 { color: #fff; font-size: 16px; margin-bottom: 16px; font-weight: 600; }
.footer-col p { font-size: 13px; line-height: 2; color: rgba(255,255,255,0.55); }
.footer-bottom {
  border-top: 1px solid rgba(255,255,255,0.1);
  text-align: center; padding: 16px 32px; font-size: 12px;
  color: rgba(255,255,255,0.35);
}
</style>
