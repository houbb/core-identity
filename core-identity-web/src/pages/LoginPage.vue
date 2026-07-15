<template>
  <div class="state-page">
    <div class="card" style="width: 100%; max-width: 420px;">
      <h1 style="margin-bottom: 8px;">登录</h1>
      <p style="color: var(--text-secondary); margin-bottom: 24px;">登录您的 Core Identity 账户</p>

      <form @submit.prevent="handleLogin" style="display: flex; flex-direction: column; gap: 16px;">
        <div>
          <label style="display: block; margin-bottom: 4px; font-size: 11px; color: var(--text-secondary);">邮箱地址</label>
          <input v-model="form.email" type="email" placeholder="you@example.com"
            style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
            background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
        </div>

        <div>
          <label style="display: block; margin-bottom: 4px; font-size: 11px; color: var(--text-secondary);">密码</label>
          <input v-model="form.password" type="password" placeholder="输入密码"
            style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
            background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
        </div>

        <div v-if="error" class="badge badge-error" style="padding: 8px 12px;">{{ error }}</div>

        <button type="submit" class="btn btn-primary" :disabled="submitting" style="width: 100%; padding: 10px;">
          {{ submitting ? '登录中……' : '登录' }}
        </button>
      </form>

      <p style="text-align: center; margin-top: 16px; font-size: 12px; color: var(--text-secondary);">
        <router-link to="/forgot-password" style="color: var(--accent);">忘记密码？</router-link>
        &nbsp;·&nbsp;
        <router-link to="/register" style="color: var(--accent);">创建账户</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const auth = useAuthStore()

const form = reactive({ email: '', password: '' })
const submitting = ref(false)
const error = ref('')

async function handleLogin() {
  error.value = ''
  if (!form.email.trim()) { error.value = '请输入邮箱地址'; return }
  if (!form.password) { error.value = '请输入密码'; return }

  submitting.value = true
  try {
    await auth.login(form.email, form.password)
    router.push('/account')
  } catch (e: any) {
    error.value = e.response?.data?.detail || e.message || '登录失败'
    form.password = ''
  } finally {
    submitting.value = false
  }
}
</script>