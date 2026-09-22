<template>
  <div class="orders-page page-container">
    <h2 class="page-heading">我的订单</h2>

    <!-- 状态标签 -->
    <div class="status-tabs">
      <span v-for="tab in tabs" :key="tab.value" class="tab-item" :class="{ active: activeTab === tab.value }" @click="activeTab = tab.value; loadOrders()">
        {{ tab.label }}
      </span>
    </div>

    <el-empty v-if="!orders.length" description="暂无订单" :image-size="100" />

    <div v-for="o in orders" :key="o.id" class="order-card card-static">
      <div class="order-header">
        <div class="order-no">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          {{ o.orderNo }}
        </div>
        <span class="status-tag" :class="'status-' + o.orderStatus">{{ statusText(o.orderStatus) }}</span>
      </div>
      <div class="order-body">
        <div class="order-info">
          <div class="info-row">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            {{ o.receiverName }} {{ o.receiverPhone }}
          </div>
          <div class="info-row">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
            {{ o.receiverAddress }}
          </div>
          <!-- 物流信息 -->
          <div class="info-row logistics-row" v-if="o.expressCompany || o.expressNo">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>
            <span class="logistics-label">物流：</span>{{ o.expressCompany }} {{ o.expressNo }}
            <span class="logistics-status" v-if="o.logisticsStatus">
              ({{ ['', '已发货', '运输中', '已签收'][o.logisticsStatus] || '' }})
            </span>
          </div>
        </div>
        <div class="order-amount">
          <span class="amount-label">订单金额</span>
          <span class="amount-value">¥{{ o.totalAmount }}</span>
        </div>
      </div>
      <div class="order-footer">
        <span class="order-time">{{ o.createTime }}</span>
        <div class="order-actions">
          <button class="btn-action btn-detail" @click="viewDetail(o)">查看详情</button>
          <button v-if="o.orderStatus===0" class="btn-action btn-pay" @click="payOrder(o.id)">去支付</button>
          <button v-if="o.orderStatus===0" class="btn-action btn-cancel" @click="cancelOrder(o.id)">取消订单</button>
          <button v-if="o.orderStatus===2" class="btn-action btn-confirm" @click="confirmOrder(o.id)">确认收货</button>
          <button v-if="o.orderStatus===3 && !o._allReviewed" class="btn-action btn-review" @click="openReview(o)">评价</button>
          <span v-if="o.orderStatus===3 && o._allReviewed" class="reviewed-tag">已评价</span>
          <button v-if="o.orderStatus===3 && o._allReviewed && !o._allAppended" class="btn-action btn-append" @click="openAppend(o)">追评</button>
          <button v-if="(o.orderStatus===2 || o.orderStatus===3) && o._canAfterSale && !o._isRetry" class="btn-action btn-aftersale" @click="openAfterSale(o)">申请售后</button>
          <button v-if="(o.orderStatus===2 || o.orderStatus===3) && o._canAfterSale && o._isRetry" class="btn-action btn-aftersale-retry" @click="openAfterSale(o)">再次申请售后</button>
          <span v-if="(o.orderStatus===2 || o.orderStatus===3) && o._canAfterSale===false" class="aftersale-closed-tag">售后已关闭</span>
          <button v-if="o.orderStatus===5" class="btn-action btn-aftersale-pending" disabled>售后处理中</button>
          <button v-if="o.orderStatus===3 || o.orderStatus===2" class="btn-action btn-complaint" @click="openComplaint(o)">投诉商家</button>
        </div>
      </div>
    </div>

    <div class="pagination-wrap">
      <el-pagination background layout="prev,pager,next" :total="total" :page-size="10" v-model:current-page="current" @current-change="loadOrders" />
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="showDetail" title="订单详情" width="600px">
      <div v-if="detailOrder" class="detail-dialog-content">
        <div class="detail-section">
          <div class="detail-label">订单编号</div>
          <div class="detail-value">{{ detailOrder.orderNo }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">订单状态</div>
          <div class="detail-value"><span class="status-tag" :class="'status-' + detailOrder.orderStatus">{{ statusText(detailOrder.orderStatus) }}</span></div>
        </div>
        <div class="detail-section" v-if="detailOrder.expressCompany">
          <div class="detail-label">物流信息</div>
          <div class="detail-value">{{ detailOrder.expressCompany }} {{ detailOrder.expressNo }}
            <span class="logistics-status" v-if="detailOrder.logisticsStatus">({{ ['', '已发货', '运输中', '已签收'][detailOrder.logisticsStatus] || '' }})</span>
          </div>
        </div>
        <div class="detail-section">
          <div class="detail-label">收货信息</div>
          <div class="detail-value">{{ detailOrder.receiverName }} {{ detailOrder.receiverPhone }}<br/>{{ detailOrder.receiverAddress }}</div>
        </div>
        <div class="detail-section" v-if="detailOrder.remark">
          <div class="detail-label">备注</div>
          <div class="detail-value">{{ detailOrder.remark }}</div>
        </div>
        <div class="detail-section">
          <div class="detail-label">商品列表</div>
        </div>
        <div class="detail-items">
          <div v-for="item in detailItems" :key="item.id" class="detail-item" @click="$router.push(`/product/${item.productId}`)">
            <div class="detail-item-img">
              <img v-if="item.productImage" :src="getImageUrl(item.productImage)" />
              <div v-else class="detail-item-placeholder">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
              </div>
            </div>
            <div class="detail-item-info">
              <div class="detail-item-name">{{ item.productName }}</div>
              <div class="detail-item-meta">¥{{ item.price }} × {{ item.quantity }}</div>
            </div>
            <div class="detail-item-subtotal">¥{{ item.subtotal }}</div>
          </div>
        </div>
        <div class="detail-total">合计：<span>¥{{ detailOrder.totalAmount }}</span></div>
      </div>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="showReview" title="订单评价" width="560px">
      <div v-if="reviewItems.length" class="review-items">
        <div v-for="item in reviewItems" :key="item.id" class="review-item-block">
          <div class="review-item-header">
            <img v-if="item.productImage" :src="getImageUrl(item.productImage)" class="review-item-thumb" />
            <span class="review-item-name">{{ item.productName }}</span>
          </div>
          <div v-if="item.reviewed" class="review-done-tip">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#389E0D" stroke-width="2"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            已评价
          </div>
          <template v-else>
            <div class="review-rating-row">
              <span class="review-label">评分</span>
              <el-rate v-model="item.rating" />
            </div>
            <el-input v-model="item.content" type="textarea" :rows="2" placeholder="分享您的购物体验..." />
          </template>
        </div>
      </div>
      <template #footer>
        <el-button @click="showReview=false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="reviewSubmitting">提交评价</el-button>
      </template>
    </el-dialog>

    <!-- 追评弹窗 -->
    <el-dialog v-model="showAppend" title="追加评价" width="560px">
      <div v-if="appendItems.length" class="review-items">
        <div v-for="item in appendItems" :key="item.id" class="review-item-block">
          <div class="review-item-header">
            <img v-if="item.productImage" :src="getImageUrl(item.productImage)" class="review-item-thumb" />
            <span class="review-item-name">{{ item.productName }}</span>
          </div>
          <div style="font-size:12px;color:#999;margin-bottom:6px">原评价：{{ item.content || '无文字评价' }}</div>
          <div v-if="item.appendContent" class="review-done-tip">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#389E0D" stroke-width="2"><path d="M22 11.08V12a10 10 0 11-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            已追评：{{ item.appendContent }}
          </div>
          <template v-else>
            <el-input v-model="item._appendText" type="textarea" :rows="2" placeholder="补充评价内容..." />
          </template>
        </div>
      </div>
      <template #footer>
        <el-button @click="showAppend=false">取消</el-button>
        <el-button type="primary" @click="submitAppend" :loading="appendSubmitting">提交追评</el-button>
      </template>
    </el-dialog>

    <!-- 售后申请弹窗 -->
    <el-dialog v-model="showAfterSale" title="申请售后" width="500px">
      <el-form :model="afterSaleForm" label-width="80px">
        <el-form-item label="售后类型">
          <el-select v-model="afterSaleForm.type" style="width:100%">
            <el-option :value="1" label="退款" />
            <el-option :value="2" label="退货退款" />
            <el-option :value="3" label="换货" />
          </el-select>
        </el-form-item>
        <el-form-item label="售后原因">
          <el-input v-model="afterSaleForm.reason" placeholder="请输入售后原因" />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input v-model="afterSaleForm.description" type="textarea" :rows="3" placeholder="请描述具体问题..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAfterSale=false">取消</el-button>
        <el-button type="primary" @click="submitAfterSale" :loading="afterSaleSubmitting">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 投诉商家弹窗 -->
    <el-dialog v-model="showComplaint" title="投诉商家" width="520px">
      <el-form :model="complaintForm" label-width="80px">
        <el-form-item label="投诉类型">
          <el-select v-model="complaintForm.type" style="width:100%">
            <el-option :value="1" label="态度恶劣" />
            <el-option :value="2" label="辱骂消费者" />
            <el-option :value="3" label="虚假宣传" />
            <el-option :value="4" label="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="投诉说明">
          <el-input v-model="complaintForm.content" type="textarea" :rows="3" placeholder="请详细描述投诉原因..." />
        </el-form-item>
        <el-form-item label="截图证明">
          <div class="complaint-upload-area">
            <div v-for="(img, i) in complaintImages" :key="i" class="complaint-img-item">
              <img :src="getImageUrl(img)" class="complaint-img-preview" />
              <span class="complaint-img-del" @click="complaintImages.splice(i, 1)">&times;</span>
            </div>
            <div v-if="complaintImages.length < 5" class="complaint-img-add" @click="$refs.complaintFileInput?.click()">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>
            </div>
            <input type="file" ref="complaintFileInput" accept="image/*" style="display:none" @change="uploadComplaintImage" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showComplaint=false">取消</el-button>
        <el-button type="danger" @click="submitComplaint" :loading="complaintSubmitting">提交投诉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getImageUrl } from '../utils/image'

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待付款', value: '0' },
  { label: '待发货', value: '1' },
  { label: '待收货', value: '2' },
  { label: '已完成', value: '3' },
  { label: '已取消', value: '4' },
]

const activeTab = ref('all')
const orders = ref([])
const total = ref(0)
const current = ref(1)

function statusText(s) { return ['待付款','待发货','待收货','已完成','已取消','售后中'][s] || '未知' }

async function loadOrders() {
  const params = { current: current.value, size: 10 }
  if (activeTab.value !== 'all') params.orderStatus = Number(activeTab.value)
  const res = await request.get('/api/order/user/list', { params })
  orders.value = res.data?.records || []
  total.value = res.data?.total || 0
  // 检查已完成订单是否全部已评价 + 售后可申请状态
  for (const o of orders.value) {
    if (o.orderStatus === 3) {
      try {
        const detail = await request.get(`/api/order/detail/${o.id}`)
        const items = detail.data?.items || []
        if (items.length) {
          let allDone = true
          for (const item of items) {
            const r = await request.get('/api/review/check', { params: { orderId: o.id, productId: item.productId } })
            if (!r.data) { allDone = false; break }
          }
          o._allReviewed = allDone
          // 已全部评价时，检查是否全部追评
          if (allDone) {
            try {
              const reviewsRes = await request.get(`/api/review/order/${o.id}`)
              const reviews = reviewsRes.data || []
              o._allAppended = reviews.length > 0 && reviews.every(rv => rv.appendContent)
            } catch { o._allAppended = false }
          }
        }
      } catch {}
    }
    if (o.orderStatus === 2 || o.orderStatus === 3) {
      try {
        const r = await request.get('/api/aftersale/can-apply', { params: { orderId: o.id } })
        o._canAfterSale = r.data?.canApply === true
        o._isRetry = (r.data?.rejectedCount || 0) > 0
      } catch { o._canAfterSale = false; o._isRetry = false }
    }
  }
}

async function payOrder(id) {
  await ElMessageBox.confirm('确认模拟支付？', '支付确认')
  await request.put(`/api/order/pay/${id}`)
  ElMessage.success('支付成功')
  loadOrders()
}
async function cancelOrder(id) {
  await ElMessageBox.confirm('确认取消订单？', '提示')
  await request.put(`/api/order/cancel/${id}`)
  ElMessage.success('已取消')
  loadOrders()
}
async function confirmOrder(id) {
  await ElMessageBox.confirm('确认收货？', '提示')
  await request.put(`/api/order/confirm/${id}`)
  ElMessage.success('已确认收货')
  loadOrders()
}

// ===== 订单详情 =====
const showDetail = ref(false)
const detailOrder = ref(null)
const detailItems = ref([])

async function viewDetail(order) {
  try {
    const res = await request.get(`/api/order/detail/${order.id}`)
    detailOrder.value = res.data.order
    detailItems.value = res.data.items || []
    showDetail.value = true
  } catch {}
}

// ===== 评价功能 =====
const showReview = ref(false)
const reviewItems = ref([])
const reviewOrderId = ref(null)
const reviewSubmitting = ref(false)

async function openReview(order) {
  reviewOrderId.value = order.id
  try {
    const res = await request.get(`/api/order/detail/${order.id}`)
    const items = res.data.items || []
    reviewItems.value = items.map(item => ({
      ...item,
      rating: 5,
      content: '',
      reviewed: false
    }))
    // 检查哪些商品已评价
    for (const item of reviewItems.value) {
      try {
        const r = await request.get('/api/review/check', { params: { orderId: order.id, productId: item.productId } })
        if (r.data) item.reviewed = true
      } catch {}
    }
    // 全部已评价则标记订单并提示
    if (reviewItems.value.length && reviewItems.value.every(i => i.reviewed)) {
      order._allReviewed = true
      ElMessage.info('该订单所有商品均已评价')
      return
    }
    showReview.value = true
  } catch {}
}

async function submitReview() {
  const toReview = reviewItems.value.filter(i => !i.reviewed)
  if (!toReview.length) return ElMessage.info('所有商品已评价')
  reviewSubmitting.value = true
  try {
    for (const item of toReview) {
      await request.post('/api/review/add', {
        orderId: reviewOrderId.value,
        productId: item.productId,
        rating: item.rating,
        content: item.content
      })
    }
    ElMessage.success('评价成功')
    showReview.value = false
    // 刷新列表并标记已评价
    loadOrders()
  } catch (e) {
    if (e?.message?.includes('已评价')) {
      ElMessage.warning(e.message)
      showReview.value = false
      loadOrders()
    }
  } finally { reviewSubmitting.value = false }
}

// ===== 追评功能 =====
const showAppend = ref(false)
const appendItems = ref([])
const appendSubmitting = ref(false)

async function openAppend(order) {
  try {
    const r = await request.get(`/api/review/order/${order.id}`)
    const reviews = r.data || []
    if (!reviews.length) return ElMessage.info('暂无可追评的评价')
    if (reviews.every(i => i.appendContent)) {
      order._allAppended = true
      return ElMessage.info('所有商品已追评')
    }
    // 获取订单商品信息补充名称和图片
    const detail = await request.get(`/api/order/detail/${order.id}`)
    const itemsMap = {}
    for (const it of (detail.data?.items || [])) { itemsMap[it.productId] = it }
    appendItems.value = reviews.map(rv => ({
      ...rv,
      productName: itemsMap[rv.productId]?.productName || '商品#' + rv.productId,
      productImage: itemsMap[rv.productId]?.productImage || null,
      _appendText: ''
    }))
    showAppend.value = true
  } catch {}
}

async function submitAppend() {
  const toAppend = appendItems.value.filter(i => !i.appendContent && i._appendText?.trim())
  if (!toAppend.length) return ElMessage.warning('请至少填写一条追评内容')
  appendSubmitting.value = true
  try {
    for (const item of toAppend) {
      await request.put(`/api/review/append/${item.id}`, { appendContent: item._appendText.trim() })
    }
    ElMessage.success('追评成功')
    showAppend.value = false
    loadOrders()
  } catch {} finally { appendSubmitting.value = false }
}

// ===== 售后申请 =====
const showAfterSale = ref(false)
const afterSaleOrderId = ref(null)
const afterSaleForm = ref({ type: 1, reason: '', description: '' })
const afterSaleSubmitting = ref(false)

function openAfterSale(order) {
  afterSaleOrderId.value = order.id
  afterSaleForm.value = { type: 1, reason: '', description: '' }
  showAfterSale.value = true
}

async function submitAfterSale() {
  if (!afterSaleForm.value.reason) return ElMessage.warning('请填写售后原因')
  afterSaleSubmitting.value = true
  try {
    await request.post('/api/aftersale/apply', {
      orderId: afterSaleOrderId.value,
      ...afterSaleForm.value
    })
    ElMessage.success('售后申请已提交')
    showAfterSale.value = false
    loadOrders()
  } catch {} finally { afterSaleSubmitting.value = false }
}

// ===== 投诉商家 =====
const showComplaint = ref(false)
const complaintOrderId = ref(null)
const complaintMerchantId = ref(null)
const complaintForm = ref({ type: 1, content: '' })
const complaintImages = ref([])
const complaintSubmitting = ref(false)
const complaintFileInput = ref(null)

function openComplaint(order) {
  complaintOrderId.value = order.id
  complaintMerchantId.value = order.merchantId
  complaintForm.value = { type: 1, content: '' }
  complaintImages.value = []
  showComplaint.value = true
}

async function uploadComplaintImage(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 2 * 1024 * 1024) { ElMessage.warning('图片不能超过2MB'); return }
  const fd = new FormData()
  fd.append('file', file)
  try {
    const r = await request.post('/api/file/upload', fd)
    complaintImages.value.push(r.data)
  } catch { ElMessage.error('上传失败') }
  e.target.value = ''
}

async function submitComplaint() {
  if (!complaintForm.value.content) return ElMessage.warning('请填写投诉说明')
  complaintSubmitting.value = true
  try {
    await request.post('/api/complaint/add', {
      orderId: complaintOrderId.value,
      merchantId: complaintMerchantId.value,
      type: complaintForm.value.type,
      content: complaintForm.value.content,
      images: complaintImages.value.join(',')
    })
    ElMessage.success('投诉已提交，平台将尽快处理')
    showComplaint.value = false
  } catch {} finally { complaintSubmitting.value = false }
}

onMounted(loadOrders)
</script>

<style scoped>
.page-heading { font-size: 22px; font-weight: 700; color: var(--text-primary); margin-bottom: 20px; }

/* ===== 状态标签 ===== */
.status-tabs {
  display: flex; gap: 0; margin-bottom: 24px;
  border-bottom: 2px solid var(--border-light);
}
.tab-item {
  padding: 12px 28px; font-size: 14px; font-weight: 500;
  color: var(--text-secondary); cursor: pointer;
  border-bottom: 2px solid transparent; margin-bottom: -2px;
  transition: all 0.2s;
}
.tab-item:hover { color: var(--color-primary-dark); }
.tab-item.active { color: var(--color-primary-dark); font-weight: 600; border-bottom-color: var(--color-primary); }

/* ===== 订单卡片 ===== */
.order-card { margin-bottom: 16px; }
.order-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; border-bottom: 1px solid var(--border-light);
  background: var(--bg-section);
}
.order-no { font-size: 13px; color: var(--text-secondary); display: flex; align-items: center; gap: 6px; }
.status-tag {
  font-size: 12px; font-weight: 600; padding: 3px 12px;
  border-radius: 12px;
}
.status-0 { background: #FFF7E6; color: #D48806; }
.status-1 { background: #E6F7FF; color: #1890FF; }
.status-2 { background: var(--color-primary-bg); color: var(--color-primary-dark); }
.status-3 { background: #E6F7E9; color: #389E0D; }
.status-4 { background: #FFF1F0; color: #CF1322; }
.status-5 { background: #FFF7E6; color: #D48806; }

.order-body {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px;
}
.order-info { display: flex; flex-direction: column; gap: 6px; }
.info-row { font-size: 14px; color: var(--text-regular); display: flex; align-items: center; gap: 6px; }
.logistics-row { color: var(--color-primary-dark); font-size: 13px; }
.logistics-label { font-weight: 600; }
.logistics-status { color: var(--text-secondary); font-size: 12px; }
.order-amount { text-align: right; }
.amount-label { font-size: 12px; color: var(--text-secondary); display: block; margin-bottom: 4px; }
.amount-value { font-size: 22px; font-weight: 700; color: var(--color-accent); }

.order-footer {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 20px; border-top: 1px solid var(--border-light);
}
.order-time { font-size: 12px; color: var(--text-secondary); }
.order-actions { display: flex; gap: 8px; }
.btn-action {
  padding: 7px 20px; border-radius: var(--radius-sm); font-size: 13px; font-weight: 500;
  cursor: pointer; transition: all 0.2s; border: 1px solid transparent;
}
.btn-detail { background: var(--bg-white); color: var(--text-regular); border-color: var(--border-base); }
.btn-detail:hover { border-color: var(--color-primary); color: var(--color-primary); }
.btn-pay { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }
.btn-pay:hover { background: var(--color-primary-dark); }
.btn-cancel { background: var(--bg-white); color: var(--text-secondary); border-color: var(--border-base); }
.btn-cancel:hover { border-color: var(--color-accent); color: var(--color-accent); }
.btn-confirm { background: var(--color-primary); color: #fff; border-color: var(--color-primary); }
.btn-confirm:hover { background: var(--color-primary-dark); }
.btn-review { background: #E6F7E9; color: #389E0D; border-color: #B7EB8F; }
.btn-review:hover { background: #389E0D; color: #fff; }
.reviewed-tag { font-size: 12px; color: #389E0D; padding: 7px 12px; }
.btn-append { background: #E6F7FF; color: #1890FF; border-color: #91D5FF; }
.btn-append:hover { background: #1890FF; color: #fff; }
.btn-aftersale { background: var(--bg-white); color: #D48806; border-color: #FFE58F; }
.btn-aftersale:hover { background: #FFF7E6; border-color: #D48806; }
.btn-aftersale-retry { background: #FFF1F0; color: #CF1322; border-color: #FFA39E; }
.btn-aftersale-retry:hover { background: #CF1322; color: #fff; }
.aftersale-closed-tag { font-size: 12px; color: #999; padding: 7px 12px; }
.btn-aftersale-pending { background: #FFF7E6; color: #D48806; border: 1px solid #FFE58F; padding: 7px 16px; border-radius: var(--radius-sm); font-size: 13px; cursor: not-allowed; opacity: 0.7; }
.btn-complaint { background: var(--bg-white); color: #CF1322; border-color: #FFA39E; }
.btn-complaint:hover { background: #FFF1F0; border-color: #CF1322; }

.pagination-wrap { display: flex; justify-content: center; margin: 24px 0; }

/* ===== 订单详情弹窗 ===== */
.detail-dialog-content { font-size: 14px; }
.detail-section { display: flex; align-items: flex-start; padding: 8px 0; border-bottom: 1px solid var(--border-light); }
.detail-label { width: 80px; color: var(--text-secondary); flex-shrink: 0; font-size: 13px; }
.detail-value { flex: 1; color: var(--text-primary); }
.detail-items { margin-top: 8px; }
.detail-item {
  display: flex; align-items: center; gap: 12px; padding: 10px 0;
  border-bottom: 1px solid var(--border-light); cursor: pointer; transition: background 0.2s;
}
.detail-item:hover { background: var(--color-primary-bg); }
.detail-item:last-child { border-bottom: none; }
.detail-item-img { width: 56px; height: 56px; border-radius: 6px; overflow: hidden; flex-shrink: 0; background: #f8f5f2; }
.detail-item-img img { width: 100%; height: 100%; object-fit: cover; }
.detail-item-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
.detail-item-info { flex: 1; min-width: 0; }
.detail-item-name { font-size: 14px; font-weight: 500; color: var(--text-primary); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.detail-item-meta { font-size: 12px; color: var(--text-secondary); margin-top: 4px; }
.detail-item-subtotal { font-size: 15px; font-weight: 700; color: var(--color-accent); flex-shrink: 0; }
.detail-total { text-align: right; padding: 12px 0; font-size: 15px; color: var(--text-primary); }
.detail-total span { font-size: 20px; font-weight: 700; color: var(--color-accent); }

/* ===== 评价弹窗 ===== */
.review-items { display: flex; flex-direction: column; gap: 16px; }
.review-item-block { padding: 16px; border: 1px solid var(--border-light); border-radius: var(--radius-sm); }
.review-item-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.review-item-thumb { width: 40px; height: 40px; border-radius: 6px; object-fit: cover; }
.review-item-name { font-size: 14px; font-weight: 500; color: var(--text-primary); }
.review-done-tip { display: flex; align-items: center; gap: 6px; color: #389E0D; font-size: 13px; }
.review-rating-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.review-label { font-size: 13px; color: var(--text-secondary); }

/* ===== 投诉上传 ===== */
.complaint-upload-area { display: flex; flex-wrap: wrap; gap: 8px; }
.complaint-img-item { position: relative; width: 72px; height: 72px; border-radius: 6px; overflow: hidden; border: 1px solid var(--border-light); }
.complaint-img-preview { width: 100%; height: 100%; object-fit: cover; }
.complaint-img-del { position: absolute; top: 2px; right: 4px; font-size: 16px; color: #fff; background: rgba(0,0,0,0.5); border-radius: 50%; width: 18px; height: 18px; display: flex; align-items: center; justify-content: center; cursor: pointer; line-height: 1; }
.complaint-img-add { width: 72px; height: 72px; border: 2px dashed var(--border-light); border-radius: 6px; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: border-color 0.2s; }
.complaint-img-add:hover { border-color: var(--color-primary); }
</style>
