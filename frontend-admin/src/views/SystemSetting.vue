<template>
  <div>
    <div class="page-title">系统设置</div>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span style="font-weight:600">修改管理员密码</span></template>
          <el-form :model="pwdForm" label-width="100px" style="max-width:400px">
            <el-form-item label="当前密码">
              <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updatePassword" :loading="pwdLoading">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span style="font-weight:600">平台信息</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="平台名称">临期食品销售管理平台</el-descriptions-item>
            <el-descriptions-item label="系统版本">v1.0.0</el-descriptions-item>
            <el-descriptions-item label="技术栈">Spring Boot + Vue 3 + Element Plus</el-descriptions-item>
            <el-descriptions-item label="数据库">MySQL 8.0</el-descriptions-item>
            <el-descriptions-item label="管理员账号">admin</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdLoading = ref(false)

async function updatePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) return ElMessage.warning('请填写完整')
  if (pwdForm.newPassword.length < 6) return ElMessage.warning('新密码不能少于6位')
  if (pwdForm.newPassword !== pwdForm.confirmPassword) return ElMessage.warning('两次输入的密码不一致')
  pwdLoading.value = true
  try {
    await request.put('/api/admin/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    localStorage.removeItem('admin_token')
    router.push('/login')
  } finally { pwdLoading.value = false }
}
</script>
