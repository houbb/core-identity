<template>
  <div class="state-page">
    <div v-if="loading">加载中…</div>
    <template v-else-if="invitation">
      <div class="state-icon">📧</div>
      <h1 class="state-title">{{ invitation.organizationName || '加入组织' }}</h1>
      <p class="state-detail">
        邀请人：{{ invitation.invitedByName || '-' }}<br/>
        邮箱：{{ invitation.emailDisplay }}<br/>
        有效期至：{{ formatDate(invitation.expiresAt) }}
      </p>
      <div v-if="!authStore.isAuthenticated" class="state-actions">
        <router-link to="/login" class="btn btn-primary">登录并接受</router-link>
        <router-link to="/register" class="btn">注册并接受</router-link>
      </div>
      <div v-else class="state-actions">
        <button class="btn btn-primary" @click="accept">接受邀请</button>
        <button class="btn" @click="decline">拒绝</button>
      </div>
    </template>
    <template v-else>
      <div class="state-icon">❌</div>
      <h1 class="state-title">邀请无效</h1>
      <p class="state-detail">{{ error || '邀请不存在或已过期' }}</p>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { invitationAPI } from '@/api/organizations'
import { useAuthStore } from '@/stores/authStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const token = route.params.token as string
const invitation = ref<any>(null)
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const { data } = await invitationAPI.resolve(token)
    invitation.value = data
  } catch (e: any) {
    error.value = e?.response?.data?.detail || '邀请无效'
  } finally {
    loading.value = false
  }
})

async function accept() {
  try {
    await invitationAPI.accept(token)
    router.push('/organizations')
  } catch (e: any) {
    error.value = e?.response?.data?.detail || '接受失败'
  }
}

async function decline() {
  await invitationAPI.decline(token)
  router.push('/')
}

function formatDate(ts: number) { return new Date(ts).toLocaleDateString('zh-CN') }
</script>