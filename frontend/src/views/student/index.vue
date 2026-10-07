<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="姓名">
          <el-input v-model="query.keyword" placeholder="请输入姓名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <el-button v-if="hasPermi('student:add')" type="primary" :icon="Plus" @click="openDialog()">新增学员</el-button>
        <el-upload
          v-if="hasPermi('student:import')"
          :action="importAction"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="onImportSuccess"
          accept=".xlsx,.xls"
          style="display: inline-block; margin: 0 10px"
        >
          <el-button :icon="Upload" type="success" plain>导入</el-button>
        </el-upload>
        <el-button v-if="hasPermi('student:import')" :icon="Download" plain @click="handleTemplate">下载模板</el-button>
        <el-button :icon="Download" plain @click="handleExport">导出</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.status === 0 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasPermi('student:edit')" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="hasPermi('student:remove')" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学员' : '新增学员'" width="460px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="0" :inactive-value="1" active-text="正常" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Search, Refresh, Plus, Upload, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageStudent,
  addStudent,
  updateStudent,
  deleteStudent,
  exportStudent,
  downloadTemplate
} from '@/api/student'
import { useUserStore } from '@/store/user'
import { getToken } from '@/utils/auth'

const userStore = useUserStore()
const hasPermi = (p: string) => userStore.hasPermi(p)

const importAction = '/api/business/student/import'
const uploadHeaders = { Authorization: `Bearer ${getToken()}` }

const query = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageStudent(query)
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
  query.keyword = ''
  handleSearch()
}

const dialogVisible = ref(false)
const form = reactive<any>({})
const openDialog = (row?: any) => {
  Object.keys(form).forEach((k) => delete form[k])
  Object.assign(form, row ? { ...row } : { gender: '男', status: 0 })
  dialogVisible.value = true
}
const submitForm = async () => {
  if (!form.name) {
    ElMessage.warning('请输入姓名')
    return
  }
  if (form.id) {
    await updateStudent(form)
  } else {
    await addStudent(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}
const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(`确定删除学员「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteStudent(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const saveBlob = (data: Blob, filename: string) => {
  const url = window.URL.createObjectURL(data)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  window.URL.revokeObjectURL(url)
}
const handleExport = async () => {
  const res = await exportStudent()
  saveBlob(res.data, '学员列表.xlsx')
}
const handleTemplate = async () => {
  const res = await downloadTemplate()
  saveBlob(res.data, '学员导入模板.xlsx')
}
const onImportSuccess = (res: any) => {
  if (res.code === 200) {
    ElMessage.success(res.data || '导入成功')
  } else {
    ElMessage.error(res.msg || '导入失败')
  }
  loadData()
}

loadData()
</script>

<style scoped>
.table-toolbar {
  margin-bottom: 14px;
}
</style>
