<template>
  <div class="state-page">
    <div class="card" style="width: 100%; max-width: 420px;">
      <h1 style="margin-bottom: 8px;">平台管理员登录</h1>
      <p style="color: var(--text-secondary); margin-bottom: 24px;">Core Identity Admin Console</p>

      <form @submit.prevent="handleLogin" style="display: flex; flex-direction: column; gap: 16px;">
        <input v-model="form.email" type="email" placeholder="管理员邮箱"
          style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
          background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
        <input v-model="form.password" type="password" placeholder="密码"
          style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
          background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />

        <div v-if="error" class="badge badge-error" style="padding: 8px 12px;">{{ error }}</div>

        <button type="submit" class="btn btn-primary" :disabled="submitting" style="width: 100%; padding: 10px;">
          {{ submitting ? '登录中……' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/adminAuthStore'

const router = useRouter()
const auth = useAdminAuthStore()
const form = reactive({ email: '', password: '' })
const submitting = ref(false)
const error = ref('')

async function handleLogin() {
  error.value = ''
  if (!form.email.trim()) { error.value = '请输入邮箱'; return }
  if (!form.password) { error.value = '请输入密码'; return }

  submitting.value = true
  try {
    await auth.login(form.email, form.password)
    router.push('/admin/system')
  } catch (e: any) {
    error.value = e.response?.data?.error || '登录失败'
  } finally {
    submitting.value = false
  }
}
</script>