<template>
  <nav class="navbar">
    <div class="navbar-brand">
      <span class="logo-icon">💰</span>
      <span class="brand-name">Bill Reminder</span>
    </div>

    <div class="navbar-links">
      <RouterLink to="/dashboard" class="nav-link">
        <span>🏠</span> Dashboard
      </RouterLink>
      <RouterLink to="/bills" class="nav-link">
        <span>📋</span> Tagihan
      </RouterLink>
      <RouterLink to="/payments" class="nav-link">
        <span>💳</span> Riwayat Bayar
      </RouterLink>
    </div>

    <div class="navbar-right">
      <button class="notif-btn" @click="toggleNotifications">
        🔔
        <span v-if="unreadCount > 0" class="badge-notif">{{ unreadCount }}</span>
      </button>

      <div class="user-menu">
        <span class="user-name">{{ authStore.user?.name }}</span>
        <button class="btn btn-outline btn-sm" @click="authStore.logout()">Logout</button>
      </div>
    </div>

    <!-- Notification Dropdown -->
    <div v-if="showNotifications" class="notif-dropdown">
      <div class="notif-header">
        <span>Notifikasi</span>
        <button v-if="unreadCount > 0" @click="markAllRead" class="mark-read-btn">Tandai semua dibaca</button>
      </div>
      <div v-if="notifications.length === 0" class="notif-empty">Tidak ada notifikasi</div>
      <div v-for="notif in notifications.slice(0, 10)" :key="notif.id"
           class="notif-item" :class="{ unread: !notif.isRead }"
           @click="handleNotifClick(notif)">
        <div class="notif-msg">{{ notif.message }}</div>
        <div class="notif-time">{{ formatDate(notif.createdAt) }}</div>
      </div>
    </div>
    <div v-if="showNotifications" class="notif-backdrop" @click="showNotifications = false" />
  </nav>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/authStore.js'
import notificationService from '@/services/notificationService.js'

const authStore = useAuthStore()
const notifications = ref([])
const unreadCount = ref(0)
const showNotifications = ref(false)

onMounted(loadNotifications)

async function loadNotifications() {
  try {
    notifications.value = await notificationService.getAll()
    unreadCount.value = notifications.value.filter(n => !n.isRead).length
  } catch (e) { /* silent */ }
}

function toggleNotifications() {
  showNotifications.value = !showNotifications.value
}

async function markAllRead() {
  await notificationService.markAllRead()
  notifications.value.forEach(n => n.isRead = true)
  unreadCount.value = 0
}

async function handleNotifClick(notif) {
  if (!notif.isRead) {
    await notificationService.markRead(notif.id)
    notif.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

function formatDate(dt) {
  if (!dt) return ''
  return new Date(dt).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 24px;
  z-index: 100;
}
.navbar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  margin-right: 8px;
}
.logo-icon { font-size: 24px; }
.brand-name { font-size: 18px; font-weight: 700; color: #4f46e5; }
.navbar-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}
.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  transition: all 0.2s;
}
.nav-link:hover { background: #f0f2f5; color: #4f46e5; }
.nav-link.router-link-active { background: #e0e7ff; color: #4f46e5; }
.navbar-right { display: flex; align-items: center; gap: 16px; margin-left: auto; }
.notif-btn {
  position: relative;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
}
.badge-notif {
  position: absolute;
  top: -2px;
  right: -4px;
  background: #ef4444;
  color: white;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 5px;
  min-width: 16px;
  text-align: center;
}
.user-menu { display: flex; align-items: center; gap: 12px; }
.user-name { font-size: 14px; font-weight: 500; color: #374151; }

/* Notification Dropdown */
.notif-dropdown {
  position: fixed;
  top: 68px;
  right: 24px;
  width: 360px;
  max-height: 480px;
  overflow-y: auto;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  z-index: 200;
  border: 1px solid #e5e7eb;
}
.notif-backdrop {
  position: fixed;
  inset: 0;
  z-index: 150;
}
.notif-header {
  padding: 14px 16px;
  font-weight: 600;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  background: white;
}
.mark-read-btn {
  background: none;
  border: none;
  font-size: 12px;
  color: #4f46e5;
  cursor: pointer;
}
.notif-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background 0.15s;
}
.notif-item:hover { background: #f9fafb; }
.notif-item.unread { background: #eef2ff; }
.notif-msg { font-size: 13px; color: #374151; line-height: 1.4; }
.notif-time { font-size: 11px; color: #9ca3af; margin-top: 4px; }
.notif-empty { padding: 24px; text-align: center; color: #9ca3af; font-size: 14px; }
</style>
