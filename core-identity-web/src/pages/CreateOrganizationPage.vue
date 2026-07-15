<template>
  <div class="page-container">
    <h1>创建团队组织</h1>
    <div class="card" style="max-width:500px;margin-top:16px;">
      <label class="field">
        <span>组织名称</span>
        <input v-model="name" type="text" placeholder="例如：Acme Studio" class="input" />
      </label>
      <label class="field">
        <span>简介（可选）</span>
        <textarea v-model="description" rows="3" placeholder="简述组织用途…" class="input"></textarea>
      </label>
      <p v-if="error" class="error">{{ error }}</p>
      <button class="btn btn-primary" :disabled="!name.trim() || creating" @click="createOrg">
        {{ creating ? '创建中…' : '创建组织' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { orgAPI } from '@/api/organizations'
import { useOrganizationStore } from '@/stores/organizationStore'

const router = useRouter()
const orgStore = useOrganizationStore()
const name = ref('')
const description = ref('')
const creating = ref(false)
const error = ref('')

async function createOrg() {
  if (!name.value.trim()) return
  creating.value = true
  error.value = ''
  try {
    const { data } = await orgAPI.create(name.value.trim(), description.value)
    await orgStore.fetchMyOrganizations()
    await orgStore.switchOrganization(data.id)
    router.push(`/organizations/${data.id}`)
  } catch (e: any) {
    error.value = e?.response?.data?.detail || e?.message || '创建失败'
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.page-container { max-width: 600px; }
.field { display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; font-size: 13px; }
.input { padding: 8px 12px; border: 1px solid var(--border); border-radius: 8px; font-size: 13px; }
.error { color: var(--error); font-size: 12px; margin-bottom: 8px; }
</style>