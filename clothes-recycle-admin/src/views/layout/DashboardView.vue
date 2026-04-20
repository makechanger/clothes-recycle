<template>
  <div class="dashboard">
    <!-- 概览卡片 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6" v-for="card in overviewCards" :key="card.label">
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
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span class="chart-title">近7天订单趋势</span></template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span class="chart-title">订单状态分布</span></template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="chart-title">衣物品类分布</span></template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="chart-title">回收员接单排行 Top5</span></template>
          <div ref="rankingChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, markRaw } from 'vue'
import { User, List, Coin, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getStatistics } from '../../api/statistics'

// 概览数据
const overview = ref({})

// 概览卡片配置
const overviewCards = computed(() => [
  { label: '用户总数', value: overview.value.userCount ?? '-', icon: markRaw(User), color: '#409eff' },
  { label: '订单总数', value: overview.value.orderCount ?? '-', icon: markRaw(List), color: '#67c23a' },
  { label: '已完成订单', value: overview.value.completedOrderCount ?? '-', icon: markRaw(TrendCharts), color: '#e6a23c' },
  { label: '累计发放积分', value: overview.value.totalPointsAwarded ?? '-', icon: markRaw(Coin), color: '#f56c6c' }
])

// 图表 DOM 引用
const trendChartRef = ref(null)
const statusChartRef = ref(null)
const categoryChartRef = ref(null)
const rankingChartRef = ref(null)

// 图表实例
let trendChart = null
let statusChart = null
let categoryChart = null
let rankingChart = null

/** 初始化订单趋势折线图 */
function initTrendChart(data) {
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(d => d.date), boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '订单数',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#409eff' },
      data: data.map(d => d.count)
    }]
  })
}

/** 初始化状态分布饼图 */
function initStatusChart(data) {
  statusChart = echarts.init(statusChartRef.value)
  statusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'middle' },
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

/** 初始化品类分布柱状图 */
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

/** 初始化回收员排行柱状图（横向） */
function initRankingChart(data) {
  rankingChart = echarts.init(rankingChartRef.value)
  const names = data.map(d => d.name).reverse()
  const counts = data.map(d => d.count).reverse()
  rankingChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '10%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: names },
    series: [{
      name: '接单数',
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#e6a23c' },
          { offset: 1, color: '#f5d89a' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right' },
      data: counts
    }]
  })
}

/** 窗口 resize 自适应 */
function handleResize() {
  trendChart?.resize()
  statusChart?.resize()
  categoryChart?.resize()
  rankingChart?.resize()
}

onMounted(async () => {
  try {
    const data = await getStatistics()

    // 概览卡片
    overview.value = data.overview || {}

    // 等待 DOM 渲染后初始化图表
    await nextTick()
    initTrendChart(data.orderTrend || [])
    initStatusChart(data.orderStatusDistribution || [])
    initCategoryChart(data.categoryDistribution || [])
    initRankingChart(data.collectorRanking || [])

    window.addEventListener('resize', handleResize)
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  statusChart?.dispose()
  categoryChart?.dispose()
  rankingChart?.dispose()
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
