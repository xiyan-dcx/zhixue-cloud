<template>
  <el-container class="layout">
    <el-aside :width="collapse ? '64px' : '220px'" class="aside">
      <div class="logo-bar">
        <span class="logo-icon">智</span>
        <span v-show="!collapse" class="logo-text">智学云</span>
      </div>
      <el-menu
        :router="true"
        :default-active="route.path"
        :collapse="collapse"
        class="side-menu"
      >
        <template v-for="m in menus" :key="m.id">
          <el-sub-menu v-if="m.children && m.children.length" :index="'sub' + m.id">
            <template #title>
              <el-icon><component :is="m.icon || 'Menu'" /></el-icon>
              <span>{{ m.menuName }}</span>
            </template>
            <el-menu-item v-for="c in m.children" :key="c.id" :index="c.path">
              <el-icon><component :is="c.icon || 'Menu'" /></el-icon>
              <span>{{ c.menuName }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="m.path">
            <el-icon><component :is="m.icon || 'Menu'" /></el-icon>
            <span>{{ m.menuName }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapse = !collapse">
            <Fold v-if="!collapse" />
            <Expand v-else />
          </el-icon>
          <span class="page-title">{{ route.meta.title }}</span>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="30" :src="userStore.avatar" class="avatar">{{ nicknameFirst }}</el-avatar>
              <span class="nickname">{{ userStore.nickname }}</span>
              <el-tag size="small" effect="plain" class="role-tag">{{ roleText }}</el-tag>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { getRouters } from '@/api/auth'
import { useUserStore } from '@/store/user'
import { canViewDashboard } from '@/utils/roleAccess'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapse = ref(false)
const rawMenus = ref<any[]>([])
const filterMenus = (items: any[], parentPath = ''): any[] =>
  items.flatMap((item) => {
    const path = item.path?.startsWith('/')
      ? item.path
      : `${parentPath}/${item.path || ''}`
    const normalizedPath = path.replace(/\/+$/, '')
    if (normalizedPath === '/dashboard' && !canViewDashboard(userStore.roleKey)) return []
    const children = item.children ? filterMenus(item.children, normalizedPath) : undefined
    if (item.children?.length && !children?.length) return []
    return [{ ...item, children }]
  })
const menus = computed(() => {
  const items = filterMenus(rawMenus.value).filter(item => !['/my-courses', '/profile'].includes(item.path))
  if (userStore.roleKey === 'student') items.unshift({ id: 'my-courses', path: '/my-courses', menuName: '我的课程', icon: 'Collection' })
  items.push({ id: 'profile', path: '/profile', menuName: '个人中心', icon: 'UserFilled' })
  return items
})

const nicknameFirst = computed(() => userStore.nickname?.charAt(0) || 'U')
const roleText = computed(() => {
  const map: Record<string, string> = { admin: '管理员', teacher: '讲师', student: '学员' }
  return map[userStore.roleKey] || userStore.roleKey
})

onMounted(async () => {
  const res = await getRouters()
  rawMenus.value = res.data || []
})

const handleCommand = async (command: string) => {
  if (command === 'profile') { await router.push('/profile'); return }
  if (command === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    await userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  background: #fff;
  border-right: 1px solid #eef0f3;
  transition: width 0.25s;
  overflow: hidden;
}
.logo-bar {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-left: 18px;
  border-bottom: 1px solid #eef0f3;
}
.logo-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1f2329;
  white-space: nowrap;
}
.side-menu {
  border-right: none;
}
.header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #eef0f3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #5e6470;
}
.page-title {
  font-size: 16px;
  font-weight: 600;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}
.avatar {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}
.nickname {
  font-size: 14px;
}
.role-tag {
  margin-left: 2px;
}
.main {
  background: #f5f7fa;
  padding: 16px;
}
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
