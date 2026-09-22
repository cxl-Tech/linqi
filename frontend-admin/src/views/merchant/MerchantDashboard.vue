<template>
  <div>
    <div class="page-title">数据看板</div>
    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover"><div style="display:flex;align-items:center;gap:16px">
          <div class="stat-icon orange"><el-icon :size="22"><Goods /></el-icon></div>
          <el-statistic title="商品总数" :value="stats.productCount || 0" />
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div style="display:flex;align-items:center;gap:16px">
          <div class="stat-icon blue"><el-icon :size="22"><List /></el-icon></div>
          <el-statistic title="订单总数" :value="stats.orderCount || 0" />
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div style="display:flex;align-items:center;gap:16px">
          <div class="stat-icon brown"><el-icon :size="22"><Coin /></el-icon></div>
          <el-statistic title="销售总额" :value="stats.totalSales || 0" prefix="¥" />
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" :body-style="{background: warningTotal>0?'#FFF7ED':''}">
          <div style="display:flex;align-items:center;gap:16px">
            <div class="stat-icon red"><el-icon :size="22"><WarningFilled /></el-icon></div>
            <div>
              <el-statistic title="预警商品" :value="warningTotal" />
              <div style="font-size:11px;color:#8C8C8C;margin-top:2px">临期 {{ stats.nearExpiryCount||0 }} / 低库存 {{ stats.lowStockCount||0 }} / 过期 {{ stats.expiredCount||0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待处理事项 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="8">
        <el-card shadow="hover" class="todo-card">
          <div style="display:flex;align-items:center;gap:12px;cursor:pointer" @click="$router.push('/merchant/orders')">
            <div class="stat-icon blue" style="width:38px;height:38px;font-size:18px"><el-icon><Van /></el-icon></div>
            <div><div style="font-size:24px;font-weight:700;color:var(--admin-text-primary)">{{ stats.pendingShipCount || 0 }}</div><div style="font-size:12px;color:var(--admin-text-secondary)">待发货订单</div></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="todo-card">
          <div style="display:flex;align-items:center;gap:12px;cursor:pointer" @click="$router.push('/merchant/aftersale')">
            <div class="stat-icon purple" style="width:38px;height:38px;font-size:18px"><el-icon><Service /></el-icon></div>
            <div><div style="font-size:24px;font-weight:700;color:var(--admin-text-primary)">{{ stats.pendingAfterSale || 0 }}</div><div style="font-size:12px;color:var(--admin-text-secondary)">待处理售后</div></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="todo-card">
          <div style="display:flex;align-items:center;gap:12px;cursor:pointer" @click="$router.push('/merchant/stock-warning')">
            <div class="stat-icon red" style="width:38px;height:38px;font-size:18px"><el-icon><WarningFilled /></el-icon></div>
            <div><div style="font-size:24px;font-weight:700;color:var(--admin-text-primary)">{{ warningTotal }}</div><div style="font-size:12px;color:var(--admin-text-secondary)">商品预警</div></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="16">
        <el-card shadow="hover">
          <div style="font-weight:600;font-size:15px;margin-bottom:12px">近7天销售趋势</div>
          <div ref="trendChartRef" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div style="font-weight:600;font-size:15px;margin-bottom:12px">订单状态分布</div>
          <div ref="pieChartRef" style="height:300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="24">
        <el-card shadow="hover">
          <div style="font-weight:600;font-size:15px;margin-bottom:12px">商品销量 TOP5</div>
          <div ref="barChartRef" style="height:280px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import request from '../../utils/request'
import * as echarts from 'echarts'
import { Goods, List, Coin, WarningFilled, Van, Service } from '@element-plus/icons-vue'

const stats = ref({})
const warningTotal = computed(() => stats.value.warningCount || 0)
const trendChartRef = ref(null)
const pieChartRef = ref(null)
const barChartRef = ref(null)

onMounted(async () => {
  try {
    const r = await request.get('/api/merchant/stats')
    stats.value = r.data || {}
    await nextTick()
    renderTrendChart()
    renderPieChart()
    renderBarChart()
  } catch {}
})

function renderTrendChart() {
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  const trend = stats.value.salesTrend || []
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>销售额: ¥{c}' },
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: trend.map(i => i.date?.substring(5)), axisLabel: { color: '#8C8C8C' } },
    yAxis: { type: 'value', axisLabel: { color: '#8C8C8C', formatter: '¥{value}' }, splitLine: { lineStyle: { type: 'dashed', color: '#F0E8E0' } } },
    series: [{
      type: 'line', data: trend.map(i => i.amount), smooth: true,
      itemStyle: { color: '#C8956C' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(200,149,108,0.3)' }, { offset: 1, color: 'rgba(200,149,108,0.02)' }]) },
      lineStyle: { width: 2.5 }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function renderPieChart() {
  if (!pieChartRef.value) return
  const chart = echarts.init(pieChartRef.value)
  const dist = stats.value.orderStatusDist || []
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { fontSize: 11, color: '#8C8C8C' } },
    color: ['#F59E0B', '#3B82F6', '#8B5CF6', '#10B981', '#9CA3AF'],
    series: [{
      type: 'pie', radius: ['40%', '65%'], center: ['50%', '45%'],
      label: { show: false }, emphasis: { label: { show: true, fontWeight: 'bold' } },
      data: dist
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function renderBarChart() {
  if (!barChartRef.value) return
  const chart = echarts.init(barChartRef.value)
  const rank = stats.value.productRank || []
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>销量: {c}' },
    grid: { left: 120, right: 30, top: 10, bottom: 20 },
    xAxis: { type: 'value', axisLabel: { color: '#8C8C8C' }, splitLine: { lineStyle: { type: 'dashed', color: '#F0E8E0' } } },
    yAxis: { type: 'category', data: rank.map(i => i.name).reverse(), axisLabel: { color: '#4A4A4A', width: 100, overflow: 'truncate' } },
    series: [{
      type: 'bar', data: rank.map(i => i.sales).reverse(), barWidth: 20,
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#C8956C' }, { offset: 1, color: '#E8D5C4' }]), borderRadius: [0, 4, 4, 0] }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}
</script>

<style scoped>
.todo-card:hover { transform: translateY(-2px); transition: transform 0.2s; }
</style>
