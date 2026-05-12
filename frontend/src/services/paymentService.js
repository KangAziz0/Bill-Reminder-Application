import api from './api.js'

const paymentService = {
  async getAll() {
    const response = await api.get('/payments')
    return response.data
  },

  async getById(id) {
    const response = await api.get(`/payments/${id}`)
    return response.data
  },

  async create(data) {
    const response = await api.post('/payments', data)
    return response.data
  }
}

export default paymentService
