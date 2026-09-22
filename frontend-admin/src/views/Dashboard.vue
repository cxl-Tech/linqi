<template>
  <div>
    <div class="page-title">数据看板</div>

    <!-- 核心指标卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display:flex;align-items:center;gap:16px">
            <div class="stat-icon blue"><el-icon :size="22"><User /></el-icon></div>
            <el-statistic title="用户数" :value="stats.userCount||0" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display:flex;align-items:center;gap:16px">
            <div class="stat-icon green"><el-icon :size="22"><Shop /></el-icon></div>
            <el-statistic title="商家数" :value="stats.merchantCount||0" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display:flex;align-items:center;gap:16px">
            <div class="stat-icon orange"><el-icon :size="22"><Goods /></el-icon></div>
            <el-statistic title="商品数" :value="stats.productCount||0" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display:flex;align-items:center;gap:16px">
            <div class="stat-icon red"><el-icon :size="22"><List /></el-icon></div>
            <el-statistic title="订单数" :value="stats.orderCount||0" />
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="8">
        <el-card shadow="hover">
          <div style="display:flex;align-items:center;gap:16px">
            <div class="stat-icon brown"><el-icon :size="22"><Coin /></el-icon></div>
            <el-statistic title="总销售额" :value="stats.totalSales||0" prefix="¥" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" :body-style="{background:(stats.pendingMerchant||0)>0?'#FFF7ED':''}">
          <div style="display:flex;align-items:center;gap:16px">
            <div class="stat-icon orange"><el-icon :size="22"><Clock /></el-icon></div>
            <el-statistic title="待审核商家" :value="stats.pendingMerchant||0" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" :body-style="{background:(stats.nearExpiryProduct||0)>0?'#FEF2F2':''}">
          <div style="display:flex;align-items:center;gap:16px">
            <div class="stat-icon red"><el-icon :size="22"><WarningFilled /></el-icon></div>
            <el-statistic title="临期商品" :value="stats.nearExpiryProduct||0" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="16" style="margin-top:20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span style="font-weight:600">订单状态分布</span></template>
          <div ref="orderPieRef" style="height:320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span style="font-weight:600">分类销量分布</span></template>
          <div ref="categoryPieRef" style="height:320px"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span style="font-weight:600">商品销量 TOP 10</span></template>
          <div ref="rankBarRef" style="height:360px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'
import { User, Shop, Goods, List, Coin, Clock, WarningFilled } from '@element-plus/icons-vue'

const stats = ref({})
const orderPieRef = ref(null)
const categoryPieRef = ref(null)
const rankBarRef = ref(null)

function initPie(el, title, data, colors) {
  const chart = echarts.init(el)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    color: colors,
    series: [{
      type: 'pie', radius: ['40%', '70%'],
      label: { show: true, formatter: '{b}\n{d}%' },
      emphasis: { label: { fontSize: 14, fontWeight: 'bold' } },
      data
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function initBar(el, names, values) {
  const chart = echarts.init(el)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 180, right: 40, top: 20, bottom: 30 },
    xAxis: { type: 'value', name: '销量' },
    yAxis: { type: 'category', data: names.reverse(), axisLabel: { width: 160, overflow: 'truncate' } },
    series: [{
      type: 'bar', data: values.reverse(), barWidth: 20,
      itemStyle: { borderRadius: [0, 4, 4, 0], color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
        { offset: 0, color: '#409EFF' }, { offset: 1, color: '#67C23A' }
      ]) }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

onMounted(async () => {
  try {
    const r = await request.get('/api/admin/stats')
    stats.value = r.data || {}
    await nextTick()

    if (stats.value.orderStatusDist) {
      initPie(orderPieRef.value, '订单状态', stats.value.orderStatusDist,
        ['#E6A23C', '#909399', '#409EFF', '#67C23A', '#F56C6C'])
    }
    if (stats.value.categorySales) {
      initPie(categoryPieRef.value, '分类销量', stats.value.categorySales,
        ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4'])
    }
    if (stats.value.productRank) {
      const names = stats.value.productRank.map(p => p.name)
      const values = stats.value.productRank.map(p => p.sales)
      initBar(rankBarRef.value, names, values)
    }
  } catch {}
})
</script>
