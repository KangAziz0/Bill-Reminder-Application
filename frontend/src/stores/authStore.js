import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authService from '@/services/authService.js'
import router from '@/router/index.js'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(null)
  const loading = ref(false)
  const pendingAuth = ref(null)

  const isAuthenticated = computed(() => !!token.value)

  function initAuth() {
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    const savedPending = sessionStorage.getItem('pendingAuth')
    if (savedToken && savedUser) {
      token.value = savedToken
      user.value = JSON.parse(savedUser)
    }
    if (savedPending) {
      pendingAuth.value = JSON.parse(savedPending)
    }
  }

  async function login(credentials) {
    loading.value = true
    try {
      const response = await authService.login(credentials)
      completeLogin(response)
    } catch (e) {
      handleOtpTransition('login', credentials, e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function register(data) {
    loading.value = true
    try {
      const response = await authService.register(data)
      completeLogin(response)
    } catch (e) {
      handleOtpTransition('register', data, e)
      throw e
    } finally {
      loading.value = false
    }
  }

  async function verifyPendingAuth(otp) {
    if (!pendingAuth.value) throw new Error('Tidak ada proses OTP aktif')
    const payload = { ...pendingAuth.value.payload, otp }
    loading.value = true
    try {
      const response = pendingAuth.value.mode === 'login'
        ? await authService.login(payload)
        : await authService.register(payload)
      sessionStorage.removeItem('pendingAuth')
      pendingAuth.value = null
      completeLogin(response)
    } finally {
      loading.value = false
    }
  }

  async function resendOtp() {
    if (!pendingAuth.value) throw new Error('Tidak ada proses OTP aktif')
    const response = await authService.resendOtp({
      email: pendingAuth.value.email,
      purpose: pendingAuth.value.mode
    })
    return response.message
  }

  function completeLogin(response) {
    token.value = response.token
    user.value = { id: response.id, name: response.name, email: response.email, phoneNumber: response.phoneNumber }
    localStorage.setItem('token', response.token)
    localStorage.setItem('user', JSON.stringify(user.value))
    router.push('/dashboard')
  }

  function handleOtpTransition(mode, data, error) {
    const message = error.response?.data?.error || ''
    if (message.includes('OTP_REQUIRED')) {
      pendingAuth.value = { mode, email: data.email, payload: { ...data, otp: '' } }
      sessionStorage.setItem('pendingAuth', JSON.stringify(pendingAuth.value))
      router.push('/otp')
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }

  return { user, token, loading, isAuthenticated, pendingAuth, initAuth, login, register, verifyPendingAuth, resendOtp, logout }
})
