<template>
  <AdminLayout>
    <div class="statistics-container">
      <!-- 顶部统计卡片 -->
      <el-row :gutter="20" class="stat-row">
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #409EFF;"><el-icon :size="24"><SetUp /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.totalEquipment || 0 }}</div>
                <div class="stat-label">器材总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #67C23A;"><el-icon :size="24"><CircleCheck /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.normalCount || 0 }}</div>
                <div class="stat-label">正常运行</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #E6A23C;"><el-icon :size="24"><Warning /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.maintenanceCount || 0 }}</div>
                <div class="stat-label">维护中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #F56C6C;"><el-icon :size="24"><Calendar /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.todayReservationCount || 0 }}</div>
                <div class="stat-label">今日预约</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #909399;"><el-icon :size="24"><DataLine /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.weekReservationCount || 0 }}</div>
                <div class="stat-label">本周预约</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <div class="stat-icon" style="background: #b37feb;"><el-icon :size="24"><TrendCharts /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ statistics.monthReservationCount || 0 }}</div>
                <div class="stat-label">本月预约</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="16">
          <el-card class="chart-card">
            <template #header><span>近7天预约趋势</span></template>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="chart-card">
            <template #header><span>器材类型分布</span></template>
            <div ref="pieChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 热门器材排行 -->
      <el-row :gutter="20" class="rank-row">
        <el-col :span="24">
          <el-card class="rank-card">
            <template #header><span>热门器材 TOP10</span></template>
            <div ref="barChartRef" class="chart-container" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { SetUp, CircleCheck, Warning, Calendar, DataLine, TrendCharts } from '@element-plus/icons-vue'
import AdminLayout from '@/components/AdminLayout.vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

const statistics = reactive({
  totalEquipment: 0,
  normalCount: 0,
  maintenanceCount: 0,
  todayReservationCount: 0,
  weekReservationCount: 0,
  monthReservationCount: 0,
  typeStatistics: [] as any[],
  dailyReservationTrend: [] as any[],
  hotEquipmentRank: [] as any[]
})

const trendChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null

const loadStatistics = async () => {
  try {
    const res = await request.get('/equipment/getStatistics')
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
    const dates = statistics.dailyReservationTrend.map(item => item.date.substring(5))
    const counts = statistics.dailyReservationTrend.map(item => item.count)
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

  // 饼图
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    const pieData = statistics.typeStatistics.map(item => ({
      name: item.typeName || item.type,
      value: item.count
    }))
    pieChart.setOption({
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

  // 柱状图
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
    const names = statistics.hotEquipmentRank.map(item => item.equipmentName)
    const values = statistics.hotEquipmentRank.map(item => item.reservationCount)
    barChart.setOption({
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
}

const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
  barChart?.resize()
}

onMounted(() => {
  loadStatistics()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.statistics-container { display: flex; flex-direction: column; gap: 20px; }
.stat-row { margin-bottom: 0; }
.stat-card { height: 100px; }
.stat-content { display: flex; align-items: center; height: 60px; }
.stat-icon { width: 50px; height: 50px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; margin-right: 15px; }
.stat-info { flex: 1; }
.stat-value { font-size: 24px; font-weight: bold; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.chart-row { margin-bottom: 0; }
.chart-card { height: 400px; }
.chart-container { width: 100%; height: 320px; }
.rank-row { margin-bottom: 0; }
.rank-card { height: auto; }
</style>
