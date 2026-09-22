<template>
  <div>
    <div class="page-title">售后管理</div>
    <el-card>
      <div class="list-filter-bar">
        <el-input v-model="keyword" placeholder="搜索订单编号/原因" clearable prefix-icon="Search" />
        <el-select v-model="statusFilter" placeholder="处理状态" clearable @change="onFilterChange">
          <el-option label="待处理" :value="0" /><el-option label="已同意" :value="1" /><el-option label="已拒绝" :value="2" /><el-option label="已完成" :value="4" />
        </el-select>
      </div>
      <el-table :data="filteredList" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="订单编号" width="160">
          <template #default="{row}">{{ row.orderNo || row.orderId }}</template>
        </el-table-column>
        <el-table-column label="申请次序" width="100">
          <template #default="{row}">
            <el-tag v-if="row.retryIndex > 1" type="danger" size="small">第{{ row.retryIndex }}次申请</el-tag>
            <el-tag v-else type="info" size="small">首次申请</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100"><template #default="{row}"><el-tag size="small">{{ ['','退款','退货退款','换货'][row.type] || '未知' }}</el-tag></template></el-table-column>
        <el-table-column prop="reason" label="售后原因" show-overflow-tooltip />
        <el-table-column prop="description" label="详细描述" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{row}"><el-tag :type="['warning','success','danger','','info'][row.status]" size="small">{{ ['待处理','已同意','已拒绝','管理员介入','已完成'][row.status] }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="160">
          <template #default="{row}">
            <template v-if="row.status===0">
              <el-button size="small" type="success" @click="handle(row.id, 1, '')">同意</el-button>
              <el-button size="small" type="danger" @click="showReject(row.id)">拒绝</el-button>
            </template>
            <span v-else-if="row.merchantReply" style="font-size:12px;color:#999">已回复</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="currentPage" @current-change="loadData" />
    </el-card>
    <el-dialog v-model="rejectVisible" title="拒绝理由" width="420px">
      <el-input v-model="rejectReply" type="textarea" rows="3" placeholder="请输入拒绝理由" />
      <template #footer><el-button @click="rejectVisible=false">取消</el-button><el-button type="danger" @click="handle(rejectId, 2, rejectReply); rejectVisible=false">确认拒绝</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
const list = ref([]), total = ref(0), currentPage = ref(1), rejectVisible = ref(false), rejectId = ref(null), rejectReply = ref('')
const keyword = ref(''), statusFilter = ref(null)
const filteredList = computed(() => {
  let data = list.value
  if (statusFilter.value != null) data = data.filter(i => i.status === statusFilter.value)
  if (keyword.value) { const k = keyword.value.toLowerCase(); data = data.filter(i => String(i.orderNo || i.orderId).toLowerCase().includes(k) || i.reason?.toLowerCase().includes(k)) }
  return data
})
function onFilterChange() { currentPage.value = 1; loadData() }
async function loadData() {
  const r = await request.get('/api/merchant/aftersale/list', { params: { current: currentPage.value, size: 10 } })
  const records = r.data?.records || []
  // 加载关联订单编号
  for (const item of records) {
    if (item.orderId && !item.orderNo) {
      try { const o = await request.get(`/api/order/merchant/detail/${item.orderId}`); item.orderNo = o.data?.orderNo } catch {}
    }
  }
  list.value = records; total.value = r.data?.total || 0
}
function showReject(id) { rejectId.value = id; rejectReply.value = ''; rejectVisible.value = true }
async function handle(id, status, reply) { await request.put(`/api/merchant/aftersale/${id}`, { status: String(status), merchantReply: reply || '' }); ElMessage.success('操作成功'); loadData() }
onMounted(loadData)
</script>
