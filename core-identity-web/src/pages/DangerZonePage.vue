<template>
  <div class="page-container">
    <h1>危险区域</h1>
    <div class="card mt">
      <h2>转移所有权</h2>
      <p class="desc">将组织所有权转移给另一名成员。此操作不可撤销。</p>
      <label class="field mt"><span>新所有者用户 ID</span><input v-model="newOwnerId" class="input" /></label>
      <label class="field"><span>当前密码</span><input v-model="transferPassword" type="password" class="input" /></label>
      <button class="btn btn-accent" @click="doTransfer">转移所有权</button>
    </div>

    <div class="card mt">
      <h2>解散组织</h2>
      <p class="desc">申请解散组织，进入 7 天冷静期。期间可以取消。</p>
      <label class="field mt"><span>确认密码</span><input v-model="deletePassword" type="password" class="input" /></label>
      <button class="btn btn-accent" @click="doRequestDeletion">申请解散</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orgAPI } from '@/api/organizations'

const route = useRoute()
const router = useRouter()
const orgId = route.params.organizationId as string
const newOwnerId = ref('')
const transferPassword = ref('')
const deletePassword = ref('')

async function doTransfer() {
  if (!newOwnerId.value || !transferPassword.value) return
  await orgAPI.transferOwnership(orgId, newOwnerId.value, transferPassword.value)
  alert('所有权已转移')
}

async function doRequestDeletion() {
  if (!confirm('确认解散组织？此操作不可撤销。')) return
  await orgAPI.requestDeletion(orgId, deletePassword.value)
  router.push('/organizations')
}
</script>

<style scoped>
.page-container { max-width: 600px; }
.mt { margin-top: 16px; }
.desc { color: var(--text-secondary); font-size: 13px; }
.field { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; font-size: 13px; }
.input { padding: 8px 12px; border: 1px solid var(--border); border-radius: 8px; font-size: 13px; }
</style>