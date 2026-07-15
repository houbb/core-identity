<template>
  <div class="state-page">
    <div class="card" style="width: 100%; max-width: 420px;">
      <h1 style="margin-bottom: 8px;">忘记密码</h1>
      <p style="color: var(--text-secondary); margin-bottom: 24px;">输入邮箱地址，我们将发送密码重置链接</p>

      <form @submit.prevent="handleSubmit" style="display: flex; flex-direction: column; gap: 16px;">
        <input v-model="email" type="email" placeholder="you@example.com"
          style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
          background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />

        <button type="submit" class="btn btn-primary" :disabled="sent" style="width: 100%; padding: 10px;">
          {{ sent ? '已发送' : '发送重置链接' }}
        </button>
      </form>

      <p v-if="sent" style="text-align: center; margin-top: 12px; font-size: 12px; color: var(--success);">
        如果该邮箱关联有效账户，重置邮件已发送。
      </p>

      <p style="text-align: center; margin-top: 16px; font-size: 12px;">
        <router-link to="/login" style="color: var(--accent);">返回登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'

const auth = useAuthStore()
const email = ref('')
const sent = ref(false)

async function handleSubmit() {
  if (!email.value.trim()) return
  try {
    await auth.requestPasswordReset(email.value)
  } finally {
    sent.value = true
  }
}
</script>