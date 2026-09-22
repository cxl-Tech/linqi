<template>
  <div class="products-page">
    <div class="products-layout">
      <!-- 左侧分类侧边栏 — sticky -->
      <aside class="sidebar">
        <h4 class="sidebar-title">商品分类</h4>
        <div class="category-list">
          <div class="category-item" :class="{ active: !query.categoryId }" @click="selectCategory(null)">全部分类</div>
          <div v-for="c in categories" :key="c.id" class="category-item" :class="{ active: query.categoryId === c.id }" @click="selectCategory(c.id)">
            {{ c.name }}
          </div>
        </div>
        <div class="sidebar-section">
          <h4 class="sidebar-title">临期状态</h4>
          <div class="category-item" :class="{ active: query.expiryStatus == null }" @click="selectExpiry(null)">全部</div>
          <div class="category-item" :class="{ active: query.expiryStatus === 0 }" @click="selectExpiry(0)">正常保质期</div>
          <div class="category-item near-tag" :class="{ active: query.expiryStatus === 1 }" @click="selectExpiry(1)">临期商品</div>
        </div>
      </aside>

      <!-- 右侧主区域 -->
      <div class="main-area">
        <!-- 顶部筛选条 — sticky -->
        <div class="filter-bar">
          <div class="filter-left">
            <div class="filter-sorts">
              <span class="sort-item" :class="{ active: query.sortField === '' }" @click="setSort('')">综合</span>
              <span class="sort-item" :class="{ active: query.sortField === 'sales' }" @click="setSort('sales')">销量</span>
              <span class="sort-item" :class="{ active: query.sortField === 'price' }" @click="setSort('price')">价格</span>
            </div>
            <div class="result-info">共找到 <b>{{ total }}</b> 件商品</div>
          </div>
          <div class="filter-search">
            <input v-model="query.keyword" placeholder="在结果中搜索..." @keyup.enter="loadData" />
            <button @click="loadData">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            </button>
          </div>
        </div>

        <!-- 商品网格 -->
        <div class="product-grid">
          <div v-for="p in products" :key="p.id" class="product-card card" :class="{'promo-card': p.expiryStatus===1 && p.originalPrice && p.price < p.originalPrice}" @click="$router.push(`/product/${p.id}`)">
            <div class="product-img">
              <img :src="getImageUrl(p.images ? p.images.split(',')[0] : null)" :alt="p.name" />
              <div class="promo-ribbon" v-if="p.expiryStatus===1 && p.originalPrice && p.price < p.originalPrice">
                <span>{{ (p.price / p.originalPrice * 10).toFixed(1) }}折</span>
              </div>
            </div>
            <div class="product-info">
              <div class="product-name-row">
                <span class="badge-expiry" :class="p.expiryStatus===1?'near':p.expiryStatus===2?'expired':'normal'">
                  {{ p.expiryStatus===1?'临期':p.expiryStatus===2?'过期':'正常' }}
                </span>
                <span class="promo-badge" v-if="p.expiryStatus===1 && p.originalPrice && p.price < p.originalPrice">促销</span>
                <h4 class="product-name">{{ p.name }}</h4>
              </div>
              <div class="price-row">
                <span class="price">¥{{ p.price }}</span>
                <span class="original-price" v-if="p.originalPrice && p.price < p.originalPrice">¥{{ p.originalPrice }}</span>
              </div>
              <div class="product-meta">已售 {{ p.sales||0 }}</div>
            </div>
          </div>
        </div>

        <el-empty v-if="!products.length" description="暂无符合条件的商品" />

        <div class="pagination-wrap">
          <el-pagination background layout="prev,pager,next,total" :total="total" :page-size="20" v-model:current-page="query.current" @current-change="loadData" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '../utils/request'
import { getImageUrl } from '../utils/image'

const route = useRoute()
const products = ref([])
const categories = ref([])
const total = ref(0)
const query = reactive({
  current: 1,
  keyword: route.query.keyword || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : null,
  expiryStatus: route.query.expiryStatus != null ? Number(route.query.expiryStatus) : null,
  sortField: route.query.sortField || ''
})

function selectCategory(id) { query.categoryId = id; query.current = 1; loadData() }
function selectExpiry(val) { query.expiryStatus = val; query.current = 1; loadData() }
function setSort(field) { query.sortField = field; query.current = 1; loadData() }

async function loadData() {
  const params = { current: query.current, size: 20 }
  if (query.keyword) params.keyword = query.keyword
  if (query.categoryId) params.categoryId = query.categoryId
  if (query.expiryStatus != null) params.expiryStatus = query.expiryStatus
  if (query.sortField && query.sortField !== 'time') { params.sortField = query.sortField; params.sortOrder = query.sortField === 'price' ? 'asc' : 'desc' }
  const res = await request.get('/api/product/list', { params })
  products.value = res.data?.records || []
  total.value = res.data?.total || 0
}

watch(() => route.query, (q) => {
  if (q.keyword) query.keyword = q.keyword
  if (q.categoryId) query.categoryId = Number(q.categoryId)
  if (q.expiryStatus != null) query.expiryStatus = Number(q.expiryStatus)
  if (q.sortField) query.sortField = q.sortField
  loadData()
})

onMounted(async () => {
  try { const r = await request.get('/api/category/list'); categories.value = r.data || [] } catch {}
  loadData()
})
</script>

<style scoped>
/* ===== 页面全屏铺满 ===== */
.products-page { width: 100%; padding: 0; }
.products-layout { display: flex; align-items: flex-start; width: 100%; }

/* ===== 侧边栏 — 紧贴左侧，sticky ===== */
.sidebar {
  width: 200px; flex-shrink: 0; padding: 16px 0;
  position: sticky; top: 162px;
  align-self: flex-start;
  background: var(--bg-white); border-right: 1px solid var(--border-light);
  min-height: calc(100vh - 162px);
  overflow-y: auto;
}
.sidebar-title {
  padding: 0 20px; font-size: 14px; font-weight: 700; color: var(--text-primary);
  margin-bottom: 8px;
}
.sidebar-section { margin-top: 16px; border-top: 1px solid var(--border-light); padding-top: 12px; }
.category-list { display: flex; flex-direction: column; }
.category-item {
  padding: 10px 20px; font-size: 13px; color: var(--text-regular);
  cursor: pointer; transition: all 0.15s; border-left: 3px solid transparent;
}
.category-item:hover { background: var(--color-primary-bg); color: var(--color-primary-dark); }
.category-item.active {
  background: var(--color-primary-bg); color: var(--color-primary-dark);
  font-weight: 600; border-left-color: var(--color-primary);
}
.category-item.near-tag { color: var(--color-accent); }

/* ===== 右侧主区域 — 撑满剩余宽度 ===== */
.main-area { flex: 1; min-width: 0; padding: 0 24px 24px; }

/* ===== 筛选条 — sticky ===== */
.filter-bar {
  position: sticky; top: 162px; z-index: 50;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 16px; height: 48px;
  background: var(--bg-white); border-bottom: 1px solid var(--border-light);
  margin: 0 -24px 16px; padding: 0 24px;
}
.filter-left { display: flex; align-items: center; gap: 24px; }
.filter-sorts { display: flex; gap: 0; }
.sort-item {
  padding: 6px 20px; font-size: 13px; color: var(--text-regular);
  cursor: pointer; border-bottom: 2px solid transparent; transition: all 0.2s;
}
.sort-item:hover { color: var(--color-primary-dark); }
.sort-item.active { color: var(--color-primary-dark); font-weight: 600; border-bottom-color: var(--color-primary); }
.result-info { font-size: 13px; color: var(--text-secondary); }
.filter-search {
  display: flex; align-items: center; border: 1px solid var(--border-base);
  border-radius: 20px; overflow: hidden;
}
.filter-search input {
  border: none; outline: none; padding: 6px 14px; font-size: 13px;
  width: 180px; background: transparent; color: var(--text-primary);
}
.filter-search button {
  background: transparent; border: none; padding: 6px 12px;
  cursor: pointer; color: var(--text-secondary);
}

/* ===== 商品网格 — 5列 ===== */
.product-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; }
.product-card { cursor: pointer; border: 1px solid var(--border-light); }
.product-card:hover { border-color: var(--color-primary-light); }
.product-img { position: relative; height: 200px; overflow: hidden; background: var(--bg-section); }
.product-img img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.4s; }
.product-card:hover .product-img img { transform: scale(1.06); }
.product-info { padding: 12px; }
.product-name-row { display: flex; align-items: center; gap: 6px; margin-bottom: 8px; }
.badge-expiry {
  display: inline-block; padding: 3px 8px; border-radius: 4px;
  font-weight: 700; flex-shrink: 0; line-height: 1.3; letter-spacing: 0.5px;
  font-size: 11px; border: 1px solid transparent;
}
.badge-expiry.normal { background: var(--color-primary-bg); color: var(--color-primary-dark); border-color: var(--color-primary-light); font-size: 11px; }
.badge-expiry.near { background: #FFF1EC; color: #D4380D; border-color: #FFCCC7; font-size: 12px; }
.badge-expiry.expired { background: #F5F5F5; color: #999; border-color: #E8E8E8; font-size: 11px; }
.product-name {
  font-size: 13px; color: var(--text-primary); font-weight: 500;
  line-height: 1.4; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  min-width: 0;
}
.price-row { display: flex; align-items: baseline; gap: 6px; }
.price { font-size: 18px; font-weight: 700; color: var(--color-accent); }
.original-price { font-size: 12px; color: var(--text-secondary); text-decoration: line-through; }
.product-meta { font-size: 12px; color: var(--text-secondary); margin-top: 4px; }

/* ===== 促销标识 ===== */
.promo-card { }
.promo-ribbon {
  position: absolute; top: 0; right: 0;
  background: linear-gradient(135deg, #FF5722, #E53935);
  color: #fff; padding: 4px 14px 4px 18px;
  font-size: 13px; font-weight: 700;
  border-radius: 0 0 0 12px;
  box-shadow: 0 2px 6px rgba(229,57,53,0.3);
}
.promo-badge {
  display: inline-block; background: #E53935; color: #fff;
  font-size: 10px; font-weight: 700; padding: 1px 6px;
  border-radius: 3px; margin-left: 4px; vertical-align: middle;
  white-space: nowrap; flex-shrink: 0;
}

.pagination-wrap { display: flex; justify-content: center; margin: 32px 0 16px; }

@media (max-width: 1400px) { .product-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 1100px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) {
  .products-layout { flex-direction: column; }
  .sidebar { width: 100%; position: static; min-height: auto; border-right: none; border-bottom: 1px solid var(--border-light); }
  .product-grid { grid-template-columns: repeat(2, 1fr); }
  .filter-bar { position: static; }
}
</style>
