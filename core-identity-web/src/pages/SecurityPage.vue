<template>
  <div class="page-container">
    <h1 style="margin-bottom: 24px;">安全设置</h1>
    <div class="card" style="max-width: 480px;">
      <h2 style="margin-bottom: 16px;">修改密码</h2>
      <form @submit.prevent="handleChange" style="display: flex; flex-direction: column; gap: 16px;">
        <input v-model="form.currentPassword" type="password" placeholder="当前密码"
          style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
          background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
        <input v-model="form.newPassword" type="password" placeholder="新密码（至少 8 位）"
          style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
          background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
        <input v-model="form.confirmPassword" type="password" placeholder="确认新密码"
          style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
          background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />

        <div v-if="error" class="badge badge-error" style="padding: 8px 12px;">{{ error }}</div>
        <div v-if="success" style="font-size: 12px; color: var(--success);">{{ success }}</div>

        <button type="submit" class="btn btn-primary" :disabled="submitting" style="align-self: flex-start;">
          {{ submitting ? '处理中……' : '修改密码' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import apiClient from '@/api'

const form = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })
const error = ref('')
const success = ref('')
const submitting = ref(false)

async function handleChange() {
  error.value = ''
  success.value = ''
  if (form.newPassword.length < 8) { error.value = '密码至少 8 位'; return }
  if (form.newPassword !== form.confirmPassword) { error.value = '两次密码不一致'; return }

  submitting.value = true
  try {
    await apiClient.post('/me/password', {
      currentPassword: form.currentPassword,
      newPassword: form.newPassword
    })
    success.value = '密码修改成功！其他设备已退出登录。'
    form.currentPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
  } catch (e: any) {
    error.value = e.response?.data?.detail || '修改失败'
  } finally {
    submitting.value = false
  }
}
</script>