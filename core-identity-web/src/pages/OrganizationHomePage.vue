<template>
  <div class="page-container">
    <div class="card">
      <h1>{{ org?.name || '加载中…' }}</h1>
      <p v-if="org?.description" class="desc">{{ org.description }}</p>
      <div class="stats">
        <div class="stat">
          <span class="stat-label">状态</span>
          <span :class="['badge', statusClass]">{{ statusLabel }}</span>
        </div>
        <div class="stat">
          <span class="stat-label">类型</span>
          <span>{{ org?.organizationType === 'PERSONAL' ? '个人空间' : '团队组织' }}</span>
        </div>
        <div class="stat">
          <span class="stat-label">创建时间</span>
          <span>{{ formatDate(org?.createdAt) }}</span>
        </div>
      </div>
      <div v-if="org?.organizationType === 'TEAM'" class="actions">
        <router-link :to="`/organizations/${org?.id}/members`" class="btn">成员管理</router-link>
        <router-link :to="`/organizations/${org?.id}/roles`" class="btn">角色管理</router-link>
        <router-link :to="`/organizations/${org?.id}/invitations`" class="btn">邀请记录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { orgAPI } from '@/api/organizations'
import type { OrganizationInfo } from '@/stores/organizationStore'

const route = useRoute()
const org = ref<OrganizationInfo | null>(null)

onMounted(async () => {
  try {
    const { data } = await orgAPI.get(route.params.organizationId as string)
    org.value = data as OrganizationInfo
  } catch {
    org.value = null
  }
})

const statusClass = computed(() => {
  if (org.value?.status === 'ACTIVE') return 'badge-success'
  if (org.value?.status === 'SUSPENDED') return 'badge-warning'
  return 'badge-error'
})

const statusLabel = computed(() => {
  const map: Record<string, string> = {
    ACTIVE: '正常', SUSPENDED: '已冻结', PENDING_DELETION: '待删除', DELETED: '已删除'
  }
  return map[org.value?.status || ''] || org.value?.status || ''
})

function formatDate(ts?: number): string {
  if (!ts) return '-'
  return new Date(ts).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.page-container { max-width: 800px; }
.desc { color: var(--text-secondary); margin-top: 8px; }
.stats { display: flex; gap: 24px; margin-top: 16px; }
.stat { display: flex; flex-direction: column; gap: 4px; }
.stat-label { font-size: 11px; color: var(--text-secondary); }
.actions { margin-top: 24px; display: flex; gap: 8px; flex-wrap: wrap; }
</style>