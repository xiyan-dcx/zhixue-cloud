import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import { canViewDashboard, getHomePath } from '@/utils/roleAccess'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: 'my-courses', name: 'MyCourses', component: () => import('@/views/my-courses/index.vue'),
        meta: { title: '我的课程', studentOnly: true }
      },
      {
        path: 'profile', name: 'Profile', component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '数据看板', icon: 'DataLine', requiresDashboardAccess: true }
      },
      {
        path: 'course',
        name: 'Course',
        component: () => import('@/views/course/index.vue'),
        meta: { title: '课程管理', icon: 'Reading' }
      },
      {
        path: 'student',
        name: 'Student',
        component: () => import('@/views/student/index.vue'),
        meta: { title: '学员管理', icon: 'User' }
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '订单管理', icon: 'List' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'Avatar' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  if (to.path === '/login') {
    return true
  }
  if (!userStore.token) {
    return '/login'
  }
  if (!userStore.roleKey) {
    try {
      await userStore.fetchInfo()
    } catch {
      userStore.reset()
      return '/login'
    }
  }
  if (to.path === '/') {
    return getHomePath(userStore.roleKey)
  }
  if (to.meta.requiresDashboardAccess && !canViewDashboard(userStore.roleKey)) {
    return getHomePath(userStore.roleKey)
  }
  if (to.meta.studentOnly && userStore.roleKey !== 'student') return getHomePath(userStore.roleKey)
  return true
})

export default router
