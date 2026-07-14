<template>
  <div class="state-page">
    <div class="state-icon">🔐</div>
    <h1 class="state-title">Core Identity</h1>
    <p class="state-detail">身份服务正在初始化。</p>

    <div class="card" style="margin-bottom: 24px; text-align: left;">
      <p><strong>当前版本：</strong>{{ meta?.version || '...' }}</p>
      <p><strong>服务状态：</strong>
        <span :class="`badge ${statusBadgeClass}`">{{ meta?.status || '检查中...' }}</span>
      </p>
      <p><strong>API 版本：</strong>{{ meta?.apiVersion || '...' }}</p>
      <div v-if="meta?.capabilities?.length" style="margin-top: 8px;">
        <strong>已启用的能力：</strong>
        <div style="display: flex; gap: 6px; flex-wrap: wrap; margin-top: 6px;">
          <span v-for="cap in meta.capabilities" :key="cap" class="badge badge-info">{{ cap }}</span>
        </div>
      </div>
    </div>

    <div v-if="error" class="card" style="margin-bottom: 24px; border-color: var(--error);">
      <p style="color: var(--error);">{{ error }}</p>
    </div>

    <div class="state-actions" style="margin-top: 8px;">
      <button class="btn btn-primary" disabled>登录功能将在 P1 提供</button>
      <button class="btn btn-accent" disabled>注册功能将在 P1 提供</button>
    </div>

    <small style="margin-top: 16px;">
      当前能力：SYSTEM_META · INTERNAL_SERVICE_AUTH · AUDIT_FOUNDATION · OUTBOX_FOUNDATION
    </small>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import apiClient from '@/api'

interface MetaResponse {
  service: string
  version: string
  apiVersion: string
  status: string
  instanceName: string
  edition: string
  capabilities: string[]
}

const meta = ref<MetaResponse | null>(null)
const error = ref<string | null>(null)

const statusBadgeClass = computed(() => {
  if (!meta.value) return 'badge-info'
  switch (meta.value.status) {
    case 'RUNNING': return 'badge-success'
    case 'INITIALIZING': return 'badge-warning'
    default: return 'badge-error'
  }
})

onMounted(async () => {
  try {
    const res = await apiClient.get<MetaResponse>('/meta')
    meta.value = res.data
  } catch (e: any) {
    if (e.type === 'NETWORK_ERROR') {
      window.location.href = '/system-unavailable'
    } else {
      error.value = '无法获取服务元信息：' + (e.message || '未知错误')
    }
  }
})
</script>