<template>
  <div class="page-container">
    <h1>编辑角色</h1>
    <div class="card mt">
      <label class="field">
        <span>名称</span>
        <input v-model="roleName" class="input" :disabled="role?.systemProtected" />
      </label>
      <label class="field">
        <span>描述</span>
        <input v-model="roleDesc" class="input" />
      </label>
      <button v-if="!role?.systemProtected" class="btn btn-primary" @click="saveInfo">保存</button>
      <p v-else class="note">内置角色，名称和类型不可修改。</p>
    </div>

    <h2 style="margin-top:24px;">权限</h2>
    <div class="card mt">
      <div v-if="allPermissions.length === 0">暂无可用权限</div>
      <div v-for="p in groupedPermissions" :key="p.service" class="perm-section">
        <h3>{{ p.service }}</h3>
        <label v-for="perm in p.permissions" :key="perm.id" class="perm-row">
          <input type="checkbox" :checked="selectedIds.has(perm.id)" @change="togglePerm(perm.id)" />
          <span class="perm-name">{{ perm.name }}</span>
          <small class="perm-code">{{ perm.code }}</small>
          <span :class="['badge', riskClass(perm.riskLevel)]">{{ perm.riskLevel }}</span>
        </label>
      </div>
      <button class="btn btn-primary mt" @click="savePermissions">保存权限</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { roleAPI, permissionAPI } from '@/api/organizations'

const route = useRoute()
const orgId = route.params.organizationId as string
const roleId = route.params.roleId as string
const role = ref<any>(null)
const roleName = ref('')
const roleDesc = ref('')
const allPermissions = ref<any[]>([])
const selectedIds = ref(new Set<string>())

onMounted(async () => {
  const [{ data: r }, { data: perms }] = await Promise.all([
    roleAPI.get(orgId, roleId),
    permissionAPI.getAll()
  ])
  role.value = r
  roleName.value = r.name
  roleDesc.value = r.description || ''
  allPermissions.value = perms.permissions || []
  r.permissionIds?.forEach((id: string) => selectedIds.value.add(id))
})

const groupedPermissions = computed(() => {
  const groups: Record<string, any[]> = {}
  for (const p of allPermissions.value) {
    if (!groups[p.service]) groups[p.service] = []
    groups[p.service].push(p)
  }
  return Object.entries(groups).map(([service, permissions]) => ({ service, permissions }))
})

function togglePerm(id: string) {
  if (selectedIds.value.has(id)) selectedIds.value.delete(id)
  else selectedIds.value.add(id)
}

async function saveInfo() {
  await roleAPI.update(orgId, roleId, { name: roleName.value, description: roleDesc.value })
}

async function savePermissions() {
  await roleAPI.assignPermissions(orgId, roleId, Array.from(selectedIds.value))
}

function riskClass(level: string) {
  const map: Record<string, string> = { LOW: 'badge-success', MEDIUM: 'badge-info', HIGH: 'badge-warning', CRITICAL: 'badge-error' }
  return map[level] || 'badge-info'
}
</script>

<style scoped>
.page-container { max-width: 800px; }
.mt { margin-top: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; font-size: 13px; }
.input { padding: 8px 12px; border: 1px solid var(--border); border-radius: 8px; font-size: 13px; }
.note { color: var(--text-secondary); font-size: 12px; margin-top: 8px; }
.perm-section { margin-bottom: 16px; }
.perm-section h3 { font-size: 13px; margin-bottom: 6px; color: var(--text-secondary); }
.perm-row { display: flex; align-items: center; gap: 8px; padding: 4px 0; font-size: 13px; cursor: pointer; }
.perm-name { flex: 1; }
.perm-code { font-size: 11px; color: var(--text-secondary); font-family: monospace; }
</style>