import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authService from '@/services/authService.js'
import router from '@/router/index.js'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => !!token.value)

  function initAuth() {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    if (savedToken && savedUser) {
      token.value = savedToken
      user.value = JSON.parse(savedUser)
    }
  }

  async function login(credentials) {
    loading.value = true
    try {
      const response = await authService.login(credentials)
      token.value = response.token
      user.value = { id: response.id, name: response.name, email: response.email }
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(user.value))
      router.push('/dashboard')
    } finally {
      loading.value = false
    }
  }

  async function register(data) {
    loading.value = true
    try {
      const response = await authService.register(data)
      token.value = response.token
      user.value = { id: response.id, name: response.name, email: response.email }
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(user.value))
      router.push('/dashboard')
    } finally {
      loading.value = false
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }

  return { user, token, loading, isAuthenticated, initAuth, login, register, logout }
})
