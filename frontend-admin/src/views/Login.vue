<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <svg class="auth-logo" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="40" height="40" rx="10" fill="#C8956C"/>
          <path d="M12 28V16C12 13.2 14.2 11 17 11H23C25.8 11 28 13.2 28 16V28" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
          <path d="M8 28H32" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>
          <circle cx="20" cy="20" r="4" stroke="#fff" stroke-width="2" fill="none"/>
          <path d="M20 17V20L22 22" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        <h2>管理后台</h2>
        <p class="auth-subtitle">临期食品销售管理平台</p>
      </div>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item><el-input v-model="form.username" placeholder="管理员账号" :prefix-icon="User" size="large" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password /></el-form-item>
        <el-form-item>
          <button type="button" class="btn-submit" @click="handleLogin" :disabled="loading">
            {{ loading ? '登录中...' : '登 录' }}
          </button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        <router-link to="/merchant-login">商家登录入口</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const router = useRouter()

async function handleLogin() {
  if (!form.username || !form.password) return ElMessage.warning('请填写完整')
  loading.value = true
  try {
    const res = await request.post('/api/admin/login', form)
    localStorage.setItem('admin_token', res.data.token)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally { loading.value = false }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #3D2B1F 0%, #6B4C3B 50%, #C8956C 100%);
}
.auth-card {
  background: #fff; padding: 48px 40px; border-radius: 16px; width: 420px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.auth-header { text-align: center; margin-bottom: 32px; }
.auth-logo { width: 48px; height: 48px; margin-bottom: 16px; }
.auth-header h2 { font-size: 24px; font-weight: 700; color: #1A1A1A; margin-bottom: 8px; }
.auth-subtitle { font-size: 13px; color: #8C8C8C; }
.btn-submit {
  width: 100%; padding: 12px; background: #C8956C; color: #fff;
  border: none; border-radius: 8px; font-size: 16px; font-weight: 600;
  cursor: pointer; transition: background 0.2s; letter-spacing: 2px;
}
.btn-submit:hover { background: #8B5E3C; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
.auth-footer { text-align: center; margin-top: 20px; font-size: 13px; }
.auth-footer a { color: #C8956C; font-weight: 500; }
.auth-footer a:hover { color: #8B5E3C; }
</style>
