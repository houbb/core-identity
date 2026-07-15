<template>
  <div class="page-container">
    <h1>角色管理</h1>
    <div class="toolbar">
      <button class="btn btn-primary" @click="showCreate = true">创建角色</button>
    </div>
    <div class="card mt">
      <div v-if="roles.length === 0" class="empty">暂时没有自定义角色</div>
      <div v-for="r in roles" :key="r.id" class="role-row">
        <div class="role-info">
          <strong>{{ r.name }}</strong>
          <small>{{ r.description || '-' }}</small>
        </div>
        <span :class="['badge', r.roleType === 'BUILT_IN' ? 'badge-warning' : 'badge-info']">
          {{ r.roleType === 'BUILT_IN' ? '内置' : '自定义' }}
        </span>
        <span>权限: {{ r.permissionCount }}</span>
        <router-link :to="`/organizations/${orgId}/roles/${r.id}`" class="btn btn-small">编辑</router-link>
        <button v-if="r.roleType !== 'BUILT_IN'" class="btn btn-small" @click="deleteRole(r.id)">删除</button>
      </div>
    </div>

    <!-- Create modal -->
    <div v-if="showCreate" class="modal-overlay" @click.self="showCreate = false">
      <div class="modal">
        <h2>创建角色</h2>
        <label class="field mt"><span>名称</span><input v-model="newName" class="input" /></label>
        <label class="field"><span>描述</span><input v-model="newDesc" class="input" /></label>
        <div class="modal-actions">
          <button class="btn" @click="showCreate = false">取消</button>
          <button class="btn btn-primary" @click="createRole">创建</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { roleAPI } from '@/api/organizations'

const route = useRoute()
const orgId = route.params.organizationId as string
const roles = ref<any[]>([])
const showCreate = ref(false)
const newName = ref('')
const newDesc = ref('')

onMounted(async () => {
  await loadRoles()
})

async function loadRoles() {
  try {
    const { data } = await roleAPI.getRoles(orgId)
    roles.value = data.roles || []
  } catch {}
}

async function createRole() {
  if (!newName.value.trim()) return
  await roleAPI.create(orgId, newName.value.trim(), newDesc.value)
  showCreate.value = false
  newName.value = ''
  newDesc.value = ''
  await loadRoles()
}

async function deleteRole(roleId: string) {
  if (!confirm('确认删除该角色？')) return
  await roleAPI.delete(orgId, roleId)
  await loadRoles()
}
</script>

<style scoped>
.page-container { max-width: 800px; }
.toolbar { display: flex; gap: 8px; }
.mt { margin-top: 16px; }
.empty { color: var(--text-secondary); text-align: center; padding: 24px; }
.role-row { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--border); }
.role-info { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.btn-small { padding: 4px 10px; font-size: 12px; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: var(--bg-primary); border-radius: 12px; padding: 24px; min-width: 360px; }
.modal-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; font-size: 13px; }
.input { padding: 8px 12px; border: 1px solid var(--border); border-radius: 8px; font-size: 13px; }
</style>