<template>
  <AdminLayout>
    <div class="statistics-container">
      <!-- 顶部统计卡片 -->
      <el-row :gutter="20" class="stat-row">
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #409EFF;"><el-icon :size="24"><Avatar /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalCoach || 0 }}</div>
                <div class="stat-label">教练总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #67C23A;"><el-icon :size="24"><Male /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.maleCount || 0 }}</div>
                <div class="stat-label">男教练</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #F56C6C;"><el-icon :size="24"><Female /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.femaleCount || 0 }}</div>
                <div class="stat-label">女教练</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #E6A23C;"><el-icon :size="24"><Calendar /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.todayAppointmentCount || 0 }}</div>
                <div class="stat-label">今日预约</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #909399;"><el-icon :size="24"><Clock /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.pendingAppointmentCount || 0 }}</div>
                <div class="stat-label">待确认</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #b37feb;"><el-icon :size="24"><CircleCheck /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.confirmedAppointmentCount || 0 }}</div>
                <div class="stat-label">已确认</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域第一行 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="16">
          <el-card class="chart-card">
            <template #header><span>近7天预约趋势</span></template>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header><span>教练专长分布</span></template>
            <div ref="specialtyPieRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域第二行 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header><span>热门教练 TOP10</span></template>
            <div ref="coachBarRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header><span>教练年龄分布</span></template>
            <div ref="agePieRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { Avatar, Male, Female, Calendar, Clock, CircleCheck } from '@element-plus/icons-vue'
import AdminLayout from '@/components/AdminLayout.vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

const statistics = reactive({
  totalCoach: 0,
  maleCount: 0,
  femaleCount: 0,
  todayAppointmentCount: 0,
  weekAppointmentCount: 0,
  monthAppointmentCount: 0,
  pendingAppointmentCount: 0,
  confirmedAppointmentCount: 0,
  specialtyStatistics: [] as any[],
  dailyAppointmentTrend: [] as any[],
  hotCoachRank: [] as any[],
  ageDistribution: [] as any[]
})

const trendChartRef = ref<HTMLElement>()
const specialtyPieRef = ref<HTMLElement>()
const coachBarRef = ref<HTMLElement>()
const agePieRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let specialtyPie: echarts.ECharts | null = null
let coachBar: echarts.ECharts | null = null
let agePie: echarts.ECharts | null = null

const loadStatistics = async () => {
  try {
    const res = await request.get('/coach/getStatistics')
    Object.assign(statistics, res)
    await nextTick()
    initCharts()
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
}


const initCharts = () => {
  // 趋势图
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    const dates = statistics.dailyAppointmentTrend.map(item => item.date.substring(5))
    const counts = statistics.dailyAppointmentTrend.map(item => item.count)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: dates, boundaryGap: false },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        name: '预约数',
        type: 'line',
        smooth: true,
        data: counts,
        areaStyle: { color: 'rgba(64, 158, 255, 0.2)' },
        lineStyle: { color: '#409EFF', width: 3 },
        itemStyle: { color: '#409EFF' }
      }]
    })
  }

  // 专长分布饼图
  if (specialtyPieRef.value) {
    specialtyPie = echarts.init(specialtyPieRef.value)
    const pieData = statistics.specialtyStatistics.map(item => ({
      name: item.specialty,
      value: item.count
    }))
    specialtyPie.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { orient: 'vertical', right: 10, top: 'center' },
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

  // 热门教练柱状图
  if (coachBarRef.value) {
    coachBar = echarts.init(coachBarRef.value)
    const names = statistics.hotCoachRank.map(item => item.coachName)
    const values = statistics.hotCoachRank.map(item => item.appointmentCount)
    coachBar.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: names, axisLabel: { rotate: 30, interval: 0 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        name: '预约次数',
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

  // 年龄分布饼图
  if (agePieRef.value) {
    agePie = echarts.init(agePieRef.value)
    const pieData = statistics.ageDistribution.map(item => ({
      name: item.ageRange,
      value: item.count
    }))
    agePie.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { orient: 'vertical', right: 10, top: 'center' },
      color: ['#67C23A', '#409EFF', '#E6A23C', '#F56C6C', '#909399'],
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
}

const handleResize = () => {
  trendChart?.resize()
  specialtyPie?.resize()
  coachBar?.resize()
  agePie?.resize()
}

onMounted(() => {
  loadStatistics()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  specialtyPie?.dispose()
  coachBar?.dispose()
  agePie?.dispose()
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
