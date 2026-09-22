<template>
  <div class="detail-page page-container" v-if="product">
    <!-- 面包屑 -->
    <div class="breadcrumb-bar">
      <router-link to="/">首页</router-link>
      <span class="sep">/</span>
      <router-link to="/products">全部商品</router-link>
      <span class="sep">/</span>
      <span class="current">{{ product.name }}</span>
    </div>

    <!-- 主体：左图右信息 -->
    <div class="detail-main card-static">
      <div class="detail-left">
        <div class="main-image-wrap" v-if="images.length">
          <img :src="images[currentImg]" class="main-image" />
        </div>
        <div v-else class="main-image-wrap empty-image">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
        </div>
        <div class="thumb-list" v-if="images.length > 1">
          <div v-for="(img, i) in images" :key="i" class="thumb-item" :class="{ active: currentImg === i }" @mouseenter="currentImg = i">
            <img :src="img" />
          </div>
        </div>
      </div>

      <div class="detail-right">
        <h1 class="product-title">{{ product.name }}</h1>
        <div class="tag-row">
          <span class="badge-expiry" :class="product.expiryStatus===1?'near':product.expiryStatus===2?'expired':'normal'">
            {{ product.expiryStatus===1?'临期商品':product.expiryStatus===2?'已过期':'正常保质期' }}
          </span>
          <span class="stock-info">库存 {{ product.stock }} 件</span>
        </div>

        <!-- 价格区 -->
        <div class="price-zone" :class="{'price-zone-promo': product.expiryStatus===1 && product.originalPrice && product.price < product.originalPrice}">
          <div class="promo-banner" v-if="product.expiryStatus===1 && product.originalPrice && product.price < product.originalPrice">
            <span class="promo-banner-tag">临期促销</span>
            <span class="promo-banner-discount">{{ (product.price / product.originalPrice * 10).toFixed(1) }}折</span>
          </div>
          <div class="price-main">
            <span class="label">{{ product.expiryStatus===1 && product.originalPrice && product.price < product.originalPrice ? '促销价' : '售价' }}</span>
            <span class="symbol">¥</span>
            <span class="amount">{{ product.price }}</span>
            <span class="original" v-if="product.originalPrice && product.price < product.originalPrice">¥{{ product.originalPrice }}</span>
            <span class="discount-label" v-if="product.originalPrice && product.price < product.originalPrice">省 ¥{{ (product.originalPrice - product.price).toFixed(2) }}</span>
          </div>
        </div>

        <!-- 属性信息 -->
        <div class="attr-list">
          <div class="attr-row"><span class="attr-label">生产日期</span><span class="attr-value">{{ product.productionDate }}</span></div>
          <div class="attr-row"><span class="attr-label">保质期</span><span class="attr-value">{{ product.shelfLifeDays }} 天</span></div>
          <div class="attr-row"><span class="attr-label">到期日期</span><span class="attr-value">{{ product.expiryDate }}</span></div>
          <div class="attr-row" v-if="product.origin"><span class="attr-label">产地</span><span class="attr-value">{{ product.origin }}</span></div>
          <div class="attr-row" v-if="product.manufacturer"><span class="attr-label">厂家</span><span class="attr-value">{{ product.manufacturer }}</span></div>
          <div class="attr-row" v-if="product.licenseNo"><span class="attr-label">许可证号</span><span class="attr-value">{{ product.licenseNo }}</span></div>
        </div>

        <!-- 数量选择 -->
        <div class="quantity-row">
          <span class="attr-label">数量</span>
          <el-input-number v-model="quantity" :min="1" :max="product.stock" size="default" />
        </div>

        <!-- 操作按钮 -->
        <div class="action-row">
          <button class="btn-buy-now" @click="handleBuyNow">立即购买</button>
          <button class="btn-cart" @click="addToCart">加入购物车</button>
          <button class="btn-fav" :class="{ faved: isFaved }" @click="toggleFav">
            <svg width="18" height="18" viewBox="0 0 24 24" :fill="isFaved ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
            {{ isFaved ? '已收藏' : '收藏' }}
          </button>
          <button class="btn-consult" @click="consultMerchant">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>
            咨询商家
          </button>
        </div>
      </div>
    </div>

    <!-- 立即购买弹窗 -->
    <el-dialog v-model="showBuyNow" title="确认订单" width="520px">
      <div class="buy-now-product">
        <span class="buy-now-name">{{ product?.name }}</span>
        <span class="buy-now-qty">× {{ quantity }}</span>
        <span class="buy-now-price">¥{{ (product?.price * quantity).toFixed(2) }}</span>
      </div>
      <el-form label-width="80px" style="margin-top:16px">
        <el-form-item label="收货地址">
          <el-select v-model="selectedAddress" placeholder="请选择收货地址" style="width:100%">
            <el-option v-for="a in addresses" :key="a.id" :value="a.id" :label="a.receiverName + ' ' + a.phone + ' ' + a.province + a.city + a.district + a.detail" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="orderRemark" type="textarea" rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBuyNow=false">取消</el-button>
        <el-button type="primary" @click="submitBuyNow" :loading="submitting">提交订单</el-button>
      </template>
    </el-dialog>

    <!-- 商品详情 + 质检 -->
    <div class="detail-tabs card-static">
      <div class="tabs-header">
        <span class="tab-item" :class="{ active: activeTab === 'detail' }" @click="activeTab='detail'">商品详情</span>
        <span class="tab-item" :class="{ active: activeTab === 'reviews' }" @click="activeTab='reviews'; loadReviews()">用户评价 <span v-if="reviewTotal" class="review-count">({{ reviewTotal }})</span></span>
        <span class="tab-item" :class="{ active: activeTab === 'safety' }" @click="activeTab='safety'">食品安全</span>
      </div>
      <div class="tabs-body">
        <div v-show="activeTab === 'detail'">
          <p v-if="product.description" class="desc-text">{{ product.description }}</p>
          <el-empty v-else description="暂无详情" :image-size="80" />
          <div v-if="product.qualityReportImgs" class="quality-section">
            <h4>质检报告</h4>
            <div class="quality-imgs">
              <el-image v-for="(img, i) in product.qualityReportImgs.split(',')" :key="i" :src="getImageUrl(img)" class="quality-img" fit="cover" :preview-src-list="product.qualityReportImgs.split(',').map(getImageUrl)" />
            </div>
          </div>
        </div>
        <div v-show="activeTab === 'reviews'" class="reviews-content">
          <el-empty v-if="!reviews.length" description="暂无评价" :image-size="80" />
          <div v-for="r in reviews" :key="r.id" class="review-card">
            <div class="review-top">
              <div class="review-user">
                <div class="review-avatar">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                </div>
                <span class="review-username">用户{{ String(r.userId).slice(-4) }}</span>
              </div>
              <el-rate :model-value="r.rating" disabled size="small" />
            </div>
            <div class="review-text" v-if="r.content">{{ r.content }}</div>
            <div class="review-images" v-if="r.images">
              <el-image v-for="(img, i) in r.images.split(',')" :key="i" :src="getImageUrl(img)" class="review-img" fit="cover" :preview-src-list="r.images.split(',').map(getImageUrl)" />
            </div>
            <div class="review-time">{{ formatReviewTime(r.createTime) }}</div>
            <div class="review-append" v-if="r.appendContent">
              <span class="append-label">追加评价</span>
              <span class="append-text">{{ r.appendContent }}</span>
            </div>
            <div class="review-reply" v-if="r.merchantReply">
              <span class="reply-label">商家回复</span>
              <span class="reply-text">{{ r.merchantReply }}</span>
            </div>
          </div>
          <div class="reviews-pagination" v-if="reviewTotal > 5">
            <el-pagination background layout="prev,pager,next" :total="reviewTotal" :page-size="5" v-model:current-page="reviewPage" @current-change="loadReviews" small />
          </div>
        </div>
        <div v-show="activeTab === 'safety'" class="safety-content">
          <div class="safety-card">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#B8860B" stroke-width="2" stroke-linecap="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            <div>
              <h4>食品安全保障</h4>
              <p>请注意查看商品生产日期与保质期，临期食品请在有效期内尽快食用。</p>
              <p>本平台所有食品均经过商家资质审核，请放心购买。</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { getImageUrl } from '../utils/image'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const quantity = ref(1)
const currentImg = ref(0)
const isFaved = ref(false)
const activeTab = ref('detail')
const images = computed(() => product.value?.images ? product.value.images.split(',').filter(Boolean).map(getImageUrl) : [])
const showBuyNow = ref(false)
const addresses = ref([])
const selectedAddress = ref(null)
const orderRemark = ref('')
const submitting = ref(false)
const reviews = ref([])
const reviewTotal = ref(0)
const reviewPage = ref(1)

function formatReviewTime(t) {
  if (!t) return ''
  const d = new Date(t.replace(' ', 'T'))
  if (isNaN(d)) return t
  const Y = d.getFullYear(), M = String(d.getMonth() + 1).padStart(2, '0'), D = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0'), m = String(d.getMinutes()).padStart(2, '0')
  return `${Y}-${M}-${D} ${h}:${m}`
}

async function loadReviews() {
  try {
    const r = await request.get(`/api/review/product/${route.params.id}`, { params: { current: reviewPage.value, size: 5 } })
    reviews.value = r.data?.records || []
    reviewTotal.value = r.data?.total || 0
  } catch {}
}

onMounted(async () => {
  const res = await request.get(`/api/product/detail/${route.params.id}`)
  product.value = res.data
  loadReviews()
  if (localStorage.getItem('user_token')) {
    try { await request.post(`/api/history/record/${route.params.id}`) } catch {}
    try { const r = await request.get(`/api/favorite/check/${route.params.id}`); isFaved.value = r.data } catch {}
  }
})

async function addToCart() {
  if (!localStorage.getItem('user_token')) { router.push('/login'); return }
  await request.post('/api/cart/add', { productId: product.value.id, quantity: quantity.value })
  ElMessage.success('已加入购物车')
}
async function toggleFav() {
  if (!localStorage.getItem('user_token')) { router.push('/login'); return }
  await request.post(`/api/favorite/toggle/${product.value.id}`)
  isFaved.value = !isFaved.value
  ElMessage.success(isFaved.value ? '已收藏' : '已取消收藏')
}
async function handleBuyNow() {
  if (!localStorage.getItem('user_token')) { router.push('/login'); return }
  try {
    const r = await request.get('/api/address/list')
    addresses.value = r.data || []
  } catch {}
  selectedAddress.value = addresses.value.length ? addresses.value[0].id : null
  orderRemark.value = ''
  showBuyNow.value = true
}
async function submitBuyNow() {
  if (!selectedAddress.value) return ElMessage.warning('请选择收货地址')
  submitting.value = true
  try {
    await request.post('/api/order/buy-now', {
      productId: product.value.id,
      quantity: quantity.value,
      addressId: selectedAddress.value,
      remark: orderRemark.value
    })
    ElMessage.success('下单成功')
    showBuyNow.value = false
    router.push('/orders')
  } finally { submitting.value = false }
}
function consultMerchant() {
  if (!localStorage.getItem('user_token')) { router.push('/login'); return }
  if (!product.value?.merchantId) { ElMessage.warning('商家信息不可用'); return }
  router.push(`/chat?merchantId=${product.value.merchantId}`)
}
</script>

<style scoped>
/* ===== 面包屑 ===== */
.breadcrumb-bar {
  display: flex; align-items: center; gap: 8px; margin-bottom: 20px;
  font-size: 13px; color: var(--text-secondary);
}
.breadcrumb-bar a { color: var(--text-secondary); transition: color 0.2s; }
.breadcrumb-bar a:hover { color: var(--color-primary); }
.breadcrumb-bar .sep { color: var(--border-base); }
.breadcrumb-bar .current { color: var(--text-primary); }

/* ===== 主体 ===== */
.detail-main { display: flex; gap: 40px; padding: 32px; }
.detail-left { width: 480px; flex-shrink: 0; }
.main-image-wrap {
  width: 100%; height: 480px; border-radius: var(--radius-md);
  overflow: hidden; background: var(--bg-section);
  display: flex; align-items: center; justify-content: center;
}
.main-image { width: 100%; height: 100%; object-fit: cover; }
.empty-image { background: #fafafa; }
.thumb-list { display: flex; gap: 8px; margin-top: 12px; }
.thumb-item {
  width: 64px; height: 64px; border-radius: 6px; overflow: hidden;
  border: 2px solid transparent; cursor: pointer; transition: border-color 0.2s;
}
.thumb-item.active { border-color: var(--color-primary); }
.thumb-item img { width: 100%; height: 100%; object-fit: cover; }

.detail-right { flex: 1; min-width: 0; }
.product-title {
  font-size: 24px; font-weight: 600; color: var(--text-primary);
  line-height: 1.4; margin-bottom: 12px;
}
.tag-row { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.stock-info { font-size: 13px; color: var(--text-secondary); }

/* ===== 价格区 ===== */
.price-zone {
  background: linear-gradient(135deg, var(--color-primary-bg), #FFF5EE);
  padding: 20px 24px; border-radius: var(--radius-md); margin-bottom: 20px;
}
.price-zone-promo {
  background: linear-gradient(135deg, #FFF1F0, #FFE4E1) !important;
  border: 2px solid #FFCDD2;
}
.promo-banner {
  display: flex; align-items: center; gap: 10px; margin-bottom: 12px;
}
.promo-banner-tag {
  background: linear-gradient(135deg, #FF5722, #E53935);
  color: #fff; padding: 3px 12px; border-radius: 4px;
  font-size: 13px; font-weight: 700; letter-spacing: 1px;
}
.promo-banner-discount {
  background: #E53935; color: #fff; padding: 2px 10px; border-radius: 4px;
  font-size: 15px; font-weight: 800;
}
.price-main { display: flex; align-items: baseline; gap: 4px; }
.price-main .label { font-size: 13px; color: var(--color-accent); margin-right: 8px; }
.price-main .symbol { font-size: 18px; color: var(--color-accent); font-weight: 700; }
.price-main .amount { font-size: 36px; font-weight: 800; color: var(--color-accent); line-height: 1; }
.price-main .original {
  font-size: 14px; color: var(--text-secondary); text-decoration: line-through; margin-left: 12px;
}
.discount-label {
  margin-left: 12px; padding: 2px 10px; border-radius: 4px;
  font-size: 12px; font-weight: 600; background: var(--color-accent); color: #fff;
}

/* ===== 属性列表 ===== */
.attr-list { margin-bottom: 20px; }
.attr-row {
  display: flex; align-items: center; padding: 10px 0;
  border-bottom: 1px solid var(--border-light); font-size: 14px;
}
.attr-label { width: 80px; color: var(--text-secondary); flex-shrink: 0; }
.attr-value { color: var(--text-primary); }

/* ===== 数量选择 ===== */
.quantity-row {
  display: flex; align-items: center; gap: 16px;
  padding: 16px 0; margin-bottom: 20px;
}

/* ===== 操作按钮 ===== */
.action-row { display: flex; gap: 16px; }
.btn-buy-now {
  padding: 14px 40px; background: var(--color-accent); color: #fff;
  border: none; border-radius: var(--radius-sm); font-size: 16px; font-weight: 600;
  cursor: pointer; letter-spacing: 1px; transition: all 0.3s;
}
.btn-buy-now:hover { background: #B82E09; }
.btn-cart {
  padding: 14px 40px; background: var(--color-primary); color: #fff;
  border: none; border-radius: var(--radius-sm); font-size: 16px; font-weight: 600;
  cursor: pointer; letter-spacing: 1px; transition: all 0.3s;
}
.btn-cart:hover { background: var(--color-primary-dark); }
.buy-now-product {
  display: flex; align-items: center; gap: 12px; padding: 12px 16px;
  background: var(--color-primary-bg); border-radius: var(--radius-sm);
}
.buy-now-name { flex: 1; font-weight: 600; color: var(--text-primary); }
.buy-now-qty { color: var(--text-secondary); }
.buy-now-price { font-size: 18px; font-weight: 700; color: var(--color-accent); }
.btn-fav {
  padding: 14px 24px; background: var(--bg-white); color: var(--text-regular);
  border: 1px solid var(--border-base); border-radius: var(--radius-sm);
  font-size: 14px; cursor: pointer; display: flex; align-items: center; gap: 6px;
  transition: all 0.2s;
}
.btn-fav:hover { border-color: var(--color-primary); color: var(--color-primary); }
.btn-fav.faved { color: var(--color-accent); border-color: var(--color-accent); }
.btn-consult {
  padding: 14px 24px; background: var(--bg-white); color: var(--color-primary-dark);
  border: 1px solid var(--color-primary); border-radius: var(--radius-sm);
  font-size: 14px; cursor: pointer; display: flex; align-items: center; gap: 6px;
  transition: all 0.2s;
}
.btn-consult:hover { background: var(--color-primary-bg); }

/* ===== 详情标签页 ===== */
.detail-tabs { margin-top: 24px; }
.tabs-header {
  display: flex; border-bottom: 2px solid var(--border-light);
  padding: 0 24px;
}
.tab-item {
  padding: 16px 24px; font-size: 15px; font-weight: 500;
  color: var(--text-secondary); cursor: pointer;
  border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all 0.2s;
}
.tab-item.active { color: var(--color-primary-dark); border-bottom-color: var(--color-primary); }
.tabs-body { padding: 24px; }
.desc-text { color: var(--text-regular); line-height: 2; font-size: 14px; }
.quality-section { margin-top: 24px; }
.quality-section h4 { font-size: 15px; font-weight: 600; margin-bottom: 12px; color: var(--text-primary); }
.quality-imgs { display: flex; gap: 10px; flex-wrap: wrap; }
.quality-img { width: 140px; height: 140px; border-radius: 8px; }

.safety-content { padding: 8px 0; }
.safety-card {
  display: flex; gap: 16px; padding: 20px; border-radius: var(--radius-md);
  background: var(--color-gold-light); border: 1px solid #F0E0B0;
}
.safety-card h4 { font-size: 15px; font-weight: 600; color: var(--color-gold); margin-bottom: 8px; }
.safety-card p { font-size: 13px; color: var(--text-regular); line-height: 2; }

/* ===== 评价标签页 ===== */
.review-count { font-size: 12px; color: var(--text-secondary); }
.reviews-content { padding: 0; }
.review-card {
  padding: 16px 0; border-bottom: 1px solid var(--border-light);
}
.review-card:last-child { border-bottom: none; }
.review-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.review-user { display: flex; align-items: center; gap: 8px; }
.review-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  display: flex; align-items: center; justify-content: center;
}
.review-username { font-size: 13px; font-weight: 500; color: var(--text-primary); }
.review-text { font-size: 14px; color: var(--text-regular); line-height: 1.8; margin-bottom: 8px; }
.review-images { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }
.review-img { width: 80px; height: 80px; border-radius: 6px; }
.review-time { font-size: 12px; color: var(--text-secondary); }
.review-append {
  margin-top: 8px; padding: 10px 14px; border-radius: var(--radius-sm);
  background: #FAFAFA; font-size: 13px; border-left: 3px solid #1890FF;
}
.append-label { color: #1890FF; font-weight: 600; margin-right: 8px; }
.append-text { color: var(--text-regular); }
.review-reply {
  margin-top: 10px; padding: 10px 14px; border-radius: var(--radius-sm);
  background: var(--bg-section); font-size: 13px;
}
.reply-label { color: var(--color-primary-dark); font-weight: 600; margin-right: 8px; }
.reply-text { color: var(--text-regular); }
.reviews-pagination { display: flex; justify-content: center; padding: 16px 0; }
</style>
