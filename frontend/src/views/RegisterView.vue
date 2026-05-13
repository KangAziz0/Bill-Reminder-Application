<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-logo">💰</div>
      <h1 class="auth-title">{{ $t('auth.registerTitle') }}</h1>
      <p class="auth-subtitle">{{ $t('auth.registerSubtitle') }}</p>

      <div v-if="error" class="alert alert-danger">{{ error }}</div>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label class="form-label">{{ $t('auth.fullName') }}</label>
          <input v-model="form.name" type="text" class="form-control" :placeholder="$t('auth.namePlaceholder')" required />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('auth.email') }}</label>
          <input v-model="form.email" type="email" class="form-control" :placeholder="$t('auth.emailPlaceholder')" required />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('auth.password') }}</label>
          <input v-model="form.password" type="password" class="form-control" :placeholder="$t('auth.passwordPlaceholder')" required minlength="6" />
        </div>
        <div class="form-group">
          <label class="form-label">{{ $t('auth.phoneNumber') || 'Nomor WhatsApp' }}</label>
          <input v-model="form.phoneNumber" type="tel" class="form-control" :placeholder="$t('auth.phonePlaceholder') || 'Contoh: 08123456789'" />
          <small class="form-hint">{{ $t('auth.phoneHint') || 'Opsional, untuk menerima notifikasi via WhatsApp' }}</small>
        </div>
        <button type="submit" class="btn btn-primary w-full" :disabled="authStore.loading">
          <span v-if="authStore.loading">⏳ {{ $t('auth.registering') }}</span>
          <span v-else>{{ $t('auth.register') }}</span>
        </button>
      </form>

      <p class="auth-footer">
        {{ $t('auth.hasAccount') }} <RouterLink to="/login">{{ $t('auth.loginNow') }}</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/authStore.js'

const { t } = useI18n()
const authStore = useAuthStore()
const error = ref('')
const form = ref({ name: '', email: '', password: '', phoneNumber: '' })

async function handleRegister() {
  error.value = ''
  try {
    await authStore.register(form.value)
  } catch (e) {
    error.value = e.response?.data?.error || t('auth.registerFailed')
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}
.auth-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}
.auth-logo { font-size: 48px; text-align: center; margin-bottom: 8px; }
.auth-title { font-size: 26px; font-weight: 700; text-align: center; color: var(--text-primary); }
.auth-subtitle { text-align: center; color: var(--text-secondary); font-size: 14px; margin-bottom: 28px; }
.w-full { width: 100%; justify-content: center; }
.auth-footer { text-align: center; margin-top: 20px; font-size: 14px; color: var(--text-secondary); }
.auth-footer a { color: var(--primary); text-decoration: none; font-weight: 500; }
.form-hint { display: block; margin-top: 4px; font-size: 12px; color: var(--text-secondary); }
</style>
