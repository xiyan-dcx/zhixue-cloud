<template>
  <div class="login-page">
    <div class="login-box">
      <div class="brand-side">
        <div class="brand-inner">
          <div class="logo">智学云</div>
          <p class="slogan">ZhiXue Cloud · 在线课程管理平台</p>
          <p class="desc">课程 · 学员 · 订单 · 数据看板<br />一站式教学运营后台</p>
        </div>
      </div>
      <div class="form-side">
        <h2 class="form-title">欢迎登录</h2>
        <p class="form-sub">Welcome back, please sign in</p>
        <el-form :model="form" class="login-form" @keyup.enter="handleLogin">
          <el-form-item>
            <el-input
              v-model="form.username"
              size="large"
              placeholder="请输入账号"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="form.password"
              type="password"
              size="large"
              placeholder="请输入密码"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login(form)
    await userStore.fetchInfo()
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    // 错误提示已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #ec4899 100%);
}
.login-box {
  width: 860px;
  max-width: 92vw;
  height: 480px;
  display: flex;
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}
.brand-side {
  flex: 1;
  background: linear-gradient(160deg, #4f46e5, #7c3aed);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.brand-side::after {
  content: '';
  position: absolute;
  width: 220px;
  height: 220px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
  top: -60px;
  right: -60px;
}
.brand-inner {
  text-align: center;
  padding: 0 30px;
  z-index: 1;
}
.logo {
  font-size: 46px;
  font-weight: 800;
  letter-spacing: 4px;
}
.slogan {
  margin-top: 12px;
  font-size: 15px;
  opacity: 0.9;
}
.desc {
  margin-top: 40px;
  line-height: 2;
  font-size: 14px;
  opacity: 0.85;
}
.form-side {
  width: 380px;
  padding: 54px 44px;
}
.form-title {
  font-size: 26px;
  color: #1f2329;
}
.form-sub {
  color: #909399;
  font-size: 13px;
  margin: 6px 0 32px;
}
.login-form :deep(.el-input__wrapper) {
  padding: 4px 14px;
}
.login-btn {
  width: 100%;
  margin-top: 8px;
  letter-spacing: 6px;
  font-size: 16px;
}
</style>
