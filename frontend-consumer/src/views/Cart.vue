<template>
  <div class="cart-page page-container">
    <div class="page-title">
      <h2>我的购物车</h2>
      <span class="cart-count" v-if="cartList.length">共 {{ cartList.length }} 件商品</span>
    </div>

    <el-empty v-if="!cartList.length" description="购物车空空如也~" :image-size="120">
      <button class="btn-go-shop" @click="$router.push('/products')">去逛逛</button>
    </el-empty>

    <template v-else>
      <div class="cart-table card-static">
        <div class="table-header">
          <span class="col-check"></span>
          <span class="col-product">商品信息</span>
          <span class="col-price">单价</span>
          <span class="col-qty">数量</span>
          <span class="col-subtotal">小计</span>
          <span class="col-action">操作</span>
        </div>
        <div v-for="item in cartList" :key="item.id" class="table-row">
          <span class="col-check">
            <input type="checkbox" :checked="isSelected(item)" @change="toggleSelect(item)" class="custom-check" />
          </span>
          <div class="col-product" @click="$router.push(`/product/${item.productId}`)" style="cursor:pointer">
            <div class="product-thumb-wrap">
              <img v-if="item.product?.images" :src="getImageUrl(item.product.images.split(',')[0])" class="product-thumb" />
              <div v-else class="product-thumb-empty">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
              </div>
            </div>
            <div class="product-brief">
              <span class="product-name">{{ item.product?.name || '商品已下架' }}</span>
              <div class="product-tags">
                <span class="tag-expiry" v-if="item.product?.expiryStatus===1">临期特惠</span>
                <span class="tag-expiry normal" v-else-if="item.product?.expiryStatus===0">正常保质</span>
              </div>
              <div class="product-origin" v-if="item.product?.origin || item.product?.manufacturer">
                {{ item.product?.manufacturer || item.product?.origin }}
              </div>
            </div>
          </div>
          <div class="col-price">
            <span class="price-current">¥{{ item.product?.price || 0 }}</span>
            <span class="price-original" v-if="item.product?.originalPrice && item.product.originalPrice > item.product.price">¥{{ item.product.originalPrice }}</span>
          </div>
          <span class="col-qty">
            <el-input-number v-model="item.quantity" :min="1" :max="item.product?.stock||99" size="small" @change="updateQty(item)" />
          </span>
          <span class="col-subtotal price-text">¥{{ ((item.product?.price||0) * item.quantity).toFixed(2) }}</span>
          <span class="col-action">
            <button class="btn-remove" @click.stop="removeItem(item.id)" title="删除">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
            </button>
          </span>
        </div>
      </div>

      <!-- 结算栏 -->
      <div class="checkout-bar card-static">
        <div class="checkout-left">
          <label class="select-all">
            <input type="checkbox" :checked="selected.length === cartList.length && cartList.length > 0" @change="toggleAll" class="custom-check" />
            全选
          </label>
          <span class="selected-info">已选 <b>{{ selected.length }}</b> 件</span>
        </div>
        <div class="checkout-right">
          <div class="total-text">合计：<span class="total-price">¥{{ totalPrice }}</span></div>
          <button class="btn-checkout" :disabled="!selected.length" @click="showCheckout=true">去结算</button>
        </div>
      </div>
    </template>

    <el-dialog v-model="showCheckout" title="确认订单" width="520px">
      <el-form label-width="80px">
        <el-form-item label="收货地址">
          <el-select v-model="selectedAddress" placeholder="请选择收货地址" style="width:100%">
            <el-option v-for="a in addresses" :key="a.id" :label="`${a.receiverName} ${a.phone} - ${a.province}${a.city}${a.district}${a.detail}`" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="remark" type="textarea" rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCheckout=false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="submitting">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const cartList = ref([])
const selected = ref([])
const showCheckout = ref(false)
const addresses = ref([])
const selectedAddress = ref(null)
const remark = ref('')
const submitting = ref(false)

const totalPrice = computed(() => selected.value.reduce((s, r) => s + (r.product?.price || 0) * r.quantity, 0).toFixed(2))

function isSelected(item) { return selected.value.some(s => s.id === item.id) }
function toggleSelect(item) {
  if (isSelected(item)) { selected.value = selected.value.filter(s => s.id !== item.id) }
  else { selected.value = [...selected.value, item] }
}
function toggleAll(e) { selected.value = e.target.checked ? [...cartList.value] : [] }

async function loadCart() {
  const res = await request.get('/api/cart/list')
  cartList.value = res.data || []
}

async function updateQty(row) { await request.put(`/api/cart/update/${row.id}`, { quantity: row.quantity }) }
async function removeItem(id) { await request.delete(`/api/cart/${id}`); selected.value = selected.value.filter(s => s.id !== id); loadCart() }

async function submitOrder() {
  const invalid = selected.value.filter(s => !s.product)
  if (invalid.length) return ElMessage.error('已选商品中包含已下架或过期商品，请移除后重试')
  if (!selectedAddress.value) return ElMessage.warning('请选择收货地址')
  submitting.value = true
  try {
    await request.post('/api/order/create', { cartIds: selected.value.map(s => s.id), addressId: selectedAddress.value, remark: remark.value })
    ElMessage.success('下单成功')
    showCheckout.value = false
    router.push('/orders')
  } finally { submitting.value = false }
}

onMounted(async () => {
  loadCart()
  try { const r = await request.get('/api/address/list'); addresses.value = r.data || [] } catch {}
})
</script>

<style scoped>
.page-title { display: flex; align-items: baseline; gap: 12px; margin-bottom: 20px; }
.page-title h2 { font-size: 22px; font-weight: 700; color: var(--text-primary); }
.cart-count { font-size: 13px; color: var(--text-secondary); }

.btn-go-shop {
  padding: 10px 32px; background: var(--color-primary); color: #fff;
  border: none; border-radius: var(--radius-sm); cursor: pointer;
  font-size: 14px; transition: background 0.2s;
}
.btn-go-shop:hover { background: var(--color-primary-dark); }

/* ===== 购物车表格 ===== */
.cart-table { margin-bottom: 16px; }
.table-header, .table-row {
  display: flex; align-items: center; padding: 16px 20px;
}
.table-header {
  background: var(--bg-section); font-size: 13px; font-weight: 600;
  color: var(--text-secondary); border-bottom: 1px solid var(--border-light);
}
.table-row { border-bottom: 1px solid var(--border-light); }
.table-row:last-child { border-bottom: none; }
.table-row:hover { background: var(--color-primary-bg); }

.col-check { width: 40px; flex-shrink: 0; }
.col-product { flex: 1; display: flex; align-items: center; gap: 16px; min-width: 0; }
.col-price { width: 110px; text-align: center; flex-shrink: 0; display: flex; flex-direction: column; align-items: center; gap: 2px; }
.col-qty { width: 140px; text-align: center; flex-shrink: 0; }
.col-subtotal { width: 110px; text-align: center; flex-shrink: 0; }
.col-action { width: 60px; text-align: center; flex-shrink: 0; }

.product-thumb-wrap { width: 88px; height: 88px; border-radius: 10px; overflow: hidden; flex-shrink: 0; background: var(--bg-section); }
.product-thumb { width: 100%; height: 100%; object-fit: cover; }
.product-thumb-empty { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #f8f5f2; }
.product-brief { display: flex; flex-direction: column; gap: 6px; min-width: 0; }
.product-name {
  font-size: 14px; color: var(--text-primary); font-weight: 600;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.product-tags { display: flex; gap: 6px; }
.tag-expiry {
  font-size: 10px; padding: 2px 8px; border-radius: 3px;
  background: var(--color-accent-light); color: var(--color-accent); font-weight: 600;
}
.tag-expiry.normal { background: #E8F5E9; color: #2E7D32; }
.product-origin { font-size: 12px; color: var(--text-secondary); }
.price-current { font-size: 16px; font-weight: 700; color: var(--color-accent); }
.price-original { font-size: 12px; color: var(--text-secondary); text-decoration: line-through; }
.price-text { font-size: 16px; font-weight: 700; color: var(--color-accent); }

.custom-check {
  width: 16px; height: 16px; accent-color: var(--color-primary); cursor: pointer;
}

.btn-remove {
  background: none; border: none; cursor: pointer; color: var(--text-secondary);
  padding: 4px; border-radius: 4px; transition: all 0.2s;
}
.btn-remove:hover { color: var(--color-accent); background: var(--color-accent-light); }

/* ===== 结算栏 ===== */
.checkout-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 24px; position: sticky; bottom: 0; z-index: 10;
}
.checkout-left { display: flex; align-items: center; gap: 16px; }
.select-all { display: flex; align-items: center; gap: 6px; font-size: 13px; color: var(--text-regular); cursor: pointer; }
.selected-info { font-size: 13px; color: var(--text-secondary); }

.checkout-right { display: flex; align-items: center; gap: 24px; }
.total-text { font-size: 14px; color: var(--text-regular); }
.total-price { font-size: 26px; font-weight: 800; color: var(--color-accent); }
.btn-checkout {
  padding: 12px 40px; background: var(--color-primary); color: #fff;
  border: none; border-radius: var(--radius-sm); font-size: 16px; font-weight: 600;
  cursor: pointer; letter-spacing: 1px; transition: all 0.2s;
}
.btn-checkout:hover { background: var(--color-primary-dark); }
.btn-checkout:disabled { background: var(--border-base); cursor: not-allowed; }
</style>
