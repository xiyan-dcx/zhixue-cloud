<template>
  <div class="dashboard">
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <div class="stat-card" :style="{ background: card.bg }">
          <div class="stat-icon"><el-icon :size="28"><component :is="card.icon" /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">近30天报名趋势</div>
          <div ref="trendRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">课程销量 Top10</div>
          <div ref="topRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">各讲师营收占比</div>
          <div ref="teacherRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="chart-card">
          <div class="chart-title">月营收及环比（窗口函数 LAG）</div>
          <div ref="monthRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, shallowRef } from 'vue'
import * as echarts from 'echarts'
import {
  getOverview,
  getEnrollTrend,
  getCourseTop10,
  getTeacherRevenue,
  getMonthRevenue
} from '@/api/dashboard'

const cards = reactive([
  { label: '课程总数', value: 0, icon: 'Reading', bg: 'linear-gradient(135deg,#6366f1,#8b5cf6)' },
  { label: '学员总数', value: 0, icon: 'User', bg: 'linear-gradient(135deg,#06b6d4,#3b82f6)' },
  { label: '订单总数', value: 0, icon: 'List', bg: 'linear-gradient(135deg,#f59e0b,#ef4444)' },
  { label: '累计营收(元)', value: 0, icon: 'Wallet', bg: 'linear-gradient(135deg,#10b981,#22c55e)' }
])

const trendRef = ref<HTMLElement>()
const topRef = ref<HTMLElement>()
const teacherRef = ref<HTMLElement>()
const monthRef = ref<HTMLElement>()
const charts = shallowRef<echarts.ECharts[]>([])

const initChart = (el: HTMLElement | undefined, option: echarts.EChartsOption) => {
  if (!el) return
  const chart = echarts.init(el)
  chart.setOption(option)
  charts.value.push(chart)
}

onMounted(async () => {
  const overview = await getOverview()
  cards[0].value = overview.data.courseCount
  cards[1].value = overview.data.studentCount
  cards[2].value = overview.data.orderCount
  cards[3].value = overview.data.totalRevenue

  const trend = await getEnrollTrend()
  initChart(trendRef.value, {
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: trend.data.map((i: any) => i.day) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '报名数',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.2 },
        itemStyle: { color: '#6366f1' },
        data: trend.data.map((i: any) => i.cnt)
      }
    ]
  })

  const top = await getCourseTop10()
  initChart(topRef.value, {
    tooltip: { trigger: 'axis' },
    grid: { left: 120, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: top.data.map((i: any) => i.courseTitle).reverse() },
    series: [
      {
        type: 'bar',
        data: top.data.map((i: any) => i.cnt).reverse(),
        itemStyle: { color: '#3b82f6', borderRadius: [0, 6, 6, 0] }
      }
    ]
  })

  const teacher = await getTeacherRevenue()
  initChart(teacherRef.value, {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        data: teacher.data.map((i: any) => ({ name: i.teacherName, value: i.revenue }))
      }
    ]
  })

  const month = await getMonthRevenue()
  initChart(monthRef.value, {
    tooltip: { trigger: 'axis' },
    legend: { data: ['营收', '环比%'], top: 0 },
    grid: { left: 50, right: 50, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: month.data.map((i: any) => i.month) },
    yAxis: [
      { type: 'value', name: '营收' },
      { type: 'value', name: '环比%', axisLabel: { formatter: '{value}%' } }
    ],
    series: [
      {
        name: '营收',
        type: 'bar',
        data: month.data.map((i: any) => i.revenue),
        itemStyle: { color: '#10b981', borderRadius: [6, 6, 0, 0] }
      },
      {
        name: '环比%',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        itemStyle: { color: '#f59e0b' },
        data: month.data.map((i: any) => i.growthRate)
      }
    ]
  })
})
</script>

<style scoped>
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  height: 96px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 22px;
  gap: 16px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}
.stat-icon {
  width: 54px;
  height: 54px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
}
.stat-label {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 4px;
}
.chart-row {
  margin-bottom: 16px;
}
.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 18px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.04);
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 10px;
}
.chart {
  height: 300px;
}
</style>
