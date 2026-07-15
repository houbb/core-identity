<template>
  <div class="org-switcher">
    <button class="switcher-btn" @click="open = !open">
      <span class="org-avatar">{{ avatarText }}</span>
      <span class="org-name">{{ currentName }}</span>
      <span class="org-type-badge" :class="orgTypeClass">{{ orgTypeLabel }}</span>
      <span class="arrow">▼</span>
    </button>
    <div v-if="open" class="switcher-dropdown">
      <div class="dropdown-section">
        <div class="dropdown-label">最近使用</div>
        <button
          v-for="org in teamOrgs"
          :key="org.id"
          class="dropdown-item"
          :class="{ active: org.id === orgStore.currentOrgId }"
          @click="select(org.id)"
        >
          <span class="org-avatar small">{{ org.name.charAt(0).toUpperCase() }}</span>
          <span>{{ org.name }}</span>
          <span v-if="org.id === orgStore.currentOrgId" class="check">✓</span>
        </button>
      </div>
      <div v-if="personalOrg" class="dropdown-section">
        <div class="dropdown-label">个人空间</div>
        <button
          class="dropdown-item"
          :class="{ active: personalOrg.id === orgStore.currentOrgId }"
          @click="select(personalOrg.id)"
        >
          <span class="org-avatar small">👤</span>
          <span>{{ personalOrg.name }}</span>
        </button>
      </div>
      <div class="dropdown-footer">
        <router-link to="/organizations/new" class="btn btn-accent btn-small" @click="open = false">
          + 创建团队
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useOrganizationStore } from '@/stores/organizationStore'

const orgStore = useOrganizationStore()
const open = ref(false)

const teamOrgs = computed(() =>
  orgStore.organizations.filter(o => o.organizationType === 'TEAM')
)

const personalOrg = computed(() =>
  orgStore.organizations.find(o => o.organizationType === 'PERSONAL') || null
)

const avatarText = computed(() => {
  const org = orgStore.currentOrg
  return org?.name?.charAt(0)?.toUpperCase() || '?'
})

const currentName = computed(() => {
  const org = orgStore.currentOrg
  if (!org) return '选择组织'
  return org.name.length > 12 ? org.name.substring(0, 12) + '…' : org.name
})

const orgTypeLabel = computed(() =>
  orgStore.currentOrg?.organizationType === 'PERSONAL' ? '个人' : '团队'
)

const orgTypeClass = computed(() =>
  orgStore.currentOrg?.organizationType === 'PERSONAL' ? 'badge-info' : 'badge-success'
)

async function select(orgId: string) {
  open.value = false
  await orgStore.switchOrganization(orgId)
}
</script>

<style scoped>
.org-switcher {
  position: relative;
}

.switcher-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--bg-primary);
  cursor: pointer;
  font-size: 13px;
  color: var(--text-primary);
}

.switcher-btn:hover {
  background: var(--bg-secondary);
}

.org-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: var(--accent-bg);
  color: var(--accent);
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.org-avatar.small {
  width: 22px;
  height: 22px;
  font-size: 11px;
}

.org-name {
  flex: 1;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.org-type-badge {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
}

.arrow {
  font-size: 10px;
  color: var(--text-secondary);
}

.switcher-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  background: var(--bg-primary);
  border: 1px solid var(--border);
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  z-index: 100;
  max-height: 360px;
  overflow-y: auto;
}

.dropdown-section {
  padding: 4px;
}

.dropdown-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-secondary);
  padding: 6px 10px 2px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 8px 10px;
  border: none;
  background: none;
  cursor: pointer;
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-primary);
}

.dropdown-item:hover {
  background: var(--bg-secondary);
}

.dropdown-item.active {
  background: var(--accent-bg);
  color: var(--accent);
}

.check {
  margin-left: auto;
  color: var(--accent);
}

.dropdown-footer {
  padding: 8px;
  border-top: 1px solid var(--border);
}

.btn-small {
  width: 100%;
  padding: 6px 12px;
  font-size: 12px;
}
</style>