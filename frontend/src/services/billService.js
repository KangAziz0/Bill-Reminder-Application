import api from './api.js'

const billService = {
  async getAll(params = {}) {
    const response = await api.get('/bills', { params })
    return response.data
  },

  async getById(id) {
    const response = await api.get(`/bills/${id}`)
    return response.data
  },

  async create(data) {
    const response = await api.post('/bills', data)
    return response.data
  },

  async update(id, data) {
    const response = await api.put(`/bills/${id}`, data)
    return response.data
  },

  async delete(id) {
    await api.delete(`/bills/${id}`)
  },

  async markAsPaid(id) {
    const response = await api.patch(`/bills/${id}/mark-paid`)
    return response.data
  }
}

export default billService
