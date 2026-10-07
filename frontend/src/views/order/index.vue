<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="未支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="退款" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="studentName" label="学员" width="100" />
        <el-table-column prop="courseTitle" label="课程" min-width="180" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="讲师" width="90" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">￥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" width="170" />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { pageOrder } from '@/api/order'

const query = reactive({ status: undefined as number | undefined, pageNum: 1, pageSize: 10 })
const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageOrder(query)
    list.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
const handleSearch = () => {
  query.pageNum = 1
  loadData()
}
const resetSearch = () => {
  query.status = undefined
  handleSearch()
}

const statusText = (s: number) => ({ 0: '未支付', 1: '已支付', 2: '退款' }[s] || '未知')
const statusType = (s: number) => ({ 0: 'warning', 1: 'success', 2: 'info' }[s] || 'info')

loadData()
</script>
