<template>
  <div class="state-page">
    <div class="card" style="width: 100%; max-width: 420px; text-align: center;">
      <div class="state-icon">📧</div>
      <h1 class="state-title">验证邮箱</h1>
      <p class="state-detail">
        验证邮件已经发送到 <strong>{{ maskedEmail }}</strong>
      </p>
      <p style="font-size: 12px; color: var(--text-secondary); margin-bottom: 20px;">
        请在 30 分钟内完成验证。
      </p>

      <div class="state-actions">
        <button class="btn btn-primary" @click="resend" :disabled="cooldown > 0">
          {{ cooldown > 0 ? `${cooldown}s 后可重新发送` : '重新发送验证邮件' }}
        </button>
        <router-link to="/login" class="btn btn-accent">返回登录</router-link>
      </div>

      <div v-if="msg" style="margin-top: 12px; font-size: 12px; color: var(--success);">{{ msg }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const route = useRoute()
const auth = useAuthStore()
const email = computed(() => (route.query.email as string) || '')
const maskedEmail = computed(() => {
  const e = email.value
  const at = e.indexOf('@')
  return at > 0 ? e[0] + '***' + e.slice(at) : '***'
})

const cooldown = ref(0)
const msg = ref('')
let timer: any = null

async function resend() {
  if (cooldown.value > 0) return
  try {
    await auth.resendVerification(email.value)
    msg.value = '验证邮件已重新发送'
    cooldown.value = 60
    timer = setInterval(() => {
      cooldown.value--
      if (cooldown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch {
    msg.value = '发送失败，请稍后重试'
  }
}

onUnmounted(() => { if (timer) clearInterval(timer) })
</script>