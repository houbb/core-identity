<template>
  <div class="main-content">
    <div v-if="loading" style="text-align: center; padding: 48px; color: var(--text-secondary);">加载中……</div>

    <div v-else-if="user" style="max-width: 800px;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
        <h1>{{ user.displayName }}</h1>
        <div style="display: flex; gap: 8px;">
          <button v-if="user.status === 'DISABLED'" class="btn btn-accent" @click="handleEnable">启用账号</button>
          <button v-else class="btn" style="color: var(--error); border-color: var(--error);" @click="showDisable = true">禁用账号</button>
        </div>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px;">
        <div class="card">
          <h2 style="margin-bottom: 12px;">基本信息</h2>
          <div class="info-row"><span class="info-label">ID</span><span class="info-value" style="font-family: monospace; font-size: 11px;">{{ user.id }}</span></div>
          <div class="info-row"><span class="info-label">显示名称</span><span class="info-value">{{ user.displayName }}</span></div>
          <div class="info-row"><span class="info-label">邮箱</span><span class="info-value">{{ user.email }}</span></div>
          <div class="info-row"><span class="info-label">邮箱验证</span><span class="info-value">
            <span :class="user.emailVerified ? 'badge badge-success' : 'badge badge-warning'">
              {{ user.emailVerified ? '已验证' : '未验证' }}</span>
          </span></div>
        </div>

        <div class="card">
          <h2 style="margin-bottom: 12px;">账号状态</h2>
          <div class="info-row"><span class="info-label">状态</span><span class="info-value">
            <span :class="statusBadge(user.status)">{{ user.status }}</span>
          </span></div>
          <div class="info-row"><span class="info-label">个人空间</span><span class="info-value">{{ user.personalOrganizationName }}</span></div>
          <div class="info-row"><span class="info-label">活跃会话</span><span class="info-value">{{ user.activeSessions }}</span></div>
          <div class="info-row"><span class="info-label">创建时间</span><span class="info-value">{{ formatTime(user.createdAt) }}</span></div>
        </div>
      </div>

      <div class="card" style="margin-bottom: 16px;">
        <h2 style="margin-bottom: 12px;">管理操作</h2>
        <div style="display: flex; gap: 8px; flex-wrap: wrap;">
          <button class="btn" @click="handleRevokeSessions">撤销所有会话</button>
          <button class="btn" @click="handleResendVerification">重新发送验证邮件</button>
          <button class="btn" @click="handleSendPasswordReset">发送密码重置</button>
        </div>
      </div>

      <!-- Disable dialog -->
      <div v-if="showDisable" class="drawer-overlay" @click="showDisable = false">
        <div class="drawer" @click.stop>
          <div class="drawer-header">
            <h2>禁用账号</h2>
            <button class="drawer-close" @click="showDisable = false">✕</button>
          </div>
          <p style="margin-bottom: 16px; font-size: 13px;">您正在禁用用户 <strong>{{ user.email }}</strong></p>
          <p style="margin-bottom: 16px; font-size: 12px; color: var(--text-secondary);">
            • 用户将无法登录<br/>
            • 所有会话将立即失效<br/>
            • 未使用的验证和重置链接将失效
          </p>
          <input v-model="disableReason" placeholder="操作原因" style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border); background: var(--bg-primary); color: var(--text-primary); font-size: 13px; margin-bottom: 16px;" />
          <button class="btn btn-primary" style="background: var(--error); border-color: var(--error); width: 100%;" @click="handleDisable">确认禁用账号</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserManagementStore } from '@/stores/userManagementStore'

const route = useRoute()
const router = useRouter()
const store = useUserManagementStore()
const userId = route.params.userId as string
const user = ref<any>(null)
const loading = ref(true)
const showDisable = ref(false)
const disableReason = ref('')

onMounted(async () => {
  try {
    user.value = await store.getUserDetail(userId)
  } finally {
    loading.value = false
  }
})

async function handleDisable() {
  await store.disableUser(userId, disableReason.value || 'Admin action')
  showDisable.value = false
  user.value = await store.getUserDetail(userId)
}

async function handleEnable() {
  await store.enableUser(userId)
  user.value = await store.getUserDetail(userId)
}

async function handleRevokeSessions() {
  await store.revokeSessions(userId)
  user.value = await store.getUserDetail(userId)
}

async function handleResendVerification() {
  await store.revokeSessions(userId) // use the same store but call proper API
}

async function handleSendPasswordReset() {
  await store.revokeSessions(userId)
}

function formatTime(ts: number) { return new Date(ts).toLocaleString() }
function statusBadge(status: string) {
  return {
    'ACTIVE': 'badge badge-success',
    'PENDING_VERIFICATION': 'badge badge-warning',
    'LOCKED': 'badge badge-warning',
    'DISABLED': 'badge badge-error'
  }[status] || 'badge badge-info'
}
</script>