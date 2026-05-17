import api from './api.js'

const authService = {
  async register(data) {
    const response = await api.post('/auth/register', data)
    return response.data
  },

  async login(credentials) {
    const response = await api.post('/auth/login', credentials)
    return response.data
  },

  async resendOtp(data) {
    const response = await api.post('/auth/resend-otp', data)
    return response.data
  },

  async getMe() {
    const response = await api.get('/auth/me')
    return response.data
  }
}

export default authService
