<template>
  <section>
    <div class="heading"><div><h2>我的课程</h2><p>我的学习清单 · 共 {{ total }} 门课程</p></div><el-button type="primary" @click="$router.push('/course')">浏览课程</el-button></div>
    <el-alert title="付费课程通过模拟支付加入，免费课程直接加入；演示支付不会真实扣款。" type="info" :closable="false" class="notice" />
    <div v-loading="loading" class="course-list">
      <el-alert v-if="failed" title="清单加载失败" type="error" :closable="false"><el-button link @click="load">重试</el-button></el-alert>
      <el-empty v-else-if="!loading && !courses.length" description="还没有加入课程，去课程页选择感兴趣的内容吧" />
      <div v-else class="grid">
        <el-card v-for="course in courses" :key="course.id" shadow="hover">
          <div class="card-top"><el-tag>{{ course.category || '综合课程' }}</el-tag><el-tag v-if="course.status !== 1" type="info">暂不可学习</el-tag></div>
          <h3>{{ course.title }}</h3><p class="teacher">讲师：{{ course.teacherName || '待安排' }}</p>
          <p class="intro">{{ course.intro || '暂无课程简介' }}</p>
          <div class="actions"><el-button type="primary" :disabled="course.status !== 1" @click="learn(course)">开始学习</el-button><el-button :loading="removing === course.id" @click="remove(course)">移出清单</el-button></div>
        </el-card>
      </div>
    </div>
    <div class="pagination-wrap"><el-pagination v-model:current-page="page" :total="total" :page-size="12" layout="total, prev, pager, next" @current-change="load" /></div>
    <CourseChapters v-model="learning" :course="currentCourse" />
  </section>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myCourses, removeMyCourse } from '@/api/personal'
import CourseChapters from '@/components/CourseChapters.vue'
const courses = ref<any[]>([]), total = ref(0), page = ref(1)
const loading = ref(false), failed = ref(false), learning = ref(false)
const removing = ref<number>(), currentCourse = ref<any>(null)
let requestId = 0
const load = async () => {
  const request = ++requestId
  loading.value = true; failed.value = false
  try { const res = await myCourses({ pageNum: page.value, pageSize: 12 }); if (request === requestId) { courses.value = res.data.rows; total.value = res.data.total } }
  catch { if (request === requestId) failed.value = true }
  finally { if (request === requestId) loading.value = false }
}
const learn = (course: any) => { currentCourse.value = course; learning.value = true }
const remove = async (course: any) => {
  try { await ElMessageBox.confirm(`将「${course.title}」移出个人学习清单？课程内容不会被删除。`, '移出清单', { type: 'warning' }) } catch { return }
  removing.value = course.id
  try { await removeMyCourse(course.id); ElMessage.success('已移出清单'); if (courses.value.length === 1 && page.value > 1) page.value--; await load() }
  catch { /* 请求拦截器显示错误 */ } finally { removing.value = undefined }
}
void load()
</script>
<style scoped>
.heading,.card-top,.actions{display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap}.heading{margin-bottom:20px}.heading p,.teacher{color:#777;margin-top:8px}.notice{margin-bottom:20px}.course-list{min-height:240px}.grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(min(100%,280px),1fr));gap:20px}h3{margin-top:18px}.intro{color:#666;line-height:1.8;margin:16px 0;display:-webkit-box;-webkit-line-clamp:3;-webkit-box-orient:vertical;overflow:hidden;min-height:76px}.actions{justify-content:flex-start}
</style>
