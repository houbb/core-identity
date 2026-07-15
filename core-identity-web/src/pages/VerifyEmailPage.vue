<template>
  <div class="state-page">
    <div v-if="loading" class="card" style="width: 100%; max-width: 420px; text-align: center;">
      <p>正在验证邮箱……</p>
    </div>
    <div v-else-if="success" class="card" style="width: 100%; max-width: 420px; text-align: center;">
      <div class="state-icon">✅</div>
      <h1 class="state-title">邮箱验证成功</h1>
      <p class="state-detail">{{ message }}</p>
      <router-link to="/login" class="btn btn-primary">前往登录</router-link>
    </div>
    <div v-else class="card" style="width: 100%; max-width: 420px; text-align: center;">
      <div class="state-icon">❌</div>
      <h1 class="state-title">验证失败</h1>
      <p class="state-detail">{{ message }}</p>
      <div class="state-actions">
        <router-link to="/login" class="btn btn-accent">前往登录</router-link>
        <router-link to="/register" class="btn">重新注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const route = useRoute()
const auth = useAuthStore()
const loading = ref(true)
const success = ref(false)
const message = ref('')

onMounted(async () => {
  const token = route.query.token as string
  if (!token) {
    success.value = false
    message.value = '缺少验证令牌'
    loading.value = false
    return
  }
  try {
    const result = await auth.verifyEmail(token)
    success.value = result.success
    message.value = result.message
  } catch (e: any) {
    success.value = false
    message.value = e.response?.data?.detail || '验证令牌无效或已过期'
  } finally {
    loading.value = false
  }
})
</script>