<template>
  <div class="state-page">
    <div class="state-icon">⬆️</div>
    <h1 class="state-title">需要升级</h1>
    <p class="state-detail">
      前端版本 <strong>{{ webVersion }}</strong> 需要 Identity API ≥ {{ requiredApiVersion }}<br/>
      当前后端版本为 <strong>{{ backendVersion }}</strong>。
    </p>
    <p class="state-detail">
      请升级后端，或使用匹配版本的前端。
    </p>
    <div class="state-actions">
      <button class="btn btn-primary" @click="checkAgain">再次检查</button>
      <button class="btn" @click="copyDiagnostics">复制诊断信息</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const webVersion = '0.1.0'
const requiredApiVersion = 'v1'
const backendVersion = ref('0.0.0')

function checkAgain() {
  window.location.reload()
}

function copyDiagnostics() {
  navigator.clipboard.writeText(
    `Web: ${webVersion}, Required API: ${requiredApiVersion}, Backend: ${backendVersion.value}`
  )
}
</script>