<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="auth-title">Verifikasi OTP</h1>
      <p class="auth-subtitle">Kode OTP sudah dikirim ke {{ pendingEmail }}</p>

      <div v-if="error" class="alert alert-danger">{{ error }}</div>
      <div v-if="success" class="alert alert-success">{{ success }}</div>

      <form @submit.prevent="handleVerify">
        <div class="form-group">
          <label class="form-label">Kode OTP</label>
          <input v-model="otp" class="form-control" inputmode="numeric" required />
        </div>
        <button type="submit" class="btn btn-primary w-full" :disabled="authStore.loading">Verifikasi</button>
      </form>

      <button class="btn btn-secondary w-full mt" @click="handleResend" :disabled="resending || authStore.loading">
        {{ resending ? 'Mengirim ulang...' : 'Resend OTP' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/authStore.js'

const authStore = useAuthStore()
const otp = ref('')
const error = ref('')
const success = ref('')
const resending = ref(false)

const pendingEmail = computed(() => authStore.pendingAuth?.email || '-')

async function handleVerify() {
  error.value = ''
  success.value = ''
  try {
    await authStore.verifyPendingAuth(otp.value)
  } catch (e) {
    error.value = e.response?.data?.error || 'Verifikasi OTP gagal'
  }
}

async function handleResend() {
  error.value = ''
  success.value = ''
  resending.value = true
  try {
    const message = await authStore.resendOtp()
    success.value = message
  } catch (e) {
    error.value = e.response?.data?.error || 'Gagal kirim ulang OTP'
  } finally {
    resending.value = false
  }
}
</script>
