<template>
  <div class="page-container">
    <h1>邀请成员</h1>
    <div class="card" style="max-width:400px;margin-top:16px;">
      <label class="field">
        <span>邮箱</span>
        <input v-model="email" type="email" placeholder="member@example.com" class="input" />
      </label>
      <p v-if="error" class="error">{{ error }}</p>
      <button class="btn btn-primary" :disabled="!email.trim()" @click="invite">发送邀请</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { invitationAPI } from '@/api/organizations'

const route = useRoute()
const router = useRouter()
const orgId = route.params.organizationId as string
const email = ref('')
const error = ref('')

async function invite() {
  error.value = ''
  try {
    await invitationAPI.create(orgId, email.value.trim(), [])
    router.push(`/organizations/${orgId}/invitations`)
  } catch (e: any) {
    error.value = e?.response?.data?.detail || e?.message || '邀请失败'
  }
}
</script>

<style scoped>
.page-container { max-width: 500px; }
.field { display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; font-size: 13px; }
.input { padding: 8px 12px; border: 1px solid var(--border); border-radius: 8px; font-size: 13px; }
.error { color: var(--error); font-size: 12px; margin-bottom: 8px; }
</style>