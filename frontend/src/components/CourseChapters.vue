<template>
  <el-dialog :model-value="modelValue" :title="`${canEdit ? '章节管理' : '课程学习'} · ${course?.title || ''}`"
    width="min(960px, 94vw)" @update:model-value="$emit('update:modelValue', $event)" @closed="editorVisible = false">
    <div class="chapter-summary">
      <span>共 {{ chapters.length }} 章 · 预计 {{ totalMinutes }} 分钟</span>
      <el-button v-if="canEdit" type="primary" @click="editChapter()">新增章节</el-button>
    </div>
    <p v-if="course?.intro" class="course-intro">{{ course.intro }}</p>
    <div v-loading="loading">
      <el-alert v-if="loadFailed" title="章节加载失败" type="error" :closable="false">
        <el-button link @click="loadChapters">重新加载</el-button>
      </el-alert>
      <el-empty v-else-if="!loading && !chapters.length" description="暂未发布章节内容" />
      <el-collapse v-else v-model="activeChapter" accordion>
        <el-collapse-item v-for="chapter in chapters" :key="chapter.id" :name="chapter.id">
          <template #title>
            <div class="chapter-title"><strong>{{ chapter.title }}</strong><span>{{ chapter.duration }} 分钟</span></div>
          </template>
          <div v-if="canEdit" class="chapter-actions">
            <span>排序：{{ chapter.sortOrder }}</span>
            <el-button link type="primary" @click="editChapter(chapter)">编辑讲义</el-button>
            <el-button link type="danger" :disabled="deletingId === chapter.id" @click="removeChapter(chapter)">删除章节</el-button>
          </div>
          <article v-if="chapter.content" class="lecture">{{ chapter.content }}</article>
          <el-empty v-else description="本章讲义尚未录入" :image-size="70" />
        </el-collapse-item>
      </el-collapse>
    </div>
    <el-dialog v-model="editorVisible" :title="form.id ? '编辑章节与讲义' : '新增章节与讲义'" width="min(720px, 92vw)" append-to-body :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="章节标题（必填）"><el-input v-model="form.title" maxlength="100" show-word-limit /></el-form-item>
        <div class="chapter-numbers">
          <el-form-item label="章节排序"><el-input-number v-model="form.sortOrder" :min="1" :precision="0" /></el-form-item>
          <el-form-item label="预计学习时长（分钟）"><el-input-number v-model="form.duration" :min="0" :precision="0" /></el-form-item>
        </div>
        <el-form-item label="文字讲义"><el-input v-model="form.content" type="textarea" :rows="12" maxlength="15000" show-word-limit placeholder="建议包含：学习目标、知识讲解、操作示例、练习与自测。" /></el-form-item>
      </el-form>
      <template #footer><el-button :disabled="saving" @click="editorVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="saveChapter">保存</el-button></template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listChapter, addChapter, updateChapter, deleteChapter } from '@/api/course'
import { useUserStore } from '@/store/user'

interface Chapter { id?: number; courseId: number; title: string; sortOrder: number; duration: number; content: string }
const props = defineProps<{ modelValue: boolean; course: { id: number; title: string; intro?: string } | null }>()
defineEmits<{ 'update:modelValue': [value: boolean] }>()
const user = useUserStore()
const canEdit = computed(() => user.roleKey !== 'student' && user.hasPermi('course:edit'))
const chapters = ref<Chapter[]>([])
const loading = ref(false)
const loadFailed = ref(false)
const activeChapter = ref<number | string>('')
const totalMinutes = computed(() => chapters.value.reduce((sum, chapter) => sum + (chapter.duration || 0), 0))
const editorVisible = ref(false)
const saving = ref(false)
const deletingId = ref<number>()
const form = reactive<Chapter>({ courseId: 0, title: '', sortOrder: 1, duration: 0, content: '' })
let requestId = 0

const loadChapters = async () => {
  if (!props.course) return
  const currentRequest = ++requestId
  loading.value = true
  loadFailed.value = false
  try {
    const res = await listChapter(props.course.id)
    if (currentRequest !== requestId) return
    chapters.value = res.data || []
    if (!chapters.value.some(chapter => chapter.id === activeChapter.value)) activeChapter.value = chapters.value[0]?.id || ''
  } catch {
    if (currentRequest === requestId) loadFailed.value = true
  } finally {
    if (currentRequest === requestId) loading.value = false
  }
}
watch(() => [props.modelValue, props.course?.id], () => {
  ++requestId
  editorVisible.value = false
  chapters.value = []
  activeChapter.value = ''
  if (props.modelValue) void loadChapters()
}, { immediate: true })

const editChapter = (chapter?: Chapter) => {
  if (!canEdit.value || !props.course) return
  Object.assign(form, { id: undefined, courseId: props.course.id, title: '', sortOrder: Math.max(0, ...chapters.value.map(item => item.sortOrder)) + 1, duration: 0, content: '' }, chapter)
  editorVisible.value = true
}
const saveChapter = async () => {
  if (!canEdit.value || saving.value) return
  if (!form.title.trim()) return void ElMessage.warning('请输入章节标题')
  if (!Number.isInteger(form.sortOrder) || form.sortOrder < 1 || !Number.isInteger(form.duration) || form.duration < 0) return void ElMessage.warning('请填写有效排序和时长')
  saving.value = true
  try {
    const payload = { ...form, title: form.title.trim(), content: form.content || '' }
    await (form.id ? updateChapter(payload) : addChapter(payload))
    editorVisible.value = false
    ElMessage.success('章节已保存')
    await loadChapters()
  } catch { /* 请求拦截器显示错误，保留输入 */ }
  finally { saving.value = false }
}
const removeChapter = async (chapter: Chapter) => {
  if (!canEdit.value || !chapter.id) return
  try { await ElMessageBox.confirm(`确定删除「${chapter.title}」及其讲义吗？`, '删除章节', { type: 'warning' }) }
  catch { return }
  deletingId.value = chapter.id
  try {
    await deleteChapter(chapter.id)
    ElMessage.success('章节已删除')
    await loadChapters()
  } catch { /* 请求拦截器显示错误 */ }
  finally { deletingId.value = undefined }
}
</script>

<style scoped>
.chapter-summary, .chapter-title, .chapter-actions, .chapter-numbers { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
.chapter-summary { justify-content: space-between; margin-bottom: 16px; }
.chapter-title { width: 100%; justify-content: space-between; padding-right: 16px; }
.chapter-title span, .course-intro, .chapter-actions { color: #777; }
.course-intro { margin-bottom: 20px; line-height: 1.7; }
.chapter-actions { margin: 8px 0 16px; }
.lecture { white-space: pre-wrap; overflow-wrap: anywhere; line-height: 1.9; padding: 20px; background: #f7f8fa; border-radius: 8px; font-size: 15px; }
</style>
