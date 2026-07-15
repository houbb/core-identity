<template>
  <div class="app-layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <OrganizationSwitcher />
      </div>
      <nav class="sidebar-nav">
        <router-link to="/" class="nav-item" exact-active-class="active">
          <span class="nav-icon">🏠</span>
          <span class="nav-label">首页</span>
        </router-link>
        <template v-if="orgStore.currentOrg && orgStore.currentOrg.organizationType !== 'PERSONAL'">
          <router-link
            v-if="orgStore.hasPermission('identity.member.read')"
            :to="`/organizations/${orgStore.currentOrgId}/members`"
            class="nav-item"
            active-class="active"
          >
            <span class="nav-icon">👥</span>
            <span class="nav-label">成员</span>
          </router-link>
          <router-link
            v-if="orgStore.hasPermission('identity.invitation.read')"
            :to="`/organizations/${orgStore.currentOrgId}/invitations`"
            class="nav-item"
            active-class="active"
          >
            <span class="nav-icon">📧</span>
            <span class="nav-label">邀请</span>
          </router-link>
          <router-link
            v-if="orgStore.hasPermission('identity.role.read')"
            :to="`/organizations/${orgStore.currentOrgId}/roles`"
            class="nav-item"
            active-class="active"
          >
            <span class="nav-icon">🔐</span>
            <span class="nav-label">角色</span>
          </router-link>
          <router-link
            :to="`/organizations/${orgStore.currentOrgId}/settings`"
            class="nav-item"
            active-class="active"
          >
            <span class="nav-icon">⚙️</span>
            <span class="nav-label">设置</span>
          </router-link>
        </template>
        <router-link to="/account/profile" class="nav-item" active-class="active">
          <span class="nav-icon">👤</span>
          <span class="nav-label">个人</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <button class="btn btn-small" @click="authStore.logout(); $router.push('/login')">退出登录</button>
      </div>
    </aside>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useOrganizationStore } from '@/stores/organizationStore'
import OrganizationSwitcher from '@/components/OrganizationSwitcher.vue'

const authStore = useAuthStore()
const orgStore = useOrganizationStore()

onMounted(async () => {
  await orgStore.fetchMyOrganizations()
  if (orgStore.currentOrgId) {
    await orgStore.fetchPermissions()
  }
})
</script>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 220px;
  min-width: 220px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  padding: 12px;
}

.sidebar-header {
  margin-bottom: 16px;
}

.sidebar-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  color: var(--text-primary);
  text-decoration: none;
  font-size: 13px;
  transition: background 0.15s;
}

.nav-item:hover {
  background: rgba(0, 0, 0, 0.05);
}

.nav-item.active {
  background: var(--accent-bg);
  color: var(--accent);
  font-weight: 500;
}

.nav-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.nav-label {
  white-space: nowrap;
}

.sidebar-footer {
  padding-top: 12px;
  border-top: 1px solid var(--border);
}

.btn-small {
  width: 100%;
  padding: 6px 12px;
  font-size: 12px;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
</style>