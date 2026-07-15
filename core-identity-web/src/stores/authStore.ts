import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import apiClient from '@/api'

export interface CurrentUser {
  userId: string
  displayName: string
  email: string
  organizationId: string
  authenticated: boolean
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<CurrentUser | null>(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => user.value?.authenticated ?? false)

  async function checkSession() {
    try {
      const { data } = await apiClient.get('/auth/session')
      if (data.authenticated) {
        user.value = {
          userId: data.userId,
          displayName: '',
          email: '',
          organizationId: '',
          authenticated: true
        }
      }
    } catch {
      user.value = null
    }
  }

  async function login(email: string, password: string) {
    const { data } = await apiClient.post('/auth/login', { email, password })
    user.value = { ...data, authenticated: true }
    return data
  }

  async function register(email: string, password: string, displayName: string, idempotencyKey?: string) {
    const { data } = await apiClient.post('/auth/register', {
      email, password, displayName, idempotencyKey
    })
    return data
  }

  async function logout() {
    await apiClient.post('/auth/logout')
    user.value = null
  }

  async function verifyEmail(token: string) {
    const { data } = await apiClient.post('/auth/email-verifications/confirm', { token })
    return data
  }

  async function resendVerification(email: string) {
    const { data } = await apiClient.post('/auth/email-verifications', { email })
    return data
  }

  async function requestPasswordReset(email: string) {
    const { data } = await apiClient.post('/auth/password-resets', { email })
    return data
  }

  async function completePasswordReset(token: string, email: string, newPassword: string) {
    const { data } = await apiClient.post('/auth/password-resets/confirm', {
      token, email, newPassword
    })
    return data
  }

  return {
    user, loading, isAuthenticated,
    checkSession, login, register, logout,
    verifyEmail, resendVerification,
    requestPasswordReset, completePasswordReset
  }
})