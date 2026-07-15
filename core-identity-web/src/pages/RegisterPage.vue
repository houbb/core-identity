<template>
  <div class="state-page">
    <div class="card" style="width: 100%; max-width: 420px;">
      <h1 style="margin-bottom: 8px;">创建账户</h1>
      <p style="color: var(--text-secondary); margin-bottom: 24px;">注册 Core Identity 账户</p>

      <form @submit.prevent="handleRegister" style="display: flex; flex-direction: column; gap: 16px;">
        <div>
          <label style="display: block; margin-bottom: 4px; font-size: 11px; color: var(--text-secondary);">显示名称</label>
          <input v-model="form.displayName" type="text" placeholder="您的名称"
            style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
            background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
        </div>

        <div>
          <label style="display: block; margin-bottom: 4px; font-size: 11px; color: var(--text-secondary);">邮箱地址</label>
          <input v-model="form.email" type="email" placeholder="you@example.com"
            style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
            background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
        </div>

        <div>
          <label style="display: block; margin-bottom: 4px; font-size: 11px; color: var(--text-secondary);">密码</label>
          <div style="position: relative;">
            <input v-model="form.password" :type="showPassword ? 'text' : 'password'" placeholder="至少 8 位字符"
              style="width: 100%; padding: 8px 40px 8px 12px; border-radius: 8px; border: 1px solid var(--border);
              background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
            <button type="button" @click="showPassword = !showPassword"
              style="position: absolute; right: 8px; top: 50%; transform: translateY(-50%);
              background: none; border: none; cursor: pointer; color: var(--text-secondary); font-size: 13px;">
              {{ showPassword ? '隐藏' : '显示' }}
            </button>
          </div>
          <small style="display: block; margin-top: 4px;">密码长度最少 8 位</small>
          <small v-if="capsLock" style="color: var(--warning);">⚠ 大写锁定已开启</small>
        </div>

        <div>
          <label style="display: block; margin-bottom: 4px; font-size: 11px; color: var(--text-secondary);">确认密码</label>
          <input v-model="form.confirmPassword" type="password" placeholder="再次输入密码"
            style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
            background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
        </div>

        <label style="display: flex; align-items: center; gap: 8px; font-size: 12px;">
          <input type="checkbox" v-model="form.agreeTerms" />
          我同意 <a href="#" style="color: var(--accent);">服务条款</a> 和 <a href="#" style="color: var(--accent);">隐私政策</a>
        </label>

        <div v-if="error" class="badge badge-error" style="padding: 8px 12px;">{{ error }}</div>

        <button type="submit" class="btn btn-primary" :disabled="submitting" style="width: 100%; padding: 10px;">
          {{ submitting ? '正在创建账户……' : '创建账户' }}
        </button>
      </form>

      <p style="text-align: center; margin-top: 16px; font-size: 12px; color: var(--text-secondary);">
        已有账户？<router-link to="/login" style="color: var(--accent);">登录</router-link>
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

const form = reactive({
  displayName: '',
  email: '',
  password: '',
  confirmPassword: '',
  agreeTerms: false
})

const showPassword = ref(false)
const capsLock = ref(false)
const submitting = ref(false)
const error = ref('')

function onCapsLock(e: KeyboardEvent) {
  capsLock.value = e.getModifierState('CapsLock')
}

async function handleRegister() {
  error.value = ''

  if (!form.displayName.trim()) { error.value = '请输入显示名称'; return }
  if (!form.email.trim()) { error.value = '请输入邮箱地址'; return }
  if (form.password.length < 8) { error.value = '密码需要至少 8 位'; return }
  if (form.password !== form.confirmPassword) { error.value = '两次密码输入不一致'; return }
  if (!form.agreeTerms) { error.value = '请同意服务条款'; return }

  submitting.value = true
  try {
    await auth.register(form.email, form.password, form.displayName)
    router.push({ name: 'check-email', query: { email: form.email } })
  } catch (e: any) {
    error.value = e.response?.data?.detail || e.message || '注册失败，请重试'
    form.password = ''
    form.confirmPassword = ''
  } finally {
    submitting.value = false
  }
}
</script>