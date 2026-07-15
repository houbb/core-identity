import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

const adminApi = axios.create({
  baseURL: '/admin-api/v1/identity',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

export const useAdminAuthStore = defineStore('adminAuth', () => {
  const admin = ref<any>(null)
  const isAuthenticated = computed(() => admin.value != null)

  async function login(email: string, password: string) {
    const { data } = await adminApi.post('/auth/login', { email, password })
    admin.value = data
    return data
  }

  async function checkAuth() {
    try {
      const { data } = await adminApi.get('/auth/me')
      if (data && data.active) {
        admin.value = data
      } else {
        admin.value = null
      }
    } catch {
      admin.value = null
    }
  }

  async function logout() {
    await adminApi.post('/auth/logout')
    admin.value = null
  }

  return { admin, isAuthenticated, login, logout, checkAuth }
})