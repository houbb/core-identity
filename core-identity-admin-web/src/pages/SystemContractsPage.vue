<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <h1>接口契约</h1>
      <button class="btn btn-primary" @click="refresh">刷新</button>
    </div>

    <div class="grid-2">
      <div class="card">
        <h2>契约兼容性</h2>
        <div class="info-row"><span class="info-label">Status</span><span :class="`info-value badge ${compatBadge}`">{{ data?.compatibility || '...' }}</span></div>
      </div>
      <div class="card">
        <h2>版本信息</h2>
        <div class="info-row"><span class="info-label">Admin Backend</span><span class="info-value">{{ data?.adminBackendVersion || '...' }}</span></div>
        <div class="info-row"><span class="info-label">Admin API</span><span class="info-value">{{ data?.adminApiVersion || '...' }}</span></div>
        <div class="info-row"><span class="info-label">Identity Backend</span><span class="info-value">{{ data?.identityBackendVersion || '...' }}</span></div>
        <div class="info-row"><span class="info-label">Identity API</span><span class="info-value">{{ data?.identityApiVersion || '...' }}</span></div>
      </div>
    </div>

    <div style="margin-top: 24px;" class="grid-3">
      <div class="card">
        <h2>Public API</h2>
        <p style="margin-top: 8px; font-size: 12px; color: var(--text-secondary);">
          <code>/api/v1/identity/meta</code><br/>
          <code>/api/v1/identity/capabilities</code>
        </p>
      </div>
      <div class="card">
        <h2>Internal API</h2>
        <p style="margin-top: 8px; font-size: 12px; color: var(--text-secondary);">
          <code>/internal/v1/identity/service-tokens</code><br/>
          <code>/internal/v1/identity/system/info</code><br/>
          <code>/internal/v1/identity/system/health</code>
        </p>
      </div>
      <div class="card">
        <h2>Admin API</h2>
        <p style="margin-top: 8px; font-size: 12px; color: var(--text-secondary);">
          <code>/admin-api/v1/identity/system/overview</code><br/>
          <code>/admin-api/v1/identity/system/health</code><br/>
          <code>/admin-api/v1/identity/system/version</code>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import apiClient from '@/api'

const data = ref<any>(null)

const compatBadge = computed(() => {
  if (!data.value) return 'badge-info'
  switch (data.value.compatibility) {
    case 'COMPATIBLE': return 'badge-success'
    case 'INCOMPATIBLE': return 'badge-error'
    default: return 'badge-warning'
  }
})

async function refresh() {
  try {
    const res = await apiClient.get('/system/contracts')
    data.value = res.data
  } catch (e) {
    // ignore
  }
}

onMounted(refresh)
</script>