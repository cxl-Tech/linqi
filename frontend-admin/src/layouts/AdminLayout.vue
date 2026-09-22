<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside class="sidebar" width="240px">
      <div class="sidebar-brand" @click="router.push('/dashboard')">
        <svg class="brand-logo" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="40" height="40" rx="10" fill="#C8956C"/>
          <path d="M12 28V16C12 13.2 14.2 11 17 11H23C25.8 11 28 13.2 28 16V28" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
          <path d="M8 28H32" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
          <circle cx="20" cy="20" r="4" stroke="#fff" stroke-width="2" fill="none"/>
          <path d="M20 17V20L22 22" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        <div class="brand-text">
          <span class="brand-name">临期优品</span>
          <span class="brand-role">管理后台</span>
        </div>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="transparent"
        text-color="rgba(255,255,255,0.65)"
        active-text-color="#C8956C"
        class="sidebar-menu"
      >
        <el-menu-item v-for="r in menuRoutes" :key="r.path" :index="'/' + r.path">
          <el-icon><component :is="r.meta?.icon || 'Document'" /></el-icon>
          <span>{{ r.meta?.title }}</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <div class="sidebar-footer-text">临期食品销售管理平台 v1.0</div>
      </div>
    </el-aside>
    <!-- 右侧 -->
    <el-container class="main-container">
      <el-header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCmd" trigger="click">
            <div class="user-info">
              <div class="user-avatar">
                <el-icon :size="18"><User /></el-icon>
              </div>
              <span class="user-name">管理员</span>
              <el-icon class="arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="settings"><el-icon><Setting /></el-icon>系统设置</el-dropdown-item>
                <el-dropdown-item command="logout" divided><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="admin-main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const menuRoutes = computed(() => router.options.routes.find(r => r.path === '/')?.children || [])
const currentTitle = computed(() => {
  const matched = route.matched.find(r => r.meta?.title)
  return matched?.meta?.title || ''
})

function handleCmd(cmd) {
  if (cmd === 'logout') { localStorage.removeItem('admin_token'); router.push('/login') }
  else if (cmd === 'settings') { router.push('/settings') }
}
</script>

<style scoped>
.admin-layout { height: 100vh; }

/* ===== 侧边栏 ===== */
.sidebar {
  background: var(--admin-sidebar-bg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.brand-logo { width: 38px; height: 38px; flex-shrink: 0; }
.brand-text { display: flex; flex-direction: column; }
.brand-name { color: #fff; font-size: 18px; font-weight: 700; letter-spacing: 2px; line-height: 1.2; }
.brand-role { color: rgba(255,255,255,0.45); font-size: 11px; letter-spacing: 1px; margin-top: 2px; }

.sidebar-menu {
  flex: 1;
  border-right: none !important;
  padding: 8px 0;
  overflow-y: auto;
}
.sidebar-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 2px 8px;
  border-radius: 8px;
  padding-left: 20px !important;
  transition: all 0.2s;
}
.sidebar-menu .el-menu-item:hover {
  background: var(--admin-sidebar-hover) !important;
}
.sidebar-menu .el-menu-item.is-active {
  background: var(--admin-sidebar-active) !important;
  color: var(--admin-sidebar-text-active) !important;
  font-weight: 600;
  position: relative;
}
.sidebar-menu .el-menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  border-radius: 0 3px 3px 0;
  background: var(--admin-primary);
}

.sidebar-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(255,255,255,0.08);
}
.sidebar-footer-text { font-size: 11px; color: rgba(255,255,255,0.25); text-align: center; }

/* ===== Header ===== */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--admin-bg-white);
  border-bottom: 1px solid var(--admin-border-light);
  padding: 0 24px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}
.header-left { display: flex; align-items: center; }
.header-right { display: flex; align-items: center; }
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}
.user-info:hover { background: var(--admin-primary-bg); }
.user-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: var(--admin-primary-light);
  color: var(--admin-primary-dark);
  display: flex; align-items: center; justify-content: center;
}
.user-name { font-size: 14px; font-weight: 500; color: var(--admin-text-primary); }
.arrow { color: var(--admin-text-secondary); font-size: 12px; }

/* ===== Main ===== */
.admin-main {
  background: var(--admin-bg-page);
  padding: 24px;
  overflow-y: auto;
}
</style>
