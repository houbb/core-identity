import apiClient from './index'

export const orgAPI = {
  getMyOrganizations: () => apiClient.get('/me/organizations'),
  setCurrentOrg: (orgId: string) => apiClient.post('/me/current-organization', { organizationId: orgId }),
  create: (name: string, description: string) => apiClient.post('/organizations', { name, description }),
  get: (orgId: string) => apiClient.get(`/organizations/${orgId}`),
  update: (orgId: string, data: Record<string, string>) => apiClient.patch(`/organizations/${orgId}`, data),
  transferOwnership: (orgId: string, newOwnerUserId: string, password: string) =>
    apiClient.post(`/organizations/${orgId}/transfer-ownership`, { newOwnerUserId, password }),
  requestDeletion: (orgId: string, password: string) =>
    apiClient.post(`/organizations/${orgId}/request-deletion`, { password }),
  cancelDeletion: (orgId: string) => apiClient.post(`/organizations/${orgId}/cancel-deletion`),
}

export const roleAPI = {
  getRoles: (orgId: string) => apiClient.get(`/organizations/${orgId}/roles`),
  create: (orgId: string, name: string, description: string) =>
    apiClient.post(`/organizations/${orgId}/roles`, { name, description }),
  get: (orgId: string, roleId: string) => apiClient.get(`/organizations/${orgId}/roles/${roleId}`),
  update: (orgId: string, roleId: string, data: Record<string, string>) =>
    apiClient.patch(`/organizations/${orgId}/roles/${roleId}`, data),
  delete: (orgId: string, roleId: string) => apiClient.delete(`/organizations/${orgId}/roles/${roleId}`),
  assignPermissions: (orgId: string, roleId: string, permissionIds: string[]) =>
    apiClient.put(`/organizations/${orgId}/roles/${roleId}/permissions`, { permissionIds }),
}

export const permissionAPI = {
  getAll: (params?: Record<string, string>) => apiClient.get('/permissions', { params }),
  getSnapshot: (orgId: string) => apiClient.get(`/me/organizations/${orgId}/permissions`),
}

export const invitationAPI = {
  getList: (orgId: string) => apiClient.get(`/organizations/${orgId}/invitations`),
  create: (orgId: string, email: string, roleIds: string[]) =>
    apiClient.post(`/organizations/${orgId}/invitations`, { email, roleIds }),
  resend: (orgId: string, invId: string) => apiClient.post(`/organizations/${orgId}/invitations/${invId}/resend`),
  revoke: (orgId: string, invId: string) => apiClient.delete(`/organizations/${orgId}/invitations/${invId}`),
  resolve: (token: string) => apiClient.get('/invitations/resolve', { params: { token } }),
  accept: (token: string) => apiClient.post('/invitations/accept', { token }),
  decline: (token: string) => apiClient.post('/invitations/decline', { token }),
}

export const memberAPI = {
  getList: (orgId: string, params?: Record<string, string>) =>
    apiClient.get(`/organizations/${orgId}/members`, { params }),
  get: (orgId: string, membershipId: string) => apiClient.get(`/organizations/${orgId}/members/${membershipId}`),
  updateRoles: (orgId: string, membershipId: string, roleIds: string[]) =>
    apiClient.put(`/organizations/${orgId}/members/${membershipId}/roles`, { roleIds }),
  remove: (orgId: string, membershipId: string) => apiClient.delete(`/organizations/${orgId}/members/${membershipId}`),
  leave: (orgId: string) => apiClient.post(`/organizations/${orgId}/leave`),
}
