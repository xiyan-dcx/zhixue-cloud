<template>
  <div class="profile-page" v-loading="loading">
    <div class="page-heading"><div><span class="eyebrow">ACCOUNT SETTINGS</span><h1>个人中心</h1><p>管理个人资料，让每一次学习都从这里开始。</p></div><span class="heading-label"><el-icon><User /></el-icon> 我的账户</span></div>
    <el-alert v-if="failed" type="error" title="个人资料加载失败" :closable="false"><el-button link @click="load">重试</el-button></el-alert>
    <template v-else>
      <section class="identity-banner">
        <div class="identity-main"><el-avatar :size="88" class="profile-avatar" :src="user.avatar || profile.avatar">{{ user.nickname?.charAt(0) || profile.nickname?.charAt(0) || '我' }}</el-avatar>
          <div class="identity-text"><div class="identity-name"><h2>{{ user.nickname || profile.nickname || '欢迎你' }}</h2><span class="role-badge">{{ roleLabel }}</span></div><p>@{{ profile.username || '—' }}</p><span class="identity-note">保持热爱，持续成长。</span></div>
        </div>
        <div class="avatar-controls"><el-button :loading="uploading" :disabled="loading" class="avatar-button" :icon="Camera" @click="fileInput?.click()">更换头像</el-button><p>PNG / JPEG · 最大 2MB<br>建议使用清晰的方形图片，尺寸不超过 2048 × 2048</p></div>
          <input ref="fileInput" type="file" accept="image/png,image/jpeg" hidden @change="onAvatar" />
      </section>
      <div class="settings-grid">
      <el-card shadow="never" class="settings-card"><template #header><div class="card-heading"><span class="section-icon"><el-icon><User /></el-icon></span><div><h3>个人资料</h3><p>完善你的信息，展示更好的自己</p></div></div></template>
        <el-form label-position="top" class="settings-form" @submit.prevent="save">
          <div class="account-facts"><div><span>登录账号</span><strong>{{ profile.username || '—' }}</strong></div><div><span>当前身份</span><strong>{{ roleLabel || '—' }}</strong></div></div>
          <el-form-item label="昵称"><el-input v-model="profile.nickname" maxlength="64" show-word-limit placeholder="请输入昵称" /></el-form-item>
          <p class="field-hint">昵称会展示在页面右上角及个人主页中。</p>
          <div class="profile-note"><el-icon><InfoFilled /></el-icon><span>登录账号与身份由管理员维护。如需调整，请联系管理员。</span></div>
          <div class="form-footer"><span>修改后记得保存</span><el-button type="primary" :icon="Check" :loading="saving" :disabled="loading" @click="save">保存资料</el-button></div>
        </el-form>
      </el-card>
      <el-card shadow="never" class="settings-card"><template #header><div class="card-heading"><span class="section-icon security-icon"><el-icon><Lock /></el-icon></span><div><h3>账户安全</h3><p>设置专属密码，保护你的账户</p></div></div></template>
        <el-form label-position="top" class="settings-form" @submit.prevent="savePassword">
          <el-form-item label="原密码"><el-input v-model="password.oldPassword" type="password" show-password :prefix-icon="Lock" placeholder="请输入当前密码" autocomplete="current-password" /></el-form-item>
          <el-form-item label="新密码"><el-input v-model="password.newPassword" type="password" show-password :prefix-icon="Key" autocomplete="new-password" placeholder="设置 8–64 个字符的新密码" maxlength="64" /></el-form-item>
          <el-form-item label="确认新密码"><el-input v-model="password.confirm" type="password" show-password :prefix-icon="Key" placeholder="再次输入新密码" autocomplete="new-password" maxlength="64" /></el-form-item>
          <p class="field-hint">建议组合字母、数字与符号；UTF-8 编码最多 72 字节。</p>
          <div class="security-note"><el-icon><InfoFilled /></el-icon><span>密码修改成功后，所有设备都需重新登录。</span></div>
          <div class="form-footer"><span>请妥善保管你的密码</span><el-button type="primary" :loading="changing" :disabled="loading" @click="savePassword">更新密码</el-button></div>
        </el-form>
      </el-card>
      </div>
    </template>
  </div>
</template>
<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Camera, Lock, Key, Check, InfoFilled } from '@element-plus/icons-vue'
import { getProfile, saveProfile, changePassword, uploadAvatar } from '@/api/personal'
import { useUserStore } from '@/store/user'
const user = useUserStore(), router = useRouter()
const loading = ref(false), failed = ref(false), saving = ref(false), changing = ref(false), uploading = ref(false)
const fileInput = ref<HTMLInputElement>()
const profile = reactive({ username: '', nickname: '', avatar: '', roleKey: '' })
const password = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const roleLabel = computed(() => ({ admin: '管理员', teacher: '讲师', student: '学员' }[profile.roleKey] || profile.roleKey))
const load = async () => { loading.value = true; failed.value = false; try { Object.assign(profile, (await getProfile()).data) } catch { failed.value = true } finally { loading.value = false } }
const save = async () => {
  if (saving.value) return
  if (!profile.nickname.trim()) return void ElMessage.warning('请输入昵称')
  saving.value = true
  try { await saveProfile(profile.nickname.trim()); await user.fetchInfo(); ElMessage.success('资料已保存') } catch { /* 统一提示 */ } finally { saving.value = false }
}
const onAvatar = async (event: Event) => {
  const input = event.target as HTMLInputElement, file = input.files?.[0]
  if (!file) return
  input.value = ''
  if (!['image/png', 'image/jpeg'].includes(file.type) || file.size > 2*1024*1024) return void ElMessage.warning('请选择2MB以内的PNG或JPEG图片')
  uploading.value = true
  try { profile.avatar = (await uploadAvatar(file)).data.url; await user.fetchInfo(); ElMessage.success('头像已更新') } catch { /* 统一提示 */ } finally { uploading.value = false }
}
const savePassword = async () => {
  if (changing.value) return
  if (!password.oldPassword) return void ElMessage.warning('请输入原密码')
  if (password.newPassword.length < 8 || password.newPassword.length > 64 || new TextEncoder().encode(password.newPassword).length > 72) return void ElMessage.warning('新密码需8–64个字符，UTF-8编码不超过72字节')
  if (password.newPassword !== password.confirm) return void ElMessage.warning('两次新密码输入不一致')
  changing.value = true
  try { await changePassword({ oldPassword: password.oldPassword, newPassword: password.newPassword }); Object.assign(password, { oldPassword: '', newPassword: '', confirm: '' }); user.reset(); ElMessage.success('密码已修改，请重新登录'); await router.replace('/login') }
  catch { /* 统一提示 */ } finally { changing.value = false }
}
void load()
</script>
<style scoped>
.profile-page { --accent: #6366f1; --ink: #27314b; max-width: 1440px; margin: 0 auto; padding: 14px 20px 40px; color: var(--ink); }
.page-heading { display: flex; justify-content: space-between; align-items: center; margin-bottom: 28px; gap: 16px; }
.eyebrow { font-size: 11px; font-weight: 700; letter-spacing: 2px; color: #8588a5; }
.page-heading h1 { font-size: 28px; font-weight: 700; letter-spacing: -1px; margin: 6px 0 8px; }
.page-heading p { color: #8891a4; font-size: 14px; }
.heading-label { display: flex; align-items: center; gap: 8px; color: #737b94; font-size: 13px; }
.identity-banner { display: flex; align-items: center; justify-content: space-between; gap: 24px; padding: 32px 36px; margin-bottom: 26px; border: 1px solid #e4e6f8; border-radius: 18px; background: linear-gradient(115deg, #eeefff, #f7f5ff 58%, #f1f6ff); }
.identity-main { display: flex; align-items: center; gap: 24px; min-width: 0; }
.profile-avatar { flex-shrink: 0; font-size: 32px; font-weight: 600; color: #fff; background: linear-gradient(145deg, #818cf8, #6366f1); border: 4px solid #fff; box-shadow: 0 6px 20px #6366f126; }
.identity-text { min-width: 0; }
.identity-name { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.identity-name h2 { font-size: 24px; overflow-wrap: anywhere; }
.role-badge { background: #ffffffbd; border: 1px solid #dedffd; border-radius: 6px; color: #6963cc; padding: 3px 10px; font-size: 12px; }
.identity-text p { margin: 6px 0 10px; color: #858ba7; font-size: 13px; overflow-wrap: anywhere; }
.identity-note { color: #727b99; font-size: 13px; }
.avatar-controls { text-align: right; flex-shrink: 0; }
.avatar-controls p { color: #8a90a6; font-size: 11px; line-height: 1.8; margin-top: 10px; }
.avatar-button { border-radius: 8px; border-color: #d9dcf4; color: #6866bf; background: #ffffffc9; }
.settings-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 26px; align-items: stretch; }
.settings-card { border: 1px solid #e9ecf3; border-radius: 16px; box-shadow: 0 4px 24px #28345404; min-width: 0; display: flex; flex-direction: column; }
.settings-card :deep(.el-card__header) { padding: 24px 28px; border-bottom-color: #f0f2f7; }
.settings-card :deep(.el-card__body) { padding: 26px 28px 24px; flex: 1; display: flex; flex-direction: column; }
.card-heading { display: flex; align-items: center; gap: 14px; }
.section-icon { display: grid; place-items: center; width: 42px; height: 42px; border-radius: 12px; color: #7773df; background: #f0efff; font-size: 21px; }
.security-icon { color: #4c9b9a; background: #edf8f7; }
.card-heading h3 { font-size: 17px; font-weight: 650; }
.card-heading p { color: #969daf; font-size: 12px; margin-top: 5px; }
.settings-form { display: flex; flex-direction: column; flex: 1; }
.account-facts { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; padding: 18px 20px; border: 1px solid #edf0f6; background: #f8f9fc; border-radius: 10px; margin-bottom: 26px; }
.account-facts span { display: block; font-size: 12px; color: #949caf; margin-bottom: 8px; }
.account-facts strong { font-size: 14px; font-weight: 550; overflow-wrap: anywhere; }
.settings-form :deep(.el-form-item) { margin-bottom: 20px; }
.settings-form :deep(.el-form-item__label) { color: #5b647e; font-size: 13px; padding-bottom: 8px; }
.settings-form :deep(.el-input__wrapper) { min-height: 44px; padding: 0 14px; border-radius: 8px; box-shadow: 0 0 0 1px #e2e6ef inset; }
.settings-form :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px var(--accent) inset, 0 0 0 3px #6366f110; }
.settings-form :deep(.el-input__inner) { font-size: 13px; color: #454e68; }
.settings-form :deep(.el-input__prefix) { color: #a2a8bb; }
.field-hint { color: #939caf; font-size: 12px; line-height: 1.8; margin: -8px 0 20px; }
.profile-note, .security-note { display: flex; gap: 9px; align-items: flex-start; padding: 14px 16px; font-size: 12px; line-height: 1.8; border-radius: 8px; color: #7b849e; background: #f8f9fc; margin-bottom: 24px; }
.profile-note .el-icon, .security-note .el-icon { margin-top: 4px; flex-shrink: 0; }
.security-note { color: #8f855f; background: #fcfaf2; }
.form-footer { display: flex; justify-content: space-between; align-items: center; gap: 12px; padding-top: 22px; border-top: 1px solid #eff1f6; margin-top: auto; }
.form-footer > span { font-size: 12px; color: #9ba2b3; }
.form-footer :deep(.el-button--primary) { --el-button-bg-color: #6366f1; --el-button-border-color: #6366f1; --el-button-hover-bg-color: #777af5; --el-button-hover-border-color: #777af5; --el-button-active-bg-color: #5255d8; --el-button-active-border-color: #5255d8; border-radius: 8px; padding: 12px 22px; height: 40px; box-shadow: 0 4px 10px #6366f11c; }
@media (max-width: 900px) { .settings-grid { grid-template-columns: 1fr; } .avatar-controls p { max-width: 210px; } .identity-banner { padding: 24px; } }
@media (max-width: 600px) { .profile-page { padding: 8px 0 24px; } .heading-label { display: none; } .page-heading h1 { font-size: 24px; } .identity-banner { flex-direction: column; align-items: flex-start; gap: 20px; } .identity-main { gap: 16px; } .identity-name h2 { font-size: 20px; } .avatar-controls { text-align: left; } .avatar-controls p { max-width: none; } .settings-card :deep(.el-card__header), .settings-card :deep(.el-card__body) { padding: 20px; } .form-footer > span { display: none; } .form-footer { justify-content: flex-end; } }
</style>
