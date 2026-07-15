<template>
  <div class="page-container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <div>
        <h1>我的账户</h1>
        <small>欢迎回来{{ user?.displayName ? '，' + user.displayName : '' }}</small>
      </div>
      <div style="display: flex; gap: 8px;">
        <router-link to="/account/profile" class="btn">编辑资料</router-link>
        <router-link to="/account/security" class="btn">安全设置</router-link>
        <router-link to="/account/sessions" class="btn">会话管理</router-link>
        <button class="btn btn-accent" @click="handleLogout">退出登录</button>
      </div>
    </div>

    <div class="grid-3" style="margin-bottom: 24px;">
      <div class="card status-card">
        <div class="status-card-header">
          <span class="status-card-title">账户状态</span>
          <span class="badge badge-success">正常</span>
        </div>
        <div class="status-card-value">{{ user?.email || '—' }}</div>
      </div>
      <div class="card status-card">
        <div class="status-card-header">
          <span class="status-card-title">个人空间</span>
          <span class="badge badge-info">PERSONAL</span>
        </div>
        <div class="status-card-value">我的工作空间</div>
      </div>
      <div class="card status-card">
        <div class="status-card-header">
          <span class="status-card-title">最近登录</span>
        </div>
        <div class="status-card-value" style="font-size: 12px; color: var(--text-secondary);">—</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'

const auth = useAuthStore()
const user = auth.user
const router = useRouter()

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}

onMounted(async () => {
  if (!auth.isAuthenticated) {
    try { await auth.checkSession() } catch { router.push('/login') }
  }
})
</script>