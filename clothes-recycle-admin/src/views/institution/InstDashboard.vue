<template>
  <div class="dashboard">
    <!-- 概览卡片 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="5" v-for="card in overviewCards" :key="card.label">
        <el-card shadow="hover" class="overview-card">
          <div class="card-icon" :style="{ background: card.color }">
            <el-icon :size="28"><component :is="card.icon" /></el-icon>
          </div>
          <div class="card-info">
            <div class="card-value">{{ card.value }}</div>
            <div class="card-label">{{ card.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header><span class="chart-title">近7天接收趋势</span></template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header><span class="chart-title">衣物去向分布</span></template>
          <div ref="destChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span class="chart-title">衣物品类分布</span></template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, markRaw } from 'vue'
import { List, Coin, Box, SetUp, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getInstitutionStatistics } from '../../api/institution'

const overview = ref({})

const overviewCards = computed(() => [
  { label: '接收总量', value: overview.value.totalOrders ?? '-', icon: markRaw(List), color: '#409eff' },
  { label: '总重量(kg)', value: overview.value.totalWeight ?? '-', icon: markRaw(Box), color: '#67c23a' },
  { label: '发放积分', value: overview.value.totalPoints ?? '-', icon: markRaw(Coin), color: '#e6a23c' },
  { label: '已分配去向', value: overview.value.assignedCount ?? '-', icon: markRaw(SetUp), color: '#909399' },
  { label: '待分配去向', value: overview.value.unassignedCount ?? '-', icon: markRaw(TrendCharts), color: '#f56c6c' }
])

const trendChartRef = ref(null)
const destChartRef = ref(null)
const categoryChartRef = ref(null)

let trendChart = null
let destChart = null
let categoryChart = null

function initTrendChart(data) {
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.date), boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '接收订单数',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#409eff' },
      data: data.map(d => d.count)
    }]
  })
}

function initDestChart(data) {
  destChart = echarts.init(destChartRef.value)
  destChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'middle' },
    color: ['#67c23a', '#409eff', '#e6a23c', '#f56c6c'],
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: data
    }]
  })
}

function initCategoryChart(data) {
  categoryChart = echarts.init(categoryChartRef.value)
  categoryChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.name) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '订单数',
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#67c23a' },
          { offset: 1, color: '#b3e19d' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      data: data.map(d => d.value)
    }]
  })
}

function handleResize() {
  trendChart?.resize()
  destChart?.resize()
  categoryChart?.resize()
}

onMounted(async () => {
  try {
    const data = await getInstitutionStatistics()
    overview.value = data.overview || {}

    await nextTick()
    initTrendChart(data.orderTrend || [])
    initDestChart(data.destinationDistribution || [])
    initCategoryChart(data.categoryDistribution || [])

    window.addEventListener('resize', handleResize)
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  destChart?.dispose()
  categoryChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.overview-row {
  margin-bottom: 20px;
}

.overview-card {
  display: flex;
  align-items: center;
  padding: 0;
}

.overview-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 20px;
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.card-info {
  margin-left: 16px;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.card-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 350px;
}
</style>
