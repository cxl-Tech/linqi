<template>
  <div>
    <div class="page-title">订单管理</div>
    <el-card>
      <div class="list-filter-bar">
        <el-input v-model="keyword" placeholder="搜索订单号/收货人/电话" clearable prefix-icon="Search" />
      </div>
      <el-tabs v-model="activeTab" @tab-change="loadData">
        <el-tab-pane label="全部" name="" /><el-tab-pane label="待发货" name="1" /><el-tab-pane label="待收货" name="2" /><el-tab-pane label="已完成" name="3" /><el-tab-pane label="售后中" name="5" />
      </el-tabs>
      <el-table :data="filteredList" stripe>
        <el-table-column prop="orderNo" label="订单编号" />
        <el-table-column prop="receiverName" label="收货人" />
        <el-table-column prop="receiverPhone" label="电话" />
        <el-table-column prop="totalAmount" label="金额"><template #default="{row}">¥{{ row.totalAmount }}</template></el-table-column>
        <el-table-column label="状态">
          <template #default="{row}"><el-tag :type="statusType(row.orderStatus)" size="small">{{ statusText(row.orderStatus) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="物流">
          <template #default="{row}"><el-tag v-if="row.expressNo" size="small" type="success">已发货</el-tag><span v-else style="color:#999;font-size:12px">-</span></template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" />
        <el-table-column label="操作">
          <template #default="{row}"><el-button v-if="row.orderStatus===1" size="small" type="primary" @click="showShipDialog(row)">发货</el-button><el-button v-if="row.orderStatus===5" size="small" type="warning" @click="$router.push('/merchant/aftersale')">去处理</el-button></template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="currentPage" @current-change="loadData" />
    </el-card>
    <el-dialog v-model="shipVisible" title="填写物流信息" width="420px">
      <el-form :model="shipForm" label-width="90px">
        <el-form-item label="快递公司">
          <el-select v-model="shipForm.expressCompany" placeholder="请选择快递公司" style="width:100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="极兔速递" value="极兔速递" />
            <el-option label="邮政EMS" value="邮政EMS" />
            <el-option label="京东物流" value="京东物流" />
            <el-option label="德邦快递" value="德邦快递" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号"><el-input v-model="shipForm.expressNo" placeholder="物流单号" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="shipVisible=false">取消</el-button><el-button type="primary" @click="ship">确认发货</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
const list = ref([]), total = ref(0), currentPage = ref(1), activeTab = ref(''), keyword = ref('')
const statusTextArr = ['待付款','待发货','待收货','已完成','已取消','售后中']
const statusTypeArr = ['warning','primary','','success','info','danger']
function statusText(s) { return statusTextArr[s] || '未知' }
function statusType(s) { return statusTypeArr[s] || 'info' }
const filteredList = computed(() => { if (!keyword.value) return list.value; const k = keyword.value.toLowerCase(); return list.value.filter(i => i.orderNo?.toLowerCase().includes(k) || i.receiverName?.toLowerCase().includes(k) || i.receiverPhone?.includes(k)) })
const shipVisible = ref(false), shipOrderId = ref(null), shipForm = reactive({ expressCompany: '', expressNo: '' })
async function loadData() {
  const params = { current: currentPage.value, size: 10 }; if (activeTab.value) params.orderStatus = activeTab.value
  const r = await request.get('/api/order/merchant/list', { params }); list.value = r.data?.records || []; total.value = r.data?.total || 0
}
function showShipDialog(row) { shipOrderId.value = row.id; shipForm.expressCompany = ''; shipForm.expressNo = ''; shipVisible.value = true }
async function ship() {
  if (!shipForm.expressCompany || !shipForm.expressNo) return ElMessage.warning('请填写完整')
  await request.put(`/api/order/merchant/ship/${shipOrderId.value}`, shipForm); ElMessage.success('发货成功'); shipVisible.value = false; loadData()
}
onMounted(loadData)
</script>
