<template>
  <div>
    <div class="page-title">订单管理</div>
    <el-card>
      <div class="list-filter-bar">
        <el-input v-model="keyword" placeholder="搜索订单号/收货人" clearable prefix-icon="Search" />
      </div>
      <el-tabs v-model="activeStatus" @tab-change="onTabChange" style="margin-bottom:16px">
        <el-tab-pane label="全部订单" name="all" />
        <el-tab-pane label="待付款" name="0" />
        <el-tab-pane label="待发货" name="1" />
        <el-tab-pane label="待收货" name="2" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已取消" name="4" />
      </el-tabs>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="orderNo" label="订单号" />
        <el-table-column prop="receiverName" label="收货人" />
        <el-table-column prop="receiverPhone" label="联系电话" />
        <el-table-column prop="totalAmount" label="金额">
          <template #default="{row}"><span style="color:#E53935;font-weight:600">¥{{ row.totalAmount }}</span></template>
        </el-table-column>
        <el-table-column label="支付状态">
          <template #default="{row}"><el-tag :type="row.payStatus===1?'success':'info'" size="small">{{ row.payStatus===1?'已付':'未付' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="订单状态">
          <template #default="{row}">
            <el-tag :type="statusTypes[row.orderStatus]" size="small">{{ statusLabels[row.orderStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作">
          <template #default="{row}">
            <el-button type="primary" size="small" link @click="showDetail(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px;text-align:right">
        <el-pagination background layout="prev,pager,next,total" :total="total" :page-size="10" v-model:current-page="current" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 订单详情抽屉 -->
    <el-drawer v-model="detailVisible" title="订单详情" size="620px" :destroy-on-close="true">
      <div v-if="detail" class="drawer-body">
        <!-- 基本信息 -->
        <div class="section-title">基本信息</div>
        <el-descriptions :column="2" border size="small" class="section-block">
          <el-descriptions-item label="订单号">{{ detail.order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="statusTypes[detail.order.orderStatus]" size="small">{{ statusLabels[detail.order.orderStatus] }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额"><span style="color:#E53935;font-weight:600">¥{{ detail.order.totalAmount }}</span></el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="detail.order.payStatus===1?'success':'info'" size="small">{{ detail.order.payStatus===1?'已支付':'未支付' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ detail.order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ detail.order.payTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detail.order.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 用户 & 商家 -->
        <div class="section-title">买家 & 商家</div>
        <el-descriptions :column="2" border size="small" class="section-block">
          <el-descriptions-item label="买家账号">{{ detail.user?.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="买家昵称">{{ detail.user?.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="买家手机">{{ detail.user?.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商家店铺">{{ detail.merchant?.shopName || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 收货 & 物流 -->
        <div class="section-title">收货 & 物流</div>
        <el-descriptions :column="2" border size="small" class="section-block">
          <el-descriptions-item label="收货人">{{ detail.order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">{{ detail.order.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="快递公司">{{ detail.order.expressCompany || '-' }}</el-descriptions-item>
          <el-descriptions-item label="快递单号">{{ detail.order.expressNo || '-' }}</el-descriptions-item>
        </el-descriptions>

        <!-- 商品明细 -->
        <div class="section-title">商品明细</div>
        <el-table :data="detail.items" size="small" border class="section-block">
          <el-table-column prop="productName" label="商品名称" min-width="180" />
          <el-table-column prop="price" label="单价" width="90">
            <template #default="{row}">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="60" />
          <el-table-column prop="subtotal" label="小计" width="90">
            <template #default="{row}"><span style="color:#E53935">¥{{ row.subtotal }}</span></template>
          </el-table-column>
        </el-table>

        <!-- 评价信息 -->
        <div class="section-title">评价信息</div>
        <div class="section-block" v-if="detail.reviews && detail.reviews.length">
          <div v-for="rv in detail.reviews" :key="rv.id" class="review-card">
            <div class="rv-header">
              <el-rate :model-value="rv.rating" disabled size="small" />
              <span class="rv-time">{{ rv.createTime }}</span>
            </div>
            <div class="rv-content" v-if="rv.content">{{ rv.content }}</div>
            <div class="rv-images" v-if="rv.images">
              <el-image v-for="(img, i) in rv.images.split(',')" :key="i" :src="img" fit="cover" style="width:56px;height:56px;border-radius:4px" :preview-src-list="rv.images.split(',')" />
            </div>
            <div class="rv-append" v-if="rv.appendContent">
              <span class="rv-badge rv-badge-blue">追加评价</span>
              <span>{{ rv.appendContent }}</span>
              <span class="rv-time" v-if="rv.appendTime" style="margin-left:8px">{{ rv.appendTime }}</span>
            </div>
            <div class="rv-reply" v-if="rv.merchantReply">
              <span class="rv-badge rv-badge-orange">商家回复</span>
              <span>{{ rv.merchantReply }}</span>
              <span class="rv-time" v-if="rv.replyTime" style="margin-left:8px">{{ rv.replyTime }}</span>
            </div>
            <div class="rv-no-reply" v-if="!rv.merchantReply">商家暂未回复</div>
          </div>
        </div>
        <div class="section-block empty-hint" v-else>暂无评价</div>

        <!-- 售后记录 -->
        <div class="section-title">售后记录</div>
        <div class="section-block" v-if="detail.afterSales && detail.afterSales.length">
          <div v-for="as_ in detail.afterSales" :key="as_.id" class="as-card">
            <div class="as-header">
              <el-tag :type="asStatusType[as_.status]" size="small">{{ asStatusLabel[as_.status] }}</el-tag>
              <span class="as-type">{{ asTypeLabel[as_.type] || '售后' }}</span>
              <span class="rv-time">{{ as_.createTime }}</span>
            </div>
            <div class="as-row" v-if="as_.reason"><b>原因：</b>{{ as_.reason }}</div>
            <div class="as-row" v-if="as_.description"><b>描述：</b>{{ as_.description }}</div>
            <div class="as-row" v-if="as_.images"><b>图片：</b>
              <el-image v-for="(img, i) in as_.images.split(',')" :key="i" :src="img" fit="cover" style="width:48px;height:48px;border-radius:4px;margin-right:4px" :preview-src-list="as_.images.split(',')" />
            </div>
            <div class="as-row rv-reply" v-if="as_.merchantReply"><span class="rv-badge rv-badge-orange">商家回复</span>{{ as_.merchantReply }}</div>
            <div class="as-row rv-reply" v-if="as_.adminReply"><span class="rv-badge rv-badge-purple">管理员回复</span>{{ as_.adminReply }}</div>
          </div>
        </div>
        <div class="section-block empty-hint" v-else>暂无售后记录</div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import request from '../utils/request'

const statusLabels = ['待付款', '待发货', '待收货', '已完成', '已取消', '售后中']
const statusTypes = ['warning', 'info', '', 'success', 'danger', 'warning']
const asStatusLabel = { 0: '待处理', 1: '已同意', 2: '已拒绝', 3: '处理中', 4: '已完成' }
const asStatusType = { 0: 'warning', 1: 'success', 2: 'danger', 3: '', 4: 'info' }
const asTypeLabel = { 1: '退款', 2: '退货退款', 3: '换货' }

const list = ref([]), total = ref(0), current = ref(1), loading = ref(false)
const keyword = ref(''), activeStatus = ref('all')
const detailVisible = ref(false), detail = ref(null)
let timer = null
watch(keyword, () => { clearTimeout(timer); timer = setTimeout(() => { current.value = 1; loadData() }, 300) })

function onTabChange() { current.value = 1; loadData() }

async function loadData() {
  loading.value = true
  try {
    const params = { current: current.value, size: 10 }
    if (activeStatus.value !== 'all') params.orderStatus = Number(activeStatus.value)
    if (keyword.value) params.keyword = keyword.value
    const r = await request.get('/api/admin/order/list', { params })
    list.value = r.data?.records || []
    total.value = r.data?.total || 0
  } finally { loading.value = false }
}

async function showDetail(id) {
  const r = await request.get(`/api/admin/order/detail/${id}`)
  detail.value = r.data
  detailVisible.value = true
}

onMounted(loadData)
</script>

<style scoped>
.drawer-body { padding: 0 20px 20px; overflow-y: auto; max-height: calc(100vh - 60px); }
.section-title { font-size: 15px; font-weight: 700; color: #333; margin: 20px 0 10px; padding-left: 10px; border-left: 3px solid #B8860B; }
.section-block { margin-bottom: 8px; }
.empty-hint { color: #999; font-size: 13px; padding: 12px 0; }

/* 评价卡片 */
.review-card { padding: 12px; border: 1px solid #f0f0f0; border-radius: 6px; margin-bottom: 10px; background: #fafafa; }
.rv-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.rv-time { font-size: 12px; color: #999; }
.rv-content { font-size: 13px; color: #333; line-height: 1.6; margin-bottom: 6px; }
.rv-images { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 6px; }
.rv-append { margin-top: 8px; padding: 8px 12px; background: #f0f9ff; border-left: 3px solid #1890FF; border-radius: 4px; font-size: 13px; color: #333; }
.rv-reply { margin-top: 8px; padding: 8px 12px; background: #fffbe6; border-left: 3px solid #faad14; border-radius: 4px; font-size: 13px; color: #333; }
.rv-no-reply { margin-top: 6px; font-size: 12px; color: #bbb; }
.rv-badge { display: inline-block; padding: 1px 8px; border-radius: 3px; font-size: 11px; font-weight: 600; margin-right: 6px; }
.rv-badge-blue { background: #e6f7ff; color: #1890ff; }
.rv-badge-orange { background: #fff7e6; color: #fa8c16; }
.rv-badge-purple { background: #f9f0ff; color: #722ed1; }

/* 售后卡片 */
.as-card { padding: 12px; border: 1px solid #f0f0f0; border-radius: 6px; margin-bottom: 10px; background: #fafafa; }
.as-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.as-type { font-size: 13px; font-weight: 600; color: #555; }
.as-row { font-size: 13px; color: #333; line-height: 1.8; }
</style>
