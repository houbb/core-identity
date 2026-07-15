<template>
  <div class="page-container">
    <div class="card">
      <h1>组织设置</h1>
      <label class="field mt">
        <span>组织名称</span>
        <input v-model="name" type="text" class="input" />
      </label>
      <label class="field">
        <span>简介</span>
        <textarea v-model="description" rows="3" class="input"></textarea>
      </label>
      <button class="btn btn-primary" @click="save">保存</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { orgAPI } from '@/api/organizations'

const route = useRoute()
const name = ref('')
const description = ref('')

onMounted(async () => {
  try {
    const { data } = await orgAPI.get(route.params.organizationId as string)
    name.value = data.name
    description.value = data.description || ''
  } catch {}
})

async function save() {
  await orgAPI.update(route.params.organizationId as string, { name: name.value, description: description.value })
}
</script>

<style scoped>
.page-container { max-width: 600px; }
.mt { margin-top: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; font-size: 13px; }
.input { padding: 8px 12px; border: 1px solid var(--border); border-radius: 8px; font-size: 13px; }
</style>