<template>
  <div class="page-container">
    <h1>成员管理</h1>
    <div class="toolbar">
      <router-link :to="`/organizations/${orgId}/invite`" class="btn btn-primary">邀请成员</router-link>
    </div>
    <div class="card mt">
      <div v-if="members.length === 0" class="empty">还没有其他成员</div>
      <div v-for="m in members" :key="m.membershipId" class="member-row">
        <div class="member-info">
          <strong>{{ m.displayName }}</strong>
          <small>{{ m.email }}</small>
        </div>
        <span :class="['badge', m.status === 'ACTIVE' ? 'badge-success' : 'badge-warning']">{{ m.status }}</span>
        <span class="badge badge-info">{{ m.roleNames.join(', ') }}</span>
        <button class="btn btn-small" @click="removeMember(m.membershipId)" :disabled="m.userId === ownerId">移除</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { memberAPI } from '@/api/organizations'
import { orgAPI } from '@/api/organizations'

const route = useRoute()
const orgId = route.params.organizationId as string
const members = ref<any[]>([])
const ownerId = ref('')

onMounted(async () => {
  try {
    const { data: org } = await orgAPI.get(orgId)
    ownerId.value = org.ownerUserId || ''
    const { data } = await memberAPI.getList(orgId)
    members.value = data.members || []
  } catch {}
})

async function removeMember(membershipId: string) {
  if (!confirm('确认移除该成员？')) return
  await memberAPI.remove(orgId, membershipId)
  members.value = members.value.filter(m => m.membershipId !== membershipId)
}
</script>

<style scoped>
.page-container { max-width: 900px; }
.toolbar { display: flex; gap: 8px; }
.mt { margin-top: 16px; }
.empty { color: var(--text-secondary); text-align: center; padding: 24px; }
.member-row { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border); }
.member-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.btn-small { padding: 4px 10px; font-size: 12px; }
</style>