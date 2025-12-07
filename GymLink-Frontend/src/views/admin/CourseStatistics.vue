<template>
  <AdminLayout>
    <div class="statistics-container">
      <!-- 顶部统计卡片 -->
      <el-row :gutter="20" class="stat-row">
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #409EFF;"><el-icon :size="24"><Reading /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalCourse || 0 }}</div>
                <div class="stat-label">课程总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #67C23A;"><el-icon :size="24"><ShoppingCart /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.todayOrderCount || 0 }}</div>
                <div class="stat-label">今日购买</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #E6A23C;"><el-icon :size="24"><Calendar /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.weekOrderCount || 0 }}</div>
                <div class="stat-label">本周购买</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #F56C6C;"><el-icon :size="24"><DataLine /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.monthOrderCount || 0 }}</div>
                <div class="stat-label">本月购买</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #909399;"><el-icon :size="24"><Money /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">¥{{ formatMoney(statistics.totalRevenue) }}</div>
                <div class="stat-label">总销售额</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #b37feb;"><el-icon :size="24"><TrendCharts /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">¥{{ formatMoney(statistics.monthRevenue) }}</div>
                <div class="stat-label">本月销售额</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域第一行 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="16">
          <el-card class="chart-card">
            <template #header><span>近7天购买趋势</span></template>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header><span>课程难度分布</span></template>
            <div ref="difficultyPieRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域第二行 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header><span>热门课程 TOP10</span></template>
            <div ref="courseBarRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header><span>热门教练 TOP10</span></template>
            <div ref="coachBarRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { Reading, ShoppingCart, Calendar, DataLine, Money, TrendCharts } from '@element-plus/icons-vue'
import AdminLayout from '@/components/AdminLayout.vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

const statistics = reactive({
  totalCourse: 0,
  todayOrderCount: 0,
  weekOrderCount: 0,
  monthOrderCount: 0,
  totalRevenue: 0,
  monthRevenue: 0,
  difficultyStatistics: [] as any[],
  typeStatistics: [] as any[],
  dailyOrderTrend: [] as any[],
  hotCourseRank: [] as any[],
  hotCoachRank: [] as any[]
})

const trendChartRef = ref<HTMLElement>()
const difficultyPieRef = ref<HTMLElement>()
const courseBarRef = ref<HTMLElement>()
const coachBarRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let difficultyPie: echarts.ECharts | null = null
let courseBar: echarts.ECharts | null = null
let coachBar: echarts.ECharts | null = null

const formatMoney = (value: number | undefined) => {
  if (!value) return '0.00'
  return Number(value).toFixed(2)
}

const loadStatistics = async () => {
  try {
    const res = await request.get('/course/getStatistics')
    Object.assign(statistics, res)
    await nextTick()
    initCharts()
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
}


const initCharts = () => {
  // 趋势图（双Y轴：购买数和销售额）
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    const dates = statistics.dailyOrderTrend.map(item => item.date.substring(5))
    const counts = statistics.dailyOrderTrend.map(item => item.count)
    const revenues = statistics.dailyOrderTrend.map(item => item.revenue || 0)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['购买数', '销售额'], top: 0 },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: dates, boundaryGap: false },
      yAxis: [
        { type: 'value', name: '购买数', minInterval: 1 },
        { type: 'value', name: '销售额(元)', position: 'right' }
      ],
      series: [
        {
          name: '购买数',
          type: 'line',
          smooth: true,
          data: counts,
          areaStyle: { color: 'rgba(64, 158, 255, 0.2)' },
          lineStyle: { color: '#409EFF', width: 3 },
          itemStyle: { color: '#409EFF' }
        },
        {
          name: '销售额',
          type: 'bar',
          yAxisIndex: 1,
          data: revenues,
          itemStyle: { color: '#67C23A', borderRadius: [4, 4, 0, 0] }
        }
      ]
    })
  }

  // 难度分布饼图
  if (difficultyPieRef.value) {
    difficultyPie = echarts.init(difficultyPieRef.value)
    const pieData = statistics.difficultyStatistics.map(item => ({
      name: item.difficultyName || item.difficulty,
      value: item.count
    }))
    difficultyPie.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { orient: 'vertical', right: 10, top: 'center' },
      color: ['#67C23A', '#E6A23C', '#F56C6C'],
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
        data: pieData
      }]
    })
  }

  // 热门课程柱状图
  if (courseBarRef.value) {
    courseBar = echarts.init(courseBarRef.value)
    const names = statistics.hotCourseRank.map(item => item.courseName)
    const values = statistics.hotCourseRank.map(item => item.orderCount)
    courseBar.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: names, axisLabel: { rotate: 30, interval: 0 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        name: '购买次数',
        type: 'bar',
        data: values,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#79bbff' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }]
    })
  }

  // 热门教练柱状图
  if (coachBarRef.value) {
    coachBar = echarts.init(coachBarRef.value)
    const names = statistics.hotCoachRank.map(item => item.coachName)
    const values = statistics.hotCoachRank.map(item => item.orderCount)
    coachBar.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: names, axisLabel: { rotate: 30, interval: 0 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        name: '课程销量',
        type: 'bar',
        data: values,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#E6A23C' },
            { offset: 1, color: '#f3d19e' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }]
    })
  }
}

const handleResize = () => {
  trendChart?.resize()
  difficultyPie?.resize()
  courseBar?.resize()
  coachBar?.resize()
}

onMounted(() => {
  loadStatistics()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  difficultyPie?.dispose()
  courseBar?.dispose()
  coachBar?.dispose()
})
</script>

<style scoped>
.statistics-container { display: flex; flex-direction: column; gap: 20px; }
.stat-row { margin-bottom: 0; }
.stat-card { height: 100px; }
.stat-content { display: flex; align-items: center; height: 60px; }
.stat-icon { width: 50px; height: 50px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; margin-right: 15px; }
.stat-info { flex: 1; }
.stat-value { font-size: 22px; font-weight: bold; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.chart-row { margin-bottom: 0; }
.chart-card { height: 400px; }
.chart-container { width: 100%; height: 320px; }
</style>
