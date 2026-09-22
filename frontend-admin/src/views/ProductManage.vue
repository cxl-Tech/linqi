<template>
  <div>
    <div class="page-title">商品管理</div>
    <el-card>
      <div class="list-filter-bar">
        <el-input v-model="keyword" placeholder="搜索商品名称" clearable prefix-icon="Search" />
        <el-select v-model="auditFilter" placeholder="审核状态" clearable @change="onFilterChange">
          <el-option label="待审" :value="0" /><el-option label="通过" :value="1" /><el-option label="拒绝" :value="2" />
        </el-select>
        <el-select v-model="expiryFilter" placeholder="临期状态" clearable @change="onFilterChange">
          <el-option label="正常" :value="0" /><el-option label="临期" :value="1" /><el-option label="过期" :value="2" />
        </el-select>
      </div>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="name" label="商品名" />
        <el-table-column prop="price" label="价格"><template #default="{row}"><span style="color:#E53935">¥{{ row.price }}</span></template></el-table-column>
        <el-table-column prop="stock" label="库存" />
        <el-table-column label="临期状态"><template #default="{row}"><el-tag :type="['success','warning','danger'][row.expiryStatus]" size="small">{{ ['正常','临期','过期'][row.expiryStatus] }}</el-tag></template></el-table-column>
        <el-table-column label="审核"><template #default="{row}"><el-tag :type="['warning','success','danger'][row.auditStatus]" size="small">{{ ['待审','通过','拒绝'][row.auditStatus] }}</el-tag></template></el-table-column>
        <el-table-column label="操作">
          <template #default="{row}">
            <el-button v-if="row.auditStatus===0" type="success" size="small" @click="auditProduct(row.id,1)">通过</el-button>
            <el-button v-if="row.auditStatus===0" type="danger" size="small" @click="auditProduct(row.id,2)">拒绝</el-button>
            <el-switch :model-value="row.status===1" @change="toggleStatus(row.id)" size="small" style="margin-left:8px" />
            <el-button type="danger" size="small" link @click="delProduct(row.id)" style="margin-left:8px">删除</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
const list = ref([]), total = ref(0), current = ref(1), loading = ref(false), keyword = ref(''), auditFilter = ref(null), expiryFilter = ref(null)
let timer = null
watch(keyword, () => { clearTimeout(timer); timer = setTimeout(() => { current.value = 1; loadData() }, 300) })
function onFilterChange() { current.value = 1; loadData() }
async function loadData() {
  loading.value = true
  try {
    const params = { current: current.value, size: 10 }
    if (keyword.value) params.keyword = keyword.value
    if (auditFilter.value != null) params.auditStatus = auditFilter.value
    if (expiryFilter.value != null) params.expiryStatus = expiryFilter.value
    const r = await request.get('/api/admin/product/list', { params })
    list.value = r.data?.records || []; total.value = r.data?.total || 0
  } finally { loading.value = false }
}
async function auditProduct(id, s) { await request.put(`/api/admin/product/audit/${id}?auditStatus=${s}`); ElMessage.success('操作成功'); loadData() }
async function toggleStatus(id) { await request.put(`/api/admin/product/status/${id}`); ElMessage.success('操作成功'); loadData() }
async function delProduct(id) { await ElMessageBox.confirm('确认删除？'); await request.delete(`/api/admin/product/${id}`); ElMessage.success('已删除'); loadData() }
onMounted(loadData)
</script>
