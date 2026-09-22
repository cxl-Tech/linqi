<template>
  <div>
    <div class="page-title">评价管理</div>
    <el-card>
      <div class="list-filter-bar">
        <el-input v-model="keyword" placeholder="搜索商品名称/评价内容" clearable prefix-icon="Search" />
      </div>
      <el-table :data="filteredList" stripe>
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="productId" label="商品ID" />
        <el-table-column label="商品名称"><template #default="{row}"><span>{{ productMap[row.productId] || '-' }}</span></template></el-table-column>
        <el-table-column prop="rating" label="评分"><template #default="{row}"><el-rate :model-value="row.rating" disabled /></template></el-table-column>
        <el-table-column prop="content" label="评价内容" show-overflow-tooltip />
        <el-table-column label="追评内容" show-overflow-tooltip>
          <template #default="{row}"><span v-if="row.appendContent" style="color:#1890FF">{{ row.appendContent }}</span><span v-else style="color:#ccc">—</span></template>
        </el-table-column>
        <el-table-column prop="merchantReply" label="商家回复" show-overflow-tooltip>
          <template #default="{row}"><span v-if="row.merchantReply">{{ row.merchantReply }}</span><span v-else style="color:#999">未回复</span></template>
        </el-table-column>
        <el-table-column prop="createTime" label="评价时间" />
        <el-table-column label="操作" width="100"><template #default="{row}"><el-button size="small" @click="showReply(row)">回复</el-button></template></el-table-column>
      </el-table>
      <el-pagination style="margin-top:16px;justify-content:flex-end" background layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="currentPage" @current-change="loadData" />
    </el-card>
    <el-dialog v-model="replyVisible" title="回复评价" width="500px">
      <div style="margin-bottom:12px;padding:10px 14px;background:#f9f9f9;border-radius:6px;font-size:13px">
        <div style="color:#666;margin-bottom:4px"><b>原评价：</b>{{ replyRow?.content || '无文字评价' }}</div>
        <div v-if="replyRow?.appendContent" style="color:#1890FF;margin-top:6px"><b>追加评价：</b>{{ replyRow.appendContent }}</div>
      </div>
      <el-input v-model="replyContent" type="textarea" rows="4" placeholder="输入回复内容（可针对追评进行回复）" />
      <template #footer><el-button @click="replyVisible=false">取消</el-button><el-button type="primary" @click="submitReply">提交</el-button></template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
const list = ref([]), total = ref(0), currentPage = ref(1), replyVisible = ref(false), replyId = ref(null), replyContent = ref(''), replyRow = ref(null)
const productMap = ref({}), keyword = ref('')
const filteredList = computed(() => { if (!keyword.value) return list.value; const k = keyword.value.toLowerCase(); return list.value.filter(i => (productMap.value[i.productId] || '').toLowerCase().includes(k) || i.content?.toLowerCase().includes(k)) })
async function loadData() {
  try {
    const r = await request.get('/api/review/merchant/list', { params: { current: currentPage.value, size: 10 } })
    list.value = r.data?.records || []
    total.value = r.data?.total || 0
    const ids = [...new Set(list.value.map(i => i.productId))]
    for (const id of ids) {
      if (!productMap.value[id]) {
        try { const p = await request.get(`/api/product/detail/${id}`); productMap.value[id] = p.data?.name || '-' } catch { productMap.value[id] = '-' }
      }
    }
  } catch {}
}
function showReply(row) { replyId.value = row.id; replyRow.value = row; replyContent.value = row.merchantReply || ''; replyVisible.value = true }
async function submitReply() { if (!replyContent.value) return ElMessage.warning('请输入回复内容'); await request.put(`/api/review/reply/${replyId.value}`, { merchantReply: replyContent.value }); ElMessage.success('回复成功'); replyVisible.value = false; loadData() }
onMounted(loadData)
</script>
