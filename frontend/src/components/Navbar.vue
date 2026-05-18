<template>
  <nav class="navbar">
    <div class="navbar-brand">
      <span class="logo-icon">💰</span>
      <span class="brand-name">{{ $t('appName') }}</span>
    </div>

    <div class="navbar-links">
      <RouterLink to="/dashboard" class="nav-link">
        <span>🏠</span> {{ $t('nav.dashboard') }}
      </RouterLink>
      <RouterLink to="/bills" class="nav-link">
        <span>📋</span> {{ $t('nav.bills') }}
      </RouterLink>
      <RouterLink to="/payments" class="nav-link">
        <span>💳</span> {{ $t('nav.payments') }}
      </RouterLink>
    </div>

    <div class="navbar-right">
      <!-- Language Switcher -->
      <div class="lang-switcher">
        <button
          class="lang-btn"
          :class="{ active: locale === 'id' }"
          @click="switchLocale('id')"
        >ID</button>
        <button
          class="lang-btn"
          :class="{ active: locale === 'en' }"
          @click="switchLocale('en')"
        >EN</button>
      </div>

      <!-- Dark Mode Toggle -->
      <button class="theme-toggle" @click="toggleTheme" :title="isDark ? $t('lightMode') : $t('darkMode')">
        <span v-if="isDark">☀️</span>
        <span v-else>🌙</span>
      </button>

      <!-- Notifications -->
      <button class="notif-btn" @click="toggleNotifications">
        🔔
        <span v-if="unreadCount > 0" class="badge-notif">{{ unreadCount }}</span>
      </button>

      <div class="user-menu">
        <span class="user-name">{{ authStore.user?.name }}</span>
        <button class="btn btn-outline btn-sm" @click="authStore.logout()">{{ $t('logout') }}</button>
      </div>
    </div>

    <!-- Notification Dropdown -->
    <div v-if="showNotifications" class="notif-dropdown">
      <div class="notif-header">
        <span>{{ $t('nav.notifications') }}</span>
        <button v-if="unreadCount > 0" @click="markAllRead" class="mark-read-btn">{{ $t('nav.markAllRead') }}</button>
      </div>
      <div v-if="notifications.length === 0" class="notif-empty">{{ $t('nav.noNotifications') }}</div>
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
import { RouterLink, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/authStore.js'
import { useTheme } from '@/composables/useTheme.js'
import notificationService from '@/services/notificationService.js'

const { locale } = useI18n()
const { isDark, toggleTheme } = useTheme()
const authStore = useAuthStore()
const router = useRouter()
const notifications = ref([])
const unreadCount = ref(0)
const showNotifications = ref(false)

onMounted(loadNotifications)

function switchLocale(lang) {
  locale.value = lang
  localStorage.setItem('locale', lang)
}

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
  notifications.value = notifications.value.filter(n => n.isRead)
  unreadCount.value = 0
}

async function handleNotifClick(notif) {
  if (!notif.isRead) {
    await notificationService.markRead(notif.id)
    notifications.value = notifications.value.filter(n => n.id !== notif.id)
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  showNotifications.value = false
  router.push('/bills')
}

function formatDate(dt) {
  if (!dt) return ''
  const lang = locale.value === 'en' ? 'en-US' : 'id-ID'
  return new Date(dt).toLocaleDateString(lang, { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: var(--bg-nav);
  box-shadow: var(--shadow);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 24px;
  z-index: 100;
  transition: background-color 0.3s ease, border-color 0.3s ease;
}
.navbar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  margin-right: 8px;
}
.logo-icon { font-size: 24px; }
.brand-name { font-size: 18px; font-weight: 700; color: var(--primary); }
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
  color: var(--text-secondary);
  transition: all 0.2s;
}
.nav-link:hover { background: var(--bg-hover); color: var(--primary); }
.nav-link.router-link-active { background: var(--primary-light); color: var(--primary); }
.navbar-right { display: flex; align-items: center; gap: 12px; margin-left: auto; }

/* Language Switcher */
.lang-switcher {
  display: flex;
  border: 1px solid var(--border);
  border-radius: 8px;
  overflow: hidden;
}
.lang-btn {
  padding: 5px 10px;
  border: none;
  background: transparent;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
}
.lang-btn.active {
  background: var(--primary);
  color: white;
}
.lang-btn:not(.active):hover {
  background: var(--bg-hover);
}

/* Theme Toggle */
.theme-toggle {
  background: none;
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}
.theme-toggle:hover {
  background: var(--bg-hover);
  border-color: var(--primary);
}

/* Notifications */
.notif-btn {
  position: relative;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 10px;
  font-size: 20px;
  cursor: pointer;
  padding: 6px 10px;
  transition: all 0.2s;
}
.notif-btn:hover { background: var(--bg-hover); border-color: var(--primary); }
.badge-notif {
  position: absolute;
  top: -2px;
  right: -4px;
  background: var(--danger);
  color: white;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 5px;
  min-width: 16px;
  text-align: center;
}
.user-menu { display: flex; align-items: center; gap: 12px; }
.user-name { font-size: 14px; font-weight: 500; color: var(--text-primary); }

/* Notification Dropdown */
.notif-dropdown {
  position: fixed;
  top: 68px;
  right: 24px;
  width: 360px;
  max-height: 480px;
  overflow-y: auto;
  background: var(--bg-dropdown);
  border-radius: 12px;
  box-shadow: var(--shadow-lg);
  z-index: 200;
  border: 1px solid var(--border);
}
.notif-backdrop {
  position: fixed;
  inset: 0;
  z-index: 150;
}
.notif-header {
  padding: 14px 16px;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  background: var(--bg-dropdown);
}
.mark-read-btn {
  background: none;
  border: none;
  font-size: 12px;
  color: var(--primary);
  cursor: pointer;
}
.mark-read-btn:hover { text-decoration: underline; }
.notif-item {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
  transition: background 0.15s;
}
.notif-item:hover { background: var(--bg-hover); }
.notif-item.unread { background: var(--primary-light); }
.notif-msg { font-size: 13px; color: var(--text-primary); line-height: 1.4; }
.notif-time { font-size: 11px; color: var(--text-muted); margin-top: 4px; }
.notif-empty { padding: 24px; text-align: center; color: var(--text-muted); font-size: 14px; }

@media (max-width: 1024px) {
  .navbar { padding: 0 16px; gap: 12px; }
  .brand-name { font-size: 16px; }
  .nav-link { padding: 8px 10px; font-size: 13px; }
  .user-name { display: none; }
}

@media (max-width: 768px) {
  .navbar {
    height: auto;
    min-height: 64px;
    padding: 10px 12px;
    flex-wrap: wrap;
    row-gap: 10px;
  }
  .navbar-brand { margin-right: auto; }
  .navbar-links {
    order: 3;
    flex: 1 1 100%;
    overflow-x: auto;
    white-space: nowrap;
    padding-bottom: 2px;
    -ms-overflow-style: none;
    scrollbar-width: none;
  }
  .navbar-links::-webkit-scrollbar { display: none; }
  .nav-link { flex-shrink: 0; }
  .navbar-right { gap: 8px; }
  .user-menu { gap: 8px; }
  .notif-dropdown {
    left: 12px;
    right: 12px;
    width: auto;
    top: 126px;
    max-height: min(65vh, 480px);
  }
}

@media (max-width: 480px) {
  .brand-name { font-size: 15px; }
  .nav-link { font-size: 12px; padding: 7px 9px; }
  .lang-btn { font-size: 11px; padding: 4px 8px; }
}
</style>
