<template>
  <div class="home">
    <!-- Banner -->
    <section class="banner-section">
      <el-carousel height="460px" :interval="5000" arrow="hover" indicator-position="outside">
        <el-carousel-item v-for="b in banners" :key="b.id">
          <div class="banner-item" :style="{ background: b.imageUrl ? `url(${b.imageUrl}) center/cover no-repeat` : '' }">
            <div class="banner-overlay">
              <h2>{{ b.title || '临期优品' }}</h2>
              <p v-if="!b.title">品质生活 · 聪明之选 · 低至1折起</p>
            </div>
          </div>
        </el-carousel-item>
        <el-carousel-item v-if="!banners.length">
          <div class="banner-item banner-default">
            <div class="banner-overlay">
              <span class="banner-tag">限时特惠</span>
              <h2>临期不临质，省钱更省心</h2>
              <p>严选品质好货，为您的品质生活保驾护航</p>
              <router-link to="/products?expiryStatus=1" class="banner-btn">立即选购 →</router-link>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 金刚区分类入口 -->
    <section class="category-zone page-container">
      <div class="category-grid">
        <div v-for="c in categories" :key="c.id" class="category-entry" @click="$router.push(`/products?categoryId=${c.id}`)">
          <div class="category-icon-wrap">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 01-8 0"/></svg>
          </div>
          <span class="category-name">{{ c.name }}</span>
        </div>
      </div>
    </section>

    <!-- 临期特惠 -->
    <section class="floor floor-accent">
      <div class="page-container">
        <div class="section-heading">
          <h3>临期特惠</h3>
          <router-link to="/products?expiryStatus=1" class="more-link">查看更多 →</router-link>
        </div>
        <div class="product-grid">
          <div v-for="p in nearExpiryProducts" :key="p.id" class="product-card card" :class="{'promo-card': p.originalPrice && p.price < p.originalPrice}" @click="$router.push(`/product/${p.id}`)">
            <div class="product-img">
              <img :src="getImageUrl(p.images ? p.images.split(',')[0] : null)" :alt="p.name" />
              <span class="badge-expiry near">临期特惠</span>
              <div class="promo-ribbon" v-if="p.originalPrice && p.price < p.originalPrice">
                <span>{{ (p.price / p.originalPrice * 10).toFixed(1) }}折</span>
              </div>
            </div>
            <div class="product-info">
              <h4 class="product-name">{{ p.name }}</h4>
              <div class="price-row">
                <span class="price">¥{{ p.price }}</span>
                <span class="original-price" v-if="p.originalPrice && p.price < p.originalPrice">¥{{ p.originalPrice }}</span>
                <span class="promo-badge" v-if="p.originalPrice && p.price < p.originalPrice">促销</span>
              </div>
              <div class="product-meta">已售 {{ p.sales || 0 }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新商品 -->
    <section class="floor">
      <div class="page-container">
        <div class="section-heading">
          <h3>新品上架</h3>
          <router-link to="/products" class="more-link">查看更多 →</router-link>
        </div>
        <div class="product-grid">
          <div v-for="p in latestProducts" :key="p.id" class="product-card card" :class="{'promo-card': p.expiryStatus===1 && p.originalPrice && p.price < p.originalPrice}" @click="$router.push(`/product/${p.id}`)">
            <div class="product-img">
              <img :src="getImageUrl(p.images ? p.images.split(',')[0] : null)" :alt="p.name" />
              <span class="badge-expiry" :class="p.expiryStatus === 1 ? 'near' : 'normal'">{{ p.expiryStatus === 1 ? '临期' : '正常' }}</span>
              <div class="promo-ribbon" v-if="p.expiryStatus===1 && p.originalPrice && p.price < p.originalPrice">
                <span>{{ (p.price / p.originalPrice * 10).toFixed(1) }}折</span>
              </div>
            </div>
            <div class="product-info">
              <h4 class="product-name">{{ p.name }}</h4>
              <div class="price-row">
                <span class="price">¥{{ p.price }}</span>
                <span class="original-price" v-if="p.originalPrice && p.price < p.originalPrice">¥{{ p.originalPrice }}</span>
                <span class="promo-badge" v-if="p.expiryStatus===1 && p.originalPrice && p.price < p.originalPrice">促销</span>
              </div>
              <div class="product-meta">已售 {{ p.sales || 0 }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 热销榜单 -->
    <section class="floor floor-warm">
      <div class="page-container">
        <div class="section-heading">
          <h3>热销榜单</h3>
          <router-link to="/products?sortField=sales&sortOrder=desc" class="more-link">查看更多 →</router-link>
        </div>
        <div class="product-grid">
          <div v-for="(p, idx) in hotProducts" :key="p.id" class="product-card card" :class="{'promo-card': p.expiryStatus===1 && p.originalPrice && p.price < p.originalPrice}" @click="$router.push(`/product/${p.id}`)">
            <div class="product-img">
              <img :src="getImageUrl(p.images ? p.images.split(',')[0] : null)" :alt="p.name" />
              <span class="rank-badge" v-if="idx < 3">TOP{{ idx + 1 }}</span>
              <span class="badge-expiry" :class="p.expiryStatus === 1 ? 'near' : 'normal'">{{ p.expiryStatus === 1 ? '临期' : '正常' }}</span>
              <div class="promo-ribbon" v-if="p.expiryStatus===1 && p.originalPrice && p.price < p.originalPrice">
                <span>{{ (p.price / p.originalPrice * 10).toFixed(1) }}折</span>
              </div>
            </div>
            <div class="product-info">
              <h4 class="product-name">{{ p.name }}</h4>
              <div class="price-row">
                <span class="price">¥{{ p.price }}</span>
                <span class="original-price" v-if="p.originalPrice && p.price < p.originalPrice">¥{{ p.originalPrice }}</span>
                <span class="promo-badge" v-if="p.expiryStatus===1 && p.originalPrice && p.price < p.originalPrice">促销</span>
              </div>
              <div class="product-meta">已售 {{ p.sales || 0 }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { getImageUrl } from '../utils/image'

const banners = ref([])
const categories = ref([])
const nearExpiryProducts = ref([])
const latestProducts = ref([])
const hotProducts = ref([])

onMounted(async () => {
  try { const r = await request.get('/api/banner/list'); banners.value = r.data || [] } catch {}
  try { const r = await request.get('/api/category/list'); categories.value = r.data || [] } catch {}
  try { const r = await request.get('/api/product/list', { params: { expiryStatus: 1, size: 10 } }); nearExpiryProducts.value = r.data?.records || [] } catch {}
  try { const r = await request.get('/api/product/list', { params: { size: 10 } }); latestProducts.value = r.data?.records || [] } catch {}
  try { const r = await request.get('/api/product/list', { params: { size: 10, sortField: 'sales', sortOrder: 'desc' } }); hotProducts.value = r.data?.records || [] } catch {}
})
</script>

<style scoped>
/* ===== Banner ===== */
.banner-section { background: var(--bg-white); }
.banner-item {
  height: 460px; position: relative; overflow: hidden;
  background: linear-gradient(135deg, #3D2B1F 0%, #6B4C3B 40%, #C8956C 100%);
}
.banner-default {
  background: linear-gradient(135deg, #3D2B1F 0%, #6B4C3B 50%, #C8956C 100%) !important;
}
.banner-overlay {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: #fff; background: rgba(0,0,0,0.15);
  text-align: center;
}
.banner-tag {
  display: inline-block; padding: 6px 20px; border: 1px solid rgba(255,255,255,0.6);
  border-radius: 20px; font-size: 13px; letter-spacing: 2px; margin-bottom: 16px;
}
.banner-overlay h2 {
  font-size: 40px; font-weight: 300; letter-spacing: 6px;
  text-shadow: 0 2px 12px rgba(0,0,0,0.3);
}
.banner-overlay p {
  margin-top: 12px; font-size: 16px; letter-spacing: 2px;
  color: rgba(255,255,255,0.85);
}
.banner-btn {
  margin-top: 28px; padding: 12px 36px; background: var(--color-primary);
  color: #fff; border-radius: 30px; font-size: 14px; letter-spacing: 1px;
  transition: all 0.3s; font-weight: 500;
}
.banner-btn:hover { background: #fff; color: var(--color-primary-dark); }

/* ===== 金刚区 ===== */
.category-zone { padding-top: 32px; padding-bottom: 8px; }
.category-grid {
  display: flex; justify-content: center; gap: 12px; flex-wrap: wrap;
}
.category-entry {
  display: flex; flex-direction: column; align-items: center; gap: 10px;
  width: 100px; padding: 16px 8px; cursor: pointer; border-radius: var(--radius-md);
  transition: all 0.3s;
}
.category-entry:hover { background: var(--color-primary-bg); }
.category-entry:hover .category-icon-wrap { background: var(--color-primary); color: #fff; }
.category-icon-wrap {
  width: 52px; height: 52px; border-radius: 14px;
  background: var(--bg-section); color: var(--color-primary-dark);
  display: flex; align-items: center; justify-content: center;
  transition: all 0.3s;
}
.category-name { font-size: 13px; color: var(--text-regular); font-weight: 500; }

/* ===== 楼层 ===== */
.floor { padding: 12px 0; }
.floor-accent { background: var(--color-primary-bg); }
.floor-warm { background: var(--color-gold-light); }

/* ===== 商品网格 ===== */
.product-grid {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px;
}
.product-card { cursor: pointer; border: 1px solid var(--border-light); }
.product-card:hover { border-color: var(--color-primary-light); }
.product-img {
  position: relative; height: 220px; overflow: hidden;
  background: var(--bg-section);
}
.product-img img {
  width: 100%; height: 100%; object-fit: cover;
  transition: transform 0.4s ease;
}
.product-card:hover .product-img img { transform: scale(1.06); }
.product-img .badge-expiry { position: absolute; top: 10px; left: 10px; }
.discount-tag {
  position: absolute; top: 10px; right: 0;
  background: var(--color-accent); color: #fff;
  padding: 3px 10px 3px 12px; font-size: 11px; font-weight: 700;
  border-radius: 4px 0 0 4px; letter-spacing: 0.5px;
}
.rank-badge {
  position: absolute; top: 0; right: 10px;
  background: linear-gradient(180deg, #B8860B, #D4A843);
  color: #fff; padding: 4px 8px; font-size: 11px; font-weight: 700;
  border-radius: 0 0 6px 6px; letter-spacing: 0.5px;
}
.product-info { padding: 14px; }
.product-name {
  font-size: 14px; color: var(--text-primary); font-weight: 500;
  margin-bottom: 8px; line-height: 1.4;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.price-row { display: flex; align-items: baseline; gap: 8px; }
.price { font-size: 20px; font-weight: 700; color: var(--color-accent); }
.original-price { font-size: 12px; color: var(--text-secondary); text-decoration: line-through; }
.product-meta { font-size: 12px; color: var(--text-secondary); margin-top: 6px; }

/* ===== 促销标识 ===== */
.promo-card { }
.promo-ribbon {
  position: absolute; top: 0; right: 0;
  background: linear-gradient(135deg, #FF5722, #E53935);
  color: #fff; padding: 4px 14px 4px 18px;
  font-size: 13px; font-weight: 700;
  border-radius: 0 0 0 12px;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 6px rgba(229,57,53,0.3);
}
.promo-badge {
  display: inline-block; background: #E53935; color: #fff;
  font-size: 10px; font-weight: 700; padding: 1px 6px;
  border-radius: 3px; margin-left: 6px; vertical-align: middle;
  white-space: nowrap; flex-shrink: 0;
  animation: promoPulse 2s ease-in-out infinite;
}
@keyframes promoPulse { 0%,100%{ opacity:1 } 50%{ opacity:0.7 } }

@media (max-width: 1200px) { .product-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 900px) { .product-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 600px) { .product-grid { grid-template-columns: repeat(2, 1fr); } }
</style>
