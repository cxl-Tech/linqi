<template>
  <div>
    <div class="page-title">店铺设置</div>
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card>
          <template #header><span style="font-weight:600;font-size:15px">店铺信息</span></template>
          <el-form :model="form" label-width="90px">
            <el-form-item label="用户名"><el-input :value="form.username" disabled /></el-form-item>
            <el-form-item label="店铺名称"><el-input v-model="form.shopName" /></el-form-item>
            <el-form-item label="联系人"><el-input v-model="form.contact" /></el-form-item>
            <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
            <el-form-item label="店铺简介"><el-input v-model="form.description" type="textarea" rows="3" /></el-form-item>
            <el-form-item><el-button type="primary" @click="save">保存</el-button></el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header><span>修改密码</span></template>
          <el-form :model="pwdForm" label-width="90px">
            <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
            <el-form-item><el-button type="warning" @click="changePwd">修改密码</el-button></el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { reactive, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
const form = reactive({ username: '', shopName: '', contact: '', phone: '', description: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '' })
onMounted(async () => { const r = await request.get('/api/merchant/info'); Object.assign(form, r.data) })
async function save() { await request.put('/api/merchant/update', form); ElMessage.success('保存成功') }
async function changePwd() { if (!pwdForm.oldPassword || !pwdForm.newPassword) return ElMessage.warning('请填写完整'); await request.put('/api/merchant/password', pwdForm); ElMessage.success('密码修改成功'); pwdForm.oldPassword = ''; pwdForm.newPassword = '' }
</script>
