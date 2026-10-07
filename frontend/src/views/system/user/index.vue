<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="用户名">
          <el-input v-model="query.keyword" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <el-button v-if="hasPermi('system:user:add')" type="primary" :icon="Plus" @click="openDialog()">新增用户</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">{{ roleName(row.roleId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.status === 0 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasPermi('system:user:edit')" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button
              v-if="hasPermi('system:user:remove') && row.username !== 'admin'"
              link
              type="danger"
              @click="handleDelete(row)"
            >删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码">
          <el-input v-model="form.password" placeholder="默认 123456" />
        </el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
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
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageUser, addUser, updateUser, deleteUser, listRole } from '@/api/user'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPermi = (p: string) => userStore.hasPermi(p)

const query = reactive({ keyword: '', pageNum: 1, pageSize: 10 })
const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const roles = ref<any[]>([])

const roleName = (roleId: number) => roles.value.find((r) => r.id === roleId)?.roleName || '-'

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageUser(query)
    list.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
const loadRoles = async () => {
  const res = await listRole()
  roles.value = res.data
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
  Object.assign(form, row ? { ...row } : { status: 0, password: '123456' })
  dialogVisible.value = true
}
const submitForm = async () => {
  if (!form.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (form.id) {
    await updateUser(form)
  } else {
    await addUser(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}
const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}

loadRoles()
loadData()
</script>

<style scoped>
.table-toolbar {
  margin-bottom: 14px;
}
</style>
