<template>
  <div class="state-page">
    <div class="state-icon">⚠️</div>
    <h1 class="state-title">系统暂不可用</h1>
    <p class="state-detail">
      Identity Backend 无法连接。可能是服务未启动或网络故障。
    </p>
    <div class="state-actions">
      <button class="btn btn-primary" @click="reconnect">重新连接</button>
      <button class="btn" @click="copyDiagnostics">复制诊断信息</button>
    </div>
    <small style="margin-top: 16px; white-space: pre-line;">{{ diagnosticInfo }}</small>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const diagnosticInfo = ref('')

function gatherDiagnostics(): string {
  const info = [
    `User Agent: ${navigator.userAgent}`,
    `Online: ${navigator.onLine}`,
    `Time: ${new Date().toISOString()}`,
    `URL: ${window.location.href}`,
    `Backend: http://localhost:8101`
  ]
  return info.join('\n')
}

function reconnect() {
  window.location.reload()
}

function copyDiagnostics() {
  diagnosticInfo.value = gatherDiagnostics()
  navigator.clipboard.writeText(diagnosticInfo.value).then(() => {
    diagnosticInfo.value += '\n[诊断信息已复制到剪贴板]'
  })
}
</script>