<template>
  <div>
    <div class="page-title">商品管理</div>
    <el-card>
      <div class="list-filter-bar">
        <el-input v-model="keyword" placeholder="搜索商品名称" clearable prefix-icon="Search" />
        <el-select v-model="statusFilter" placeholder="上架状态" clearable @change="onFilterChange">
          <el-option label="已上架" :value="1" /><el-option label="已下架" :value="0" />
        </el-select>
        <el-select v-model="expiryFilter" placeholder="临期状态" clearable @change="onFilterChange">
          <el-option label="正常" :value="0" /><el-option label="临期" :value="1" /><el-option label="过期" :value="2" />
        </el-select>
        <el-button type="primary" @click="showDialog(null)">新增商品</el-button>
      </div>
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="name" label="商品名称" show-overflow-tooltip />
        <el-table-column prop="price" label="售价"><template #default="{row}">¥{{ row.price }}</template></el-table-column>
        <el-table-column prop="originalPrice" label="原价"><template #default="{row}">¥{{ row.originalPrice }}</template></el-table-column>
        <el-table-column prop="stock" label="库存">
          <template #default="{row}"><span :style="{color: row.stock <= row.stockWarning ? '#E53935':''}">{{ row.stock }}</span></template>
        </el-table-column>
        <el-table-column label="临期状态">
          <template #default="{row}">
            <el-tag :type="row.expiryStatus===0?'success':row.expiryStatus===1?'warning':'danger'" size="small">{{ ['正常','临期','过期'][row.expiryStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核">
          <template #default="{row}">
            <el-tag :type="row.auditStatus===1?'success':row.auditStatus===2?'danger':'info'" size="small">{{ ['待审核','已通过','已驳回'][row.auditStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上架">
          <template #default="{row}"><el-switch :model-value="row.status===1" @change="toggleStatus(row)" /></template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{row}">
            <el-button size="small" @click="showDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="currentPage" @current-change="loadData" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id?'编辑商品':'新增商品'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="商品名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width:100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="售价"><el-input-number v-model="form.price" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="原价"><el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="预警阈值"><el-input-number v-model="form.stockWarning" :min="0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="生产日期"><el-date-picker v-model="form.productionDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="保质期(天)"><el-input-number v-model="form.shelfLifeDays" :min="1" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="商品图片">
          <ImageUpload v-model="form.images" :limit="5" />
        </el-form-item>
        <el-form-item label="产地"><el-input v-model="form.origin" /></el-form-item>
        <el-form-item label="厂家"><el-input v-model="form.manufacturer" /></el-form-item>
        <el-form-item label="商品介绍"><el-input v-model="form.description" type="textarea" rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageUpload from '../../components/ImageUpload.vue'

const list = ref([]), total = ref(0), currentPage = ref(1), pageSize = ref(10), keyword = ref('')
const categories = ref([]), dialogVisible = ref(false), statusFilter = ref(null), expiryFilter = ref(null)
let _timer = null
watch(keyword, () => { clearTimeout(_timer); _timer = setTimeout(() => { currentPage.value = 1; loadData() }, 300) })
function onFilterChange() { currentPage.value = 1; loadData() }
const form = reactive({ id: null, name: '', categoryId: null, price: 0, originalPrice: 0, stock: 0, stockWarning: 10, productionDate: '', shelfLifeDays: 180, images: '', origin: '', manufacturer: '', description: '' })

async function loadData() {
  const params = { current: currentPage.value, size: pageSize.value }
  if (keyword.value) params.keyword = keyword.value
  if (statusFilter.value != null) params.status = statusFilter.value
  const r = await request.get('/api/product/merchant/list', { params })
  let data = r.data?.records || []
  if (expiryFilter.value != null) data = data.filter(i => i.expiryStatus === expiryFilter.value)
  list.value = data; total.value = r.data?.total || 0
}
function showDialog(row) {
  if (row) { Object.assign(form, row) } else {
    form.id = null; form.name = ''; form.categoryId = null; form.price = 0; form.originalPrice = 0; form.stock = 0; form.stockWarning = 10; form.productionDate = ''; form.shelfLifeDays = 180; form.images = ''; form.origin = ''; form.manufacturer = ''; form.description = ''
  }
  dialogVisible.value = true
}
async function save() {
  if (!form.name || !form.categoryId || !form.productionDate) return ElMessage.warning('请填写必要信息')
  if (!form.id && form.productionDate && form.shelfLifeDays) {
    const expiryDate = new Date(form.productionDate)
    expiryDate.setDate(expiryDate.getDate() + form.shelfLifeDays)
    if (expiryDate <= new Date()) return ElMessage.error('该商品已过期，无法新增过期商品')
  }
  if (form.id) { await request.put('/api/product/merchant/update', form) } else { await request.post('/api/product/merchant/add', form) }
  ElMessage.success('保存成功'); dialogVisible.value = false; loadData()
}
async function toggleStatus(row) { await request.put(`/api/product/merchant/status/${row.id}`); ElMessage.success('已切换'); loadData() }
async function del(id) { await ElMessageBox.confirm('确认删除？'); await request.delete(`/api/product/merchant/${id}`); ElMessage.success('已删除'); loadData() }

onMounted(async () => {
  loadData()
  try { const r = await request.get('/api/category/list'); categories.value = r.data || [] } catch {}
})
</script>
