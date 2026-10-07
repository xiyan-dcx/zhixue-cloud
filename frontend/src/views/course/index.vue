<template>
  <div>
    <div class="search-bar">
      <el-form :inline="true">
        <el-form-item label="课程名称">
          <el-input v-model="query.keyword" placeholder="请输入课程名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="草稿" :value="0" />
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <el-button v-if="hasPermi('course:add')" type="primary" :icon="Plus" @click="openDialog()">
          新增课程
        </el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="封面" width="90">
          <template #default="{ row }">
            <el-image
              v-if="row.cover"
              :src="row.cover"
              fit="cover"
              style="width: 56px; height: 38px; border-radius: 4px"
            />
            <span v-else class="no-cover">无</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="课程标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="讲师" width="90" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="price" label="价格" width="90">
          <template #default="{ row }">￥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="studentCount" label="报名数" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button v-if="userStore.roleKey === 'student' && row.status === 1" link type="success" :loading="joiningId === row.id" @click="joinCourse(row)">{{ purchasedIds.includes(row.id) ? '已开通 · 恢复清单' : Number(row.price) > 0 ? '购买课程' : '免费加入' }}</el-button>
            <el-button v-if="userStore.roleKey !== 'student' || Number(row.price) === 0" link type="primary" @click="openChapter(row)">{{ userStore.roleKey === 'student' ? '开始学习' : '章节管理' }}</el-button>
            <el-button v-if="hasPermi('course:edit')" link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button
              v-if="hasPermi('course:edit') && row.status !== 1"
              link
              type="success"
              @click="changeStatus(row, 1)"
            >上架</el-button>
            <el-button
              v-if="hasPermi('course:edit') && row.status === 1"
              link
              type="warning"
              @click="changeStatus(row, 2)"
            >下架</el-button>
            <el-button v-if="hasPermi('course:remove')" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <!-- 新增/编辑 课程 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑课程' : '新增课程'" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="封面">
          <el-upload
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="onCoverSuccess"
            accept="image/*"
            class="cover-uploader"
          >
            <img v-if="form.cover" :src="form.cover" class="cover-img" />
            <el-icon v-else class="cover-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入课程标题" />
        </el-form-item>
        <el-form-item label="讲师">
          <el-input v-model="form.teacherName" placeholder="讲师姓名" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="如 后端 / 前端" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="课程简介" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="2">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 章节管理 -->
    <CourseChapters v-model="chapterVisible" :course="currentCourse" />
    <el-dialog v-model="purchaseVisible" title="确认购买课程" width="min(480px, 92vw)" :close-on-click-modal="false" :show-close="!joiningId" :close-on-press-escape="!joiningId">
      <el-alert title="演示支付 · 不会真实扣款" type="warning" :closable="false" show-icon />
      <div class="purchase-summary"><h3>{{ purchaseCourse?.title }}</h3><p>讲师：{{ purchaseCourse?.teacherName || '待安排' }}</p><div class="purchase-price"><span>应付金额</span><strong>¥ {{ Number(purchaseCourse?.price || 0).toFixed(2) }}</strong></div><p>确认后将生成模拟订单，并加入“我的课程”。</p></div>
      <template #footer><el-button :disabled="!!joiningId" @click="purchaseVisible = false">取消</el-button><el-button type="primary" :loading="!!joiningId" @click="confirmPurchase">确认模拟支付</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageCourse,
  addCourse,
  updateCourse,
  deleteCourse,
  changeCourseStatus
} from '@/api/course'
import { useUserStore } from '@/store/user'
import { getToken } from '@/utils/auth'
import CourseChapters from '@/components/CourseChapters.vue'
import { joinMyCourse, purchasedCourseIds, restoreMyCourse } from '@/api/personal'

const userStore = useUserStore()
const joiningId = ref<number>()
const purchasedIds = ref<number[]>([])
const purchaseVisible = ref(false)
const purchaseCourse = ref<any>(null)
const joinCourse = async (course: any) => {
  if (joiningId.value) return
  if (purchasedIds.value.includes(course.id)) {
    joiningId.value = course.id
    try { await restoreMyCourse(course.id); ElMessage.success('已恢复到我的课程，无需再次购买') }
    catch { /* 统一错误提示 */ } finally { joiningId.value = undefined }
    return
  }
  if (Number(course.price) > 0) { purchaseCourse.value = { ...course }; purchaseVisible.value = true; return }
  await completePurchase(course)
}
const confirmPurchase = async () => {
  if (!purchaseCourse.value || joiningId.value) return
  await completePurchase(purchaseCourse.value, true)
}
const completePurchase = async (course: any, confirmed = false) => {
  joiningId.value = course.id
  try {
    await joinMyCourse(course.id, confirmed ? { confirmed: true, expectedAmount: Number(course.price) } : undefined)
    purchasedIds.value.push(course.id)
    purchaseVisible.value = false
    ElMessage.success(Number(course.price) > 0 ? '模拟支付成功，课程已加入我的课程' : '免费课程已加入我的课程')
  }
  catch { /* 统一错误提示 */ } finally { joiningId.value = undefined }
}
const hasPermi = (p: string) => userStore.hasPermi(p)

const uploadAction = '/api/file/upload'
const uploadHeaders = { Authorization: `Bearer ${getToken()}` }

const query = reactive({ keyword: '', status: undefined as number | undefined, pageNum: 1, pageSize: 10 })
const list = ref<any[]>([])
const total = ref(0)
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageCourse(query)
    if (userStore.roleKey === 'student') purchasedIds.value = (await purchasedCourseIds()).data || []
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
  query.status = undefined
  handleSearch()
}

const statusText = (s: number) => ({ 0: '草稿', 1: '上架', 2: '下架' }[s] || '未知')
const statusType = (s: number) => ({ 0: 'info', 1: 'success', 2: 'warning' }[s] || 'info')

const dialogVisible = ref(false)
const form = reactive<any>({})
const openDialog = (row?: any) => {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { status: 1, price: 0, studentCount: 0 })
  }
  dialogVisible.value = true
}
const submitForm = async () => {
  if (!form.title) {
    ElMessage.warning('请输入课程标题')
    return
  }
  if (form.id) {
    await updateCourse(form)
  } else {
    await addCourse(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}
const onCoverSuccess = (res: any) => {
  form.cover = res.data.url
}

const changeStatus = async (row: any, status: number) => {
  await changeCourseStatus(row.id, status)
  ElMessage.success('操作成功')
  loadData()
}
const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(`确定删除课程「${row.title}」吗？`, '提示', { type: 'warning' })
  await deleteCourse(row.id)
  ElMessage.success('删除成功')
  loadData()
}

// 章节
const chapterVisible = ref(false)
const currentCourse = ref<any>(null)
const openChapter = (row: any) => {
  currentCourse.value = row
  chapterVisible.value = true
}

loadData()
</script>

<style scoped>
.purchase-summary { padding-top: 24px; }
.purchase-summary h3 { font-size: 20px; margin-bottom: 12px; }
.purchase-summary p { color: #8a91a2; font-size: 13px; line-height: 1.8; }
.purchase-price { display: flex; justify-content: space-between; align-items: center; padding: 22px 0; margin: 12px 0; border-block: 1px solid #edf0f6; }
.purchase-price strong { color: #6366f1; font-size: 28px; }
.table-toolbar {
  margin-bottom: 14px;
}
.no-cover {
  color: #c0c4cc;
  font-size: 12px;
}
.cover-uploader :deep(.el-upload) {
  width: 120px;
  height: 80px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.cover-img {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}
.cover-icon {
  font-size: 24px;
  color: #8c939d;
}
.chapter-form {
  margin-top: 16px;
}
</style>
