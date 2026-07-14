import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/admin-api/v1/identity',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.code === 'ERR_NETWORK' || !error.response) {
      return Promise.reject({ type: 'NETWORK_ERROR', message: 'Network error' })
    }
    return Promise.reject(error)
  }
)

export default apiClient