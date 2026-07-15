<template>
  <div class="page-container">
    <h1 style="margin-bottom: 24px;">编辑个人资料</h1>
    <div class="card" style="max-width: 480px;">
      <form @submit.prevent="handleSave" style="display: flex; flex-direction: column; gap: 16px;">
        <div>
          <label style="display: block; margin-bottom: 4px; font-size: 11px; color: var(--text-secondary);">显示名称</label>
          <input v-model="form.displayName" placeholder="您的名称"
            style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
            background: var(--bg-primary); color: var(--text-primary); font-size: 13px;" />
        </div>
        <div>
          <label style="display: block; margin-bottom: 4px; font-size: 11px; color: var(--text-secondary);">邮箱</label>
          <input :value="profile.email" disabled
            style="width: 100%; padding: 8px 12px; border-radius: 8px; border: 1px solid var(--border);
            background: var(--bg-secondary); color: var(--text-secondary); font-size: 13px;" />
        </div>
        <button type="submit" class="btn btn-primary" style="align-self: flex-start;">保存修改</button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import apiClient from '@/api'

const profile = reactive({ displayName: '', email: '' })
const form = reactive({ displayName: '' })

onMounted(async () => {
  try {
    const { data } = await apiClient.get('/me')
    profile.displayName = data.displayName
    profile.email = data.primaryEmail?.address || ''
    form.displayName = data.displayName
  } catch { /* ignore */ }
})

async function handleSave() {
  await apiClient.patch('/me', { displayName: form.displayName })
  profile.displayName = form.displayName
}
</script>