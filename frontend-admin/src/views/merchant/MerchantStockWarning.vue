<template>
  <div>
    <div class="page-title">商品预警</div>
    <el-card>
      <div class="list-filter-bar">
        <el-input v-model="keyword" placeholder="搜索商品名称" clearable prefix-icon="Search" style="max-width:260px" />
      </div>
      <el-tabs v-model="activeTab" style="margin-bottom:12px">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane :label="`低库存 (${lowStockList.length})`" name="lowStock" />
        <el-tab-pane :label="`临期 (${nearExpiryList.length})`" name="nearExpiry" />
        <el-tab-pane :label="`过期 (${expiredList.length})`" name="expired" />
      </el-tabs>
      <el-empty v-if="!displayList.length" description="暂无预警商品" />
      <el-table v-else :data="displayList" stripe :row-class-name="rowClassName">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="商品名称" show-overflow-tooltip />
        <el-table-column label="价格" width="140">
          <template #default="{row}">
            <span style="font-weight:600;color:#E53935">¥{{ row.price }}</span>
            <span v-if="row.originalPrice && row.price < row.originalPrice" style="font-size:11px;color:#999;text-decoration:line-through;margin-left:4px">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="当前库存" width="100">
          <template #default="{row}"><span :style="{color: row.stock <= row.stockWarning ? '#E53935' : '', fontWeight: 600}">{{ row.stock }}</span></template>
        </el-table-column>
        <el-table-column prop="stockWarning" label="预警阈值" width="90" />
        <el-table-column label="临期状态" width="90">
          <template #default="{row}"><el-tag :type="row.expiryStatus===0?'success':row.expiryStatus===1?'warning':'danger'" size="small">{{ ['正常','临期','过期'][row.expiryStatus] }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="到期日期" width="110" />
        <el-table-column label="预警类型" width="140">
          <template #default="{row}">
            <el-tag v-if="row.expiryStatus===2" type="danger" size="small" style="margin-right:4px">过期</el-tag>
            <el-tag v-if="row.stock <= row.stockWarning" type="danger" size="small" style="margin-right:4px">低库存</el-tag>
            <el-tag v-if="row.expiryStatus===1" type="warning" size="small">临期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.expiryStatus!==2" size="small" type="primary" @click="handleRestock(row)">补货</el-button>
            <el-button v-if="row.expiryStatus===1" size="small" type="success" @click="openPromotion(row)">促销</el-button>
            <el-button v-if="row.expiryStatus>=1" size="small" type="warning" @click="offShelf(row)">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 补货弹窗 -->
    <el-dialog v-model="restockVisible" title="商品补货" width="420px">
      <div class="info-block">
        <div class="info-row"><span class="info-label">商品名称</span><span>{{ restockProduct.name }}</span></div>
        <div class="info-row"><span class="info-label">当前库存</span><span :style="{color: restockProduct.stock <= restockProduct.stockWarning ? '#E53935' : '', fontWeight: 600}">{{ restockProduct.stock }}</span></div>
        <div class="info-row"><span class="info-label">预警阈值</span><span>{{ restockProduct.stockWarning }}</span></div>
      </div>
      <el-form label-width="90px" style="margin-top:16px">
        <el-form-item label="补货数量">
          <el-input-number v-model="restockQty" :min="1" :max="99999" style="width:100%" />
        </el-form-item>
        <el-form-item label="补货后库存">
          <span style="font-size:16px;font-weight:700;color:#389E0D">{{ (restockProduct.stock || 0) + restockQty }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="restockVisible=false">取消</el-button>
        <el-button type="primary" @click="submitRestock" :loading="restockLoading">确认补货</el-button>
      </template>
    </el-dialog>

    <!-- 促销弹窗 -->
    <el-dialog v-model="promoVisible" title="临期促销" width="440px">
      <div class="info-block">
        <div class="info-row"><span class="info-label">商品名称</span><span>{{ promoProduct.name }}</span></div>
        <div class="info-row"><span class="info-label">原价</span><span style="text-decoration:line-through;color:#999">¥{{ promoProduct.originalPrice }}</span></div>
        <div class="info-row"><span class="info-label">当前售价</span><span style="font-weight:600">¥{{ promoProduct.price }}</span></div>
        <div class="info-row"><span class="info-label">到期日期</span><span style="color:#D48806">{{ promoProduct.expiryDate }}</span></div>
      </div>
      <el-form label-width="90px" style="margin-top:16px">
        <el-form-item label="促销价格">
          <el-input-number v-model="promoPrice" :min="0.01" :max="promoProduct.originalPrice || 99999" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="折扣力度" v-if="promoProduct.originalPrice > 0">
          <span style="font-size:15px;font-weight:700;color:#E53935">{{ Math.round((1 - promoPrice / promoProduct.originalPrice) * 100) }}% OFF</span>
          <span style="font-size:12px;color:#999;margin-left:8px">（约 {{ (promoPrice / promoProduct.originalPrice * 10).toFixed(1) }} 折）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="promoVisible=false">取消</el-button>
        <el-button type="success" @click="submitPromotion" :loading="promoLoading">确认促销</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([]), keyword = ref(''), activeTab = ref('all')

const lowStockList = computed(() => list.value.filter(i => i.stock <= i.stockWarning))
const nearExpiryList = computed(() => list.value.filter(i => i.expiryStatus === 1))
const expiredList = computed(() => list.value.filter(i => i.expiryStatus === 2))
const displayList = computed(() => {
  let data = list.value
  if (activeTab.value === 'lowStock') data = lowStockList.value
  else if (activeTab.value === 'nearExpiry') data = nearExpiryList.value
  else if (activeTab.value === 'expired') data = expiredList.value
  if (keyword.value) { const k = keyword.value.toLowerCase(); data = data.filter(i => i.name?.toLowerCase().includes(k)) }
  return data
})

function rowClassName({ row }) {
  if (row.expiryStatus === 2) return 'expired-row'
  return ''
}

// ===== 补货 =====
const restockVisible = ref(false)
const restockProduct = reactive({ id: null, name: '', stock: 0, stockWarning: 0 })
const restockQty = ref(50)
const restockLoading = ref(false)

async function handleRestock(row) {
  if (row.expiryStatus === 1) {
    try {
      await ElMessageBox.confirm('该商品属于临期商品，建议下架处理。是否仍要补货？', '临期提醒', { type: 'warning', confirmButtonText: '继续补货', cancelButtonText: '取消' })
    } catch { return }
  }
  Object.assign(restockProduct, { id: row.id, name: row.name, stock: row.stock, stockWarning: row.stockWarning })
  restockQty.value = Math.max(row.stockWarning - row.stock + 10, 10)
  restockVisible.value = true
}

async function submitRestock() {
  if (restockQty.value <= 0) return ElMessage.warning('补货数量必须大于0')
  restockLoading.value = true
  try {
    await request.put(`/api/product/merchant/restock/${restockProduct.id}`, { quantity: restockQty.value })
    ElMessage.success(`已补货 ${restockQty.value} 件`)
    restockVisible.value = false
    await loadData()
  } catch {} finally { restockLoading.value = false }
}

// ===== 促销 =====
const promoVisible = ref(false)
const promoProduct = reactive({ id: null, name: '', price: 0, originalPrice: 0, expiryDate: '' })
const promoPrice = ref(0)
const promoLoading = ref(false)

function openPromotion(row) {
  Object.assign(promoProduct, { id: row.id, name: row.name, price: row.price, originalPrice: row.originalPrice || row.price, expiryDate: row.expiryDate })
  promoPrice.value = row.price
  promoVisible.value = true
}

async function submitPromotion() {
  if (promoPrice.value <= 0) return ElMessage.warning('促销价格必须大于0')
  if (promoProduct.originalPrice && promoPrice.value > promoProduct.originalPrice) return ElMessage.warning('促销价格不能高于原价')
  promoLoading.value = true
  try {
    await request.put(`/api/product/merchant/promote/${promoProduct.id}`, { promotionPrice: promoPrice.value })
    ElMessage.success('促销价格设置成功')
    promoVisible.value = false
    await loadData()
  } catch {} finally { promoLoading.value = false }
}

// ===== 下架 =====
async function offShelf(row) {
  await ElMessageBox.confirm(`确认下架「${row.name}」？`, '下架确认', { type: 'warning' })
  await request.put(`/api/product/merchant/status/${row.id}`)
  ElMessage.success('已下架')
  await loadData()
}

async function loadData() {
  const r = await request.get('/api/merchant/stock/warning')
  list.value = r.data || []
}

onMounted(loadData)
</script>
<style scoped>
.info-block { background: #FAFAFA; border-radius: 8px; padding: 12px 16px; }
.info-row { display: flex; justify-content: space-between; padding: 6px 0; font-size: 13px; color: #666; }
.info-label { color: #999; }
:deep(.expired-row) { background-color: #FFF1F0 !important; }
:deep(.expired-row td) { background-color: #FFF1F0 !important; }
</style>
