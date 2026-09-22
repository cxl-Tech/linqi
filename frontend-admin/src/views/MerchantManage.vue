<template>
  <div>
    <div class="page-title">商家管理</div>
    <el-card>
      <div class="list-filter-bar">
        <el-input v-model="keyword" placeholder="搜索店铺名/账号/联系人" clearable prefix-icon="Search" />
        <el-select v-model="auditFilter" placeholder="审核状态" clearable @change="onFilterChange">
          <el-option label="待审核" :value="0" /><el-option label="已通过" :value="1" /><el-option label="未通过" :value="2" />
        </el-select>
      </div>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="username" label="账号" />
        <el-table-column prop="shopName" label="店铺名" />
        <el-table-column prop="contact" label="联系人" />
        <el-table-column label="审核状态"><template #default="{row}"><el-tag :type="['warning','success','danger'][row.auditStatus]">{{ ['待审核','已通过','未通过'][row.auditStatus] }}</el-tag></template></el-table-column>
        <el-table-column label="状态"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'正常':'禁用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作">
          <template #default="{row}">
            <el-button v-if="row.auditStatus===0" type="success" size="small" @click="audit(row.id,1)">通过</el-button>
            <el-button v-if="row.auditStatus===0" type="danger" size="small" @click="audit(row.id,2)">拒绝</el-button>
            <el-switch :model-value="row.status===1" @change="toggleStatus(row.id)" style="margin-left:8px" />
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px;text-align:right"><el-pagination background layout="prev,pager,next" :total="total" :page-size="10" v-model:current-page="current" @current-change="loadData" /></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
const list = ref([]), total = ref(0), current = ref(1), loading = ref(false), keyword = ref(''), auditFilter = ref(null)
let timer = null
watch(keyword, () => { clearTimeout(timer); timer = setTimeout(() => { current.value = 1; loadData() }, 300) })
function onFilterChange() { current.value = 1; loadData() }
async function loadData() {
  loading.value = true
  try {
    const params = { current: current.value, size: 10 }
    if (keyword.value) params.keyword = keyword.value
    if (auditFilter.value != null) params.auditStatus = auditFilter.value
    const r = await request.get('/api/admin/merchant/list', { params })
    list.value = r.data?.records || []; total.value = r.data?.total || 0
  } finally { loading.value = false }
}
async function audit(id, status) { await request.put(`/api/admin/merchant/audit/${id}?auditStatus=${status}`); ElMessage.success('操作成功'); loadData() }
async function toggleStatus(id) { await request.put(`/api/admin/merchant/status/${id}`); ElMessage.success('操作成功'); loadData() }
onMounted(loadData)
</script>
