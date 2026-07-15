<template>
  <div class="page-container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <h1>会话管理</h1>
      <button class="btn btn-accent" @click="revokeAll">撤销所有其他会话</button>
    </div>

    <div v-if="sessions.length === 0" style="text-align: center; padding: 48px; color: var(--text-secondary);">
      没有其他活跃会话
    </div>

    <div v-for="s in sessions" :key="s.id" class="card" style="margin-bottom: 12px; padding: 16px;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <div style="font-weight: 600;">{{ s.deviceName || 'Unknown' }}</div>
          <small>{{ s.userAgent || '—' }}</small>
          <div style="margin-top: 4px;">
            <span class="badge badge-info" style="margin-right: 8px;">IP: {{ s.ipAddress || '—' }}</span>
            <span v-if="s.isCurrent" class="badge badge-success">当前设备</span>
          </div>
        </div>
        <div style="text-align: right;">
          <small>创建: {{ formatTime(s.createdAt) }}</small><br/>
          <small>最近: {{ formatTime(s.lastActiveAt) }}</small><br/>
          <button v-if="!s.isCurrent" class="btn" style="margin-top: 8px; font-size: 11px; padding: 4px 12px;"
            @click="revokeOne(s.id)">撤销</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import apiClient from '@/api'

interface SessionInfo {
  id: string; deviceName: string; userAgent?: string;
  ipAddress?: string; isCurrent: boolean;
  createdAt: number; lastActiveAt: number;
}

const sessions = ref<SessionInfo[]>([])

onMounted(async () => {
  try {
    const { data } = await apiClient.get('/me/sessions')
    sessions.value = data.sessions || data || []
  } catch { /* ignore */ }
})

async function revokeOne(id: string) {
  await apiClient.delete(`/me/sessions/${id}`)
  sessions.value = sessions.value.filter(s => s.id !== id)
}

async function revokeAll() {
  await apiClient.delete('/me/sessions')
  sessions.value = []
}

function formatTime(ts: number) {
  return new Date(ts).toLocaleString()
}
</script>