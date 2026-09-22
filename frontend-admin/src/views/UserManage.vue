<template>
  <div>
    <div class="page-title">用户管理</div>
    <el-card>
      <div class="list-filter-bar">
        <el-input v-model="keyword" placeholder="搜索用户名/昵称/手机号" clearable prefix-icon="Search" />
        <el-select v-model="statusFilter" placeholder="用户状态" clearable @change="onFilterChange">
          <el-option label="正常" :value="1" /><el-option label="禁用" :value="0" />
        </el-select>
      </div>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column label="状态"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'正常':'禁用' }}</el-tag></template></el-table-column>
        <el-table-column prop="createTime" label="注册时间" />
        <el-table-column label="操作"><template #default="{row}"><el-switch :model-value="row.status===1" @change="toggleStatus(row.id)" /></template></el-table-column>
      </el-table>
      <div style="margin-top:16px;text-align:right"><el-pagination background layout="prev,pager,next,total" :total="total" :page-size="10" v-model:current-page="current" @current-change="loadData" /></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const list = ref([]), total = ref(0), current = ref(1), keyword = ref(''), loading = ref(false), statusFilter = ref(null)
let timer = null
watch(keyword, () => { clearTimeout(timer); timer = setTimeout(() => { current.value = 1; loadData() }, 300) })
function onFilterChange() { current.value = 1; loadData() }

async function loadData() {
  loading.value = true
  try {
    const params = { current: current.value, size: 10 }
    if (keyword.value) params.keyword = keyword.value
    if (statusFilter.value != null) params.status = statusFilter.value
    const r = await request.get('/api/admin/user/list', { params })
    list.value = r.data?.records || []; total.value = r.data?.total || 0
  } finally { loading.value = false }
}
async function toggleStatus(id) { await request.put(`/api/admin/user/status/${id}`); ElMessage.success('操作成功'); loadData() }

onMounted(loadData)
</script>
