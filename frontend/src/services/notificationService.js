import api from './api.js'

const notificationService = {
  async getAll() {
    const response = await api.get('/notifications')
    return response.data
  },

  async getUnread() {
    const response = await api.get('/notifications/unread')
    return response.data
  },

  async markAllRead() {
    await api.patch('/notifications/mark-all-read')
  },

  async markRead(id) {
    await api.patch(`/notifications/${id}/read`)
  }
}

export default notificationService
