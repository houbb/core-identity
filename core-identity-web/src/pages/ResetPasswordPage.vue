<template>
  <div class="state-page">
    <div class="card" style="width: 100%; max-width: 420px;">
      <h1 style="margin-bottom: 8px;">重置密码</h1>
      <p style="color: var(--text-secondary); margin-bottom: 24px;">请输入新密码</p>

      <form @submit.prevent="handleReset" style="display: flex; flex-direction: column; gap: 16px;">
        <input v-model="email" type="email" placeholder="邮箱"
          style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
          background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
        <input v-model="newPassword" type="password" placeholder="新密码（至少 8 位）"
          style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
          background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
        <input v-model="confirmPassword" type="password" placeholder="确认新密码"
          style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
          background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />

        <div v-if="error" class="badge badge-error" style="padding: 8px 12px;">{{ error }}</div>
        <div v-if="success" style="font-size: 12px; color: var(--success);">{{ success }}</div>

        <button type="submit" class="btn btn-primary" :disabled="submitting" style="width: 100%; padding: 10px;">
          {{ submitting ? '处理中……' : '重置密码' }}
        </button>
      </form>

      <p style="text-align: center; margin-top: 16px; font-size: 12px;">
        <router-link to="/login" style="color: var(--accent);">返回登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const route = useRoute()
const auth = useAuthStore()
const token = ref((route.query.token as string) || '')
const email = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const error = ref('')
const success = ref('')
const submitting = ref(false)

async function handleReset() {
  error.value = ''
  success.value = ''

  if (!email.value.trim()) { error.value = '请输入邮箱'; return }
  if (newPassword.value.length < 8) { error.value = '密码至少 8 位'; return }
  if (newPassword.value !== confirmPassword.value) { error.value = '两次密码不一致'; return }

  submitting.value = true
  try {
    await auth.completePasswordReset(token.value, email.value, newPassword.value)
    success.value = '密码重置成功！请使用新密码登录。'
  } catch (e: any) {
    error.value = e.response?.data?.detail || '重置失败'
  } finally {
    submitting.value = false
  }
}
</script>