import axios from 'axios'

const http = axios.create({
  baseURL: '/admin-api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('admin_access_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor
http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    console.error('Admin API Error:', error)
    return Promise.reject(error)
  },
)

export default http