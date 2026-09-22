<template>
  <div>
    <div class="page-title">售后管理</div>
    <el-card>
      <div class="list-filter-bar">
        <el-select v-model="statusFilter" placeholder="处理状态" clearable @change="loadData">
          <el-option label="待处理" :value="0" />
          <el-option label="已同意" :value="1" />
          <el-option label="已拒绝" :value="2" />
          <el-option label="已完成" :value="4" />
        </el-select>
      </div>
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column label="用户ID" width="80" prop="userId" />
        <el-table-column label="申请次序" width="100">
          <template #default="{row}">
            <el-tag v-if="row.retryIndex > 1" type="danger" size="small">第{{ row.retryIndex }}次</el-tag>
            <el-tag v-else type="info" size="small">首次</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{row}">
            <el-tag size="small">{{ ['','退款','退货退款','换货'][row.type] || '未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="售后原因" show-overflow-tooltip />
        <el-table-column prop="description" label="详细描述" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="[,'success','danger','warning','info'][row.status] || 'warning'" size="small">
              {{ ['待处理','已同意','已拒绝','管理员介入','已完成'][row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商家回复" show-overflow-tooltip>
          <template #default="{row}">{{ row.merchantReply || '-' }}</template>
        </el-table-column>
        <el-table-column label="管理员回复" show-overflow-tooltip>
          <template #default="{row}">{{ row.adminReply || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button size="small" type="success" @click="handleApprove(row)" :disabled="row.status===4">同意</el-button>
            <el-button size="small" type="danger" @click="showReject(row)" :disabled="row.status===4">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="currentPage" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="rejectVisible" title="拒绝售后" width="420px">
      <el-input v-model="rejectReply" type="textarea" rows="3" placeholder="请输入拒绝理由" />
      <template #footer>
        <el-button @click="rejectVisible=false">取消</el-button>
        <el-button type="danger" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([]), total = ref(0), currentPage = ref(1)
const statusFilter = ref(null)
const rejectVisible = ref(false), rejectId = ref(null), rejectReply = ref('')

async function loadData() {
  const params = { current: currentPage.value, size: 10 }
  if (statusFilter.value != null) params.status = statusFilter.value
  const r = await request.get('/api/admin/aftersale/list', { params })
  list.value = r.data?.records || []
  total.value = r.data?.total || 0
}

async function handleApprove(row) {
  await ElMessageBox.confirm('确认同意此售后申请？管理员权限高于商家，操作不可撤销。', '确认同意')
  await request.put(`/api/admin/aftersale/${row.id}`, { status: '1', adminReply: '管理员已同意' })
  ElMessage.success('已同意')
  loadData()
}

function showReject(row) {
  rejectId.value = row.id
  rejectReply.value = ''
  rejectVisible.value = true
}

async function handleReject() {
  if (!rejectReply.value) return ElMessage.warning('请输入拒绝理由')
  await request.put(`/api/admin/aftersale/${rejectId.value}`, { status: '2', adminReply: rejectReply.value })
  ElMessage.success('已拒绝')
  rejectVisible.value = false
  loadData()
}

onMounted(loadData)
</script>
