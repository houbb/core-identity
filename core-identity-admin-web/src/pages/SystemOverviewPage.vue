<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <div>
        <h1>系统概览</h1>
        <small v-if="data">最近检查：{{ new Date().toLocaleString() }}</small>
      </div>
      <button class="btn btn-primary" @click="refresh">刷新</button>
    </div>

    <div class="grid-3">
      <!-- Admin Backend Card -->
      <div class="card status-card" @click="selectedCard = 'admin'">
        <div class="status-card-header">
          <span class="status-card-title">Admin Backend</span>
          <span :class="`badge badge-success`">HEALTHY</span>
        </div>
        <div class="status-card-value">v0.1.0</div>
        <small>Response: {{ data?.adminBackend?.uptime ? (data.adminBackend.uptime / 1000).toFixed(0) + 's uptime' : '...' }}</small>
      </div>

      <!-- Identity Backend Card -->
      <div class="card status-card" @click="selectedCard = 'identity'">
        <div class="status-card-header">
          <span class="status-card-title">Identity Backend</span>
          <span :class="`badge ${identityBadgeClass}`">{{ identityStatus }}</span>
        </div>
        <div class="status-card-value">{{ identityVersion }}</div>
        <small>{{ data?.identityBackend?.reachable ? 'Reachable' : 'Unreachable' }}</small>
      </div>

      <!-- Database Card -->
      <div class="card status-card" @click="selectedCard = 'database'">
        <div class="status-card-header">
          <span class="status-card-title">Database</span>
          <span :class="`badge ${dbBadgeClass}`">{{ dbStatus }}</span>
        </div>
        <div class="status-card-value">{{ dbType }}</div>
        <small>{{ data?.identityBackend?.info?.flywayStatus || '...' }}</small>
      </div>

      <!-- Public API Card -->
      <div class="card status-card">
        <div class="status-card-header">
          <span class="status-card-title">Public API</span>
          <span class="badge badge-info">v1</span>
        </div>
        <div class="status-card-value">/api/v1/identity</div>
        <small>Capabilities: <code>SYSTEM_META, ...</code></small>
      </div>

      <!-- Internal API Card -->
      <div class="card status-card">
        <div class="status-card-header">
          <span class="status-card-title">Internal API</span>
          <span :class="`badge ${identityBadgeClass}`">{{ identityStatus }}</span>
        </div>
        <div class="status-card-value">/internal/v1/identity</div>
        <small>Service auth: enabled</small>
      </div>

      <!-- Schema Migration Card -->
      <div class="card status-card">
        <div class="status-card-header">
          <span class="status-card-title">Schema Migration</span>
          <span :class="`badge badge-success`">MIGRATED</span>
        </div>
        <div class="status-card-value">{{ data?.identityBackend?.info?.flywayStatus || '...' }}</div>
        <small>Version: {{ data?.identityBackend?.info?.schemaVersion || '...' }}</small>
      </div>
    </div>

    <!-- Detail Drawer -->
    <div v-if="selectedCard" class="drawer-overlay" @click="selectedCard = null"></div>
    <div v-if="selectedCard" class="drawer">
      <div class="drawer-header">
        <h2>{{ drawerTitle }}</h2>
        <button class="drawer-close" @click="selectedCard = null">&times;</button>
      </div>

      <div v-if="selectedCard === 'admin'">
        <div class="info-row"><span class="info-label">Service</span><span class="info-value">core-identity-admin-backend</span></div>
        <div class="info-row"><span class="info-label">Version</span><span class="info-value">0.1.0</span></div>
        <div class="info-row"><span class="info-label">Status</span><span class="info-value badge badge-success">HEALTHY</span></div>
        <div class="info-row"><span class="info-label">Boot Time</span><span class="info-value">{{ data?.adminBackend?.bootTime || '...' }}</span></div>
      </div>

      <div v-if="selectedCard === 'identity'">
        <div class="info-row"><span class="info-label">Service</span><span class="info-value">core-identity-backend</span></div>
        <div class="info-row"><span class="info-label">Version</span><span class="info-value">{{ identityVersion }}</span></div>
        <div class="info-row"><span class="info-label">API Version</span><span class="info-value">{{ identityApiVersion }}</span></div>
        <div class="info-row"><span class="info-label">Reachable</span><span class="info-value">{{ data?.identityBackend?.reachable ? 'Yes' : 'No' }}</span></div>
      </div>

      <div v-if="selectedCard === 'database'">
        <div class="info-row"><span class="info-label">Type</span><span class="info-value">{{ dbType }}</span></div>
        <div class="info-row"><span class="info-label">Connection</span><span class="info-value">{{ dbStatus }}</span></div>
        <div class="info-row"><span class="info-label">Flyway</span><span class="info-value">{{ data?.identityBackend?.info?.flywayStatus || '...' }}</span></div>
      </div>

      <div style="margin-top: 20px;">
        <button class="btn btn-accent" @click="copyDiagnostics">复制诊断信息</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import apiClient from '@/api'

interface OverviewData {
  overallStatus: string
  timestamp: string
  adminBackend: any
  identityBackend: any
}

const data = ref<OverviewData | null>(null)
const selectedCard = ref<string | null>(null)
const error = ref<string | null>(null)

const identityStatus = computed(() => {
  if (!data.value) return 'CHECKING...'
  return data.value.identityBackend?.reachable ? 'HEALTHY' : 'UNAVAILABLE'
})
const identityVersion = computed(() => data.value?.identityBackend?.info?.version || '...')
const identityApiVersion = computed(() => data.value?.identityBackend?.info?.apiVersion || '...')
const dbType = computed(() => data.value?.identityBackend?.info?.databaseType || '...')
const dbStatus = computed(() => data.value?.identityBackend?.info?.databaseStatus || '...')

const identityBadgeClass = computed(() => identityStatus.value === 'HEALTHY' ? 'badge-success' : 'badge-error')
const dbBadgeClass = computed(() => dbStatus.value === 'CONNECTED' ? 'badge-success' : 'badge-error')

const drawerTitle = computed(() => {
  switch (selectedCard.value) {
    case 'admin': return 'Admin Backend Details'
    case 'identity': return 'Identity Backend Details'
    case 'database': return 'Database Details'
    default: return ''
  }
})

async function refresh() {
  error.value = null
  try {
    const res = await apiClient.get<OverviewData>('/system/overview')
    data.value = res.data
  } catch (e: any) {
    error.value = '获取系统概览失败'
    if (e.type === 'NETWORK_ERROR') {
      error.value = 'Admin Backend 无法连接'
    }
  }
}

function copyDiagnostics() {
  navigator.clipboard.writeText(JSON.stringify(data.value, null, 2))
}

onMounted(refresh)
</script>