import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiClient from '@/api'

export interface OrganizationInfo {
  id: string
  organizationType: string
  name: string
  slug: string
  description: string
  status: string
  ownerUserId: string
  authorizationVersion: number
  createdAt: number
}

export interface PermissionSnapshot {
  organizationId: string
  membershipId: string
  roles: string[]
  permissions: string[]
  permissionVersion: number
}

export const useOrganizationStore = defineStore('organization', () => {
  const organizations = ref<OrganizationInfo[]>([])
  const currentOrgId = ref<string | null>(localStorage.getItem('currentOrgId'))
  const permissionSnapshot = ref<PermissionSnapshot | null>(null)
  const loading = ref(false)

  const currentOrg = computed(() =>
    organizations.value.find(o => o.id === currentOrgId.value) || null
  )

  async function fetchMyOrganizations() {
    loading.value = true
    try {
      const { data } = await apiClient.get('/me/organizations')
      organizations.value = data.organizations || []

      // If no current org selected, pick first
      if (!currentOrgId.value && organizations.value.length > 0) {
        currentOrgId.value = organizations.value[0].id
        localStorage.setItem('currentOrgId', currentOrgId.value!)
      }
    } catch {
      organizations.value = []
    } finally {
      loading.value = false
    }
  }

  async function switchOrganization(orgId: string) {
    currentOrgId.value = orgId
    localStorage.setItem('currentOrgId', orgId)
    try {
      await apiClient.post('/me/current-organization', { organizationId: orgId })
    } catch {
      // Non-critical
    }
    await fetchPermissions()
  }

  async function fetchPermissions() {
    if (!currentOrgId.value) return
    try {
      const { data } = await apiClient.get(`/me/organizations/${currentOrgId.value}/permissions`)
      permissionSnapshot.value = data
    } catch {
      permissionSnapshot.value = null
    }
  }

  function hasPermission(code: string): boolean {
    return permissionSnapshot.value?.permissions?.includes(code) ?? false
  }

  function clearOrg() {
    currentOrgId.value = null
    localStorage.removeItem('currentOrgId')
    permissionSnapshot.value = null
  }

  return {
    organizations, currentOrgId, permissionSnapshot, loading, currentOrg,
    fetchMyOrganizations, switchOrganization, fetchPermissions,
    hasPermission, clearOrg
  }
})
