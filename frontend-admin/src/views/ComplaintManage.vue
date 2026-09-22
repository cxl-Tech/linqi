<template>
  <div>
    <div class="page-title">投诉管理</div>
    <el-card>
      <div class="list-filter-bar">
        <el-select v-model="statusFilter" placeholder="处理状态" clearable @change="loadData">
          <el-option label="待处理" :value="0" />
          <el-option label="已处理" :value="1" />
        </el-select>
      </div>
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="merchantId" label="商家ID" width="80" />
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column label="投诉类型" width="110">
          <template #default="{row}">
            <el-tag :type="['','danger','danger','warning','info'][row.type]" size="small">
              {{ ['','态度恶劣','辱骂消费者','虚假宣传','其他'][row.type] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="投诉内容" show-overflow-tooltip />
        <el-table-column label="截图" width="100">
          <template #default="{row}">
            <template v-if="row.images">
              <el-image :src="row.images.split(',')[0]" class="complaint-thumb" fit="cover" :preview-src-list="row.images.split(',')" />
            </template>
            <span v-else style="color:#999">无</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status===0 ? 'warning' : 'success'" size="small">{{ row.status===0 ? '待处理' : '已处理' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="adminReply" label="处理回复" show-overflow-tooltip />
        <el-table-column prop="createTime" label="投诉时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status===0" size="small" type="primary" @click="openHandle(row)">处理</el-button>
            <span v-else style="font-size:12px;color:#999">已处理</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="currentPage" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="handleVisible" title="处理投诉" width="450px">
      <div style="margin-bottom:12px;font-size:13px;color:#666">
        <div><b>投诉类型：</b>{{ ['','态度恶劣','辱骂消费者','虚假宣传','其他'][handleRow?.type] }}</div>
        <div style="margin-top:6px"><b>投诉内容：</b>{{ handleRow?.content }}</div>
      </div>
      <el-input v-model="adminReply" type="textarea" rows="3" placeholder="请输入处理回复..." />
      <template #footer>
        <el-button @click="handleVisible=false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const list = ref([]), total = ref(0), currentPage = ref(1)
const statusFilter = ref(null)
const handleVisible = ref(false), handleRow = ref(null), adminReply = ref('')

async function loadData() {
  const params = { current: currentPage.value, size: 10 }
  if (statusFilter.value != null) params.status = statusFilter.value
  const r = await request.get('/api/admin/complaint/list', { params })
  list.value = r.data?.records || []
  total.value = r.data?.total || 0
}

function openHandle(row) {
  handleRow.value = row
  adminReply.value = ''
  handleVisible.value = true
}

async function submitHandle() {
  if (!adminReply.value) return ElMessage.warning('请输入处理回复')
  await request.put(`/api/admin/complaint/${handleRow.value.id}`, { adminReply: adminReply.value })
  ElMessage.success('处理成功')
  handleVisible.value = false
  loadData()
}

onMounted(loadData)
</script>
<style scoped>
.complaint-thumb { width: 40px; height: 40px; border-radius: 4px; cursor: pointer; }
</style>
