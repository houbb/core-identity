import { defineStore } from 'pinia'
import axios from 'axios'

const adminApi = axios.create({
  baseURL: '/admin-api/v1/identity',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

export const useUserManagementStore = defineStore('userManagement', () => {
  async function fetchUsers(page = 0, size = 50, status?: string, email?: string) {
    const params = new URLSearchParams({ page: String(page), size: String(size) })
    if (status) params.append('status', status)
    if (email) params.append('email', email)
    const { data } = await adminApi.get(`/users?${params}`)
    return data
  }

  async function getUserDetail(userId: string) {
    const { data } = await adminApi.get(`/users/${userId}`)
    return data
  }

  async function createUser(email: string, displayName: string) {
    const { data } = await adminApi.post('/users', { email, displayName })
    return data
  }

  async function disableUser(userId: string, reason: string) {
    return adminApi.post(`/users/${userId}/disable`, { reason })
  }

  async function enableUser(userId: string) {
    return adminApi.post(`/users/${userId}/enable`)
  }

  async function revokeSessions(userId: string) {
    return adminApi.post(`/users/${userId}/revoke-sessions`)
  }

  return { fetchUsers, getUserDetail, createUser, disableUser, enableUser, revokeSessions }
})