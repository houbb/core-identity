<template>
  <div class="page-container">
    <h1>邀请记录</h1>
    <div class="toolbar">
      <router-link :to="`/organizations/${orgId}/invite`" class="btn btn-primary">邀请成员</router-link>
    </div>
    <div class="card mt">
      <div v-if="invitations.length === 0" class="empty">没有待处理邀请</div>
      <div v-for="inv in invitations" :key="inv.id" class="inv-row">
        <div class="inv-info">
          <strong>{{ inv.emailDisplay || inv.emailNormalized }}</strong>
          <small>邀请时间: {{ formatDate(inv.createdAt) }}</small>
        </div>
        <span :class="['badge', statusClass(inv.status)]">{{ inv.status }}</span>
        <button v-if="inv.status === 'PENDING'" class="btn btn-small" @click="revoke(inv.id)">撤销</button>
        <button v-if="inv.status === 'PENDING'" class="btn btn-small" @click="resend(inv.id)">重发</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { invitationAPI } from '@/api/organizations'

const route = useRoute()
const orgId = route.params.organizationId as string
const invitations = ref<any[]>([])

onMounted(async () => {
  try {
    const { data } = await invitationAPI.getList(orgId)
    invitations.value = data.invitations || []
  } catch {}
})

async function resend(invId: string) { await invitationAPI.resend(orgId, invId) }
async function revoke(invId: string) {
  await invitationAPI.revoke(orgId, invId)
  const inv = invitations.value.find(i => i.id === invId)
  if (inv) inv.status = 'REVOKED'
}

function statusClass(status: string) {
  const map: Record<string, string> = { PENDING: 'badge-info', ACCEPTED: 'badge-success', DECLINED: 'badge-warning', REVOKED: 'badge-error', EXPIRED: 'badge-error' }
  return map[status] || 'badge-info'
}

function formatDate(ts: number) { return new Date(ts).toLocaleDateString('zh-CN') }
</script>

<style scoped>
.page-container { max-width: 800px; }
.toolbar { display: flex; gap: 8px; }
.mt { margin-top: 16px; }
.empty { color: var(--text-secondary); text-align: center; padding: 24px; }
.inv-row { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border); }
.inv-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.btn-small { padding: 4px 10px; font-size: 12px; }
</style>