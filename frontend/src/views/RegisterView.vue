<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-logo">💰</div>
      <h1 class="auth-title">Daftar Akun</h1>
      <p class="auth-subtitle">Buat akun Bill Reminder Anda</p>

      <div v-if="error" class="alert alert-danger">{{ error }}</div>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label class="form-label">Nama Lengkap</label>
          <input v-model="form.name" type="text" class="form-control" placeholder="John Doe" required />
        </div>
        <div class="form-group">
          <label class="form-label">Email</label>
          <input v-model="form.email" type="email" class="form-control" placeholder="email@contoh.com" required />
        </div>
        <div class="form-group">
          <label class="form-label">Password</label>
          <input v-model="form.password" type="password" class="form-control" placeholder="Min. 6 karakter" required minlength="6" />
        </div>
        <button type="submit" class="btn btn-primary w-full" :disabled="authStore.loading">
          <span v-if="authStore.loading">⏳ Mendaftar...</span>
          <span v-else>Daftar</span>
        </button>
      </form>

      <p class="auth-footer">
        Sudah punya akun? <RouterLink to="/login">Login</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/authStore.js'

const authStore = useAuthStore()
const error = ref('')
const form = ref({ name: '', email: '', password: '' })

async function handleRegister() {
  error.value = ''
  try {
    await authStore.register(form.value)
  } catch (e) {
    error.value = e.response?.data?.error || 'Pendaftaran gagal. Coba lagi.'
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
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.15);
}
.auth-logo { font-size: 48px; text-align: center; margin-bottom: 8px; }
.auth-title { font-size: 26px; font-weight: 700; text-align: center; color: #1f2937; }
.auth-subtitle { text-align: center; color: #6b7280; font-size: 14px; margin-bottom: 28px; }
.w-full { width: 100%; justify-content: center; }
.auth-footer { text-align: center; margin-top: 20px; font-size: 14px; color: #6b7280; }
.auth-footer a { color: #4f46e5; text-decoration: none; font-weight: 500; }
</style>
