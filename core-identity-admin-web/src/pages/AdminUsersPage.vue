<template>
  <div class="main-content">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <h1>用户管理</h1>
      <button class="btn btn-primary" @click="showCreate = true">创建用户</button>
    </div>

    <!-- Quick stats -->
    <div class="grid-3" style="margin-bottom: 24px;">
      <div class="card status-card">
        <div class="status-card-header"><span class="status-card-title">总用户</span></div>
        <div class="status-card-value">{{ users.length }}</div>
      </div>
      <div class="card status-card">
        <div class="status-card-header"><span class="status-card-title">活跃</span></div>
        <div class="status-card-value">{{ users.filter((u: any) => u.status === 'ACTIVE').length }}</div>
      </div>
      <div class="card status-card">
        <div class="status-card-header"><span class="status-card-title">禁用</span></div>
        <div class="status-card-value">{{ users.filter((u: any) => u.status === 'DISABLED').length }}</div>
      </div>
    </div>

    <!-- Search -->
    <div style="margin-bottom: 16px; display: flex; gap: 8px;">
      <input v-model="searchEmail" placeholder="搜索邮箱……"
        style="padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
        background: var(--bg-secondary); color: var(--text-primary); font-size: 13px; width: 240px;" />
      <button class="btn" @click="searchEmail = ''">清除</button>
    </div>

    <!-- User list -->
    <div v-if="users.length === 0" style="text-align: center; padding: 48px; color: var(--text-secondary);">
      暂无用户数据
    </div>

    <div v-for="user in filteredUsers" :key="user.id" class="card" style="margin-bottom: 8px; padding: 16px;
      display: flex; justify-content: space-between; align-items: center; cursor: pointer;"
      @click="router.push(`/admin/users/${user.id}`)">
      <div>
        <div style="font-weight: 600;">{{ user.displayName }}</div>
        <small>{{ user.email }}</small>
      </div>
      <div style="display: flex; gap: 8px; align-items: center;">
        <span class="badge" :class="statusBadge(user.status)">{{ user.status }}</span>
        <span style="color: var(--text-secondary);">→</span>
      </div>
    </div>

    <!-- Create user modal -->
    <div v-if="showCreate" class="drawer-overlay" @click="showCreate = false">
      <div class="drawer" @click.stop>
        <div class="drawer-header">
          <h2>创建用户</h2>
          <button class="drawer-close" @click="showCreate = false">✕</button>
        </div>
        <form @submit.prevent="handleCreate" style="display: flex; flex-direction: column; gap: 16px;">
          <input v-model="newUser.email" type="email" placeholder="邮箱"
            style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
            background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
          <input v-model="newUser.displayName" placeholder="显示名称"
            style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
            background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
          <button type="submit" class="btn btn-primary">创建</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserManagementStore } from '@/stores/userManagementStore'

const router = useRouter()
const store = useUserManagementStore()
const users = ref<any[]>([])
const searchEmail = ref('')
const showCreate = ref(false)
const newUser = ref({ email: '', displayName: '' })

const filteredUsers = computed(() => {
  if (!searchEmail.value) return users.value
  return users.value.filter((u: any) =>
    (u.email || '').toLowerCase().includes(searchEmail.value.toLowerCase()))
})

onMounted(async () => {
  try { users.value = (await store.fetchUsers()).users || []; } catch { /* */ }
})

async function handleCreate() {
  try {
    await store.createUser(newUser.value.email, newUser.value.displayName)
    showCreate.value = false
    newUser.value = { email: '', displayName: '' }
    users.value = (await store.fetchUsers()).users || []
  } catch { /* */ }
}

function statusBadge(status: string) {
  return {
    'ACTIVE': 'badge-success',
    'PENDING_VERIFICATION': 'badge-warning',
    'LOCKED': 'badge-warning',
    'DISABLED': 'badge-error'
  }[status] || 'badge-info'
}
</script>