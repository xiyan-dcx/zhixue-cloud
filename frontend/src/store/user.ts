import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getInfo, logout as logoutApi, type LoginForm } from '@/api/auth'
import { setToken, removeToken, getToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userId = ref<number>(0)
  const nickname = ref<string>('')
  const avatar = ref<string>('')
  const roleKey = ref<string>('')
  const permissions = ref<string[]>([])

  const login = async (form: LoginForm) => {
    const res = await loginApi(form)
    token.value = res.data.token
    setToken(res.data.token)
  }

  const fetchInfo = async () => {
    const res = await getInfo()
    userId.value = res.data.userId
    nickname.value = res.data.nickname
    avatar.value = res.data.avatar
    roleKey.value = res.data.roleKey
    permissions.value = res.data.permissions || []
  }

  const reset = () => {
    userId.value = 0
    nickname.value = ''
    avatar.value = ''
    token.value = ''
    roleKey.value = ''
    permissions.value = []
    removeToken()
  }

  const logout = async () => {
    try {
      await logoutApi()
    } catch {
      // 忽略登出接口异常
    }
    reset()
  }

  const hasPermi = (perm: string): boolean =>
    roleKey.value === 'admin' || permissions.value.includes(perm)

  return {
    token,
    userId,
    nickname,
    avatar,
    roleKey,
    permissions,
    login,
    fetchInfo,
    reset,
    logout,
    hasPermi
  }
})
