<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <h1>服务健康</h1>
      <button class="btn btn-primary" @click="refresh">重新检查</button>
    </div>

    <div class="grid-2">
      <!-- Admin Backend Health -->
      <div class="card">
        <h2>Admin Backend</h2>
        <div class="info-row"><span class="info-label">Status</span><span class="info-value badge badge-success">HEALTHY</span></div>
        <div class="info-row"><span class="info-label">Service</span><span class="info-value">core-identity-admin-backend</span></div>
      </div>

      <!-- Identity Backend Health -->
      <div class="card">
        <h2>Identity Backend</h2>
        <div class="info-row"><span class="info-label">Status</span><span :class="`info-value badge ${identityHealthBadge}`">{{ identityHealth?.status || '...' }}</span></div>
        <div class="info-row"><span class="info-label">Service</span><span class="info-value">{{ identityHealth?.service || '...' }}</span></div>
        <div class="info-row"><span class="info-label">Timestamp</span><span class="info-value">{{ identityHealth?.timestamp ? new Date(identityHealth.timestamp).toLocaleString() : '...' }}</span></div>
      </div>

      <!-- Aggregated Status -->
      <div class="card">
        <h2>聚合状态</h2>
        <div class="info-row"><span class="info-label">Overall</span><span :class="`info-value badge ${aggregatedBadge}`">{{ aggregated }}</span></div>
      </div>
    </div>

    <div v-if="error" class="card" style="margin-top: 16px; border-color: var(--error);">
      <p style="color: var(--error);">{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import apiClient from '@/api'

const identityHealth = ref<any>(null)
const aggregated = ref('CHECKING...')
const error = ref<string | null>(null)

const identityHealthBadge = computed(() => {
  const s = identityHealth.value?.status
  if (s === 'HEALTHY') return 'badge-success'
  if (s === 'UNAVAILABLE') return 'badge-error'
  return 'badge-warning'
})

const aggregatedBadge = computed(() => {
  if (aggregated.value === 'HEALTHY') return 'badge-success'
  if (aggregated.value?.includes('DEGRADED')) return 'badge-warning'
  return 'badge-error'
})

async function refresh() {
  error.value = null
  try {
    const res = await apiClient.get('/system/health')
    identityHealth.value = res.data?.identityBackend
    aggregated.value = res.data?.aggregatedStatus || 'UNKNOWN'
  } catch (e: any) {
    error.value = '获取健康状态失败'
    aggregated.value = 'UNAVAILABLE'
  }
}

onMounted(refresh)
</script>