<template>
  <div class="otp-page">
    <div class="otp-card">
      <!-- Header -->
      <div class="otp-header">
        <div class="otp-icon">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" fill="url(#gradient)" />
            <path d="M24 14L14 20V28L24 34L34 28V20L24 14Z" stroke="white" stroke-width="2" fill="none"/>
            <path d="M24 22V26M24 30H24.01" stroke="white" stroke-width="2" stroke-linecap="round"/>
            <defs>
              <linearGradient id="gradient" x1="0" y1="0" x2="48" y2="48">
                <stop stop-color="#667eea"/>
                <stop offset="1" stop-color="#764ba2"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1 class="otp-title">Verifikasi OTP</h1>
        <p class="otp-subtitle">
          Masukkan kode 6 digit yang telah dikirim ke
        </p>
        <p class="otp-email">{{ maskedEmail }}</p>
      </div>

      <!-- Alerts -->
      <transition name="fade">
        <div v-if="error" class="alert alert-danger">
          <span class="alert-icon">⚠️</span>
          <span>{{ error }}</span>
        </div>
      </transition>
      <transition name="fade">
        <div v-if="success" class="alert alert-success">
          <span class="alert-icon">✅</span>
          <span>{{ success }}</span>
        </div>
      </transition>

      <!-- OTP Input -->
      <form @submit.prevent="handleVerify" class="otp-form">
        <div class="otp-inputs">
          <input
            v-for="(digit, index) in otpDigits"
            :key="index"
            :ref="el => { if (el) inputRefs[index] = el }"
            type="text"
            inputmode="numeric"
            maxlength="1"
            class="otp-input"
            :class="{ 'filled': otpDigits[index], 'error': hasError }"
            :value="otpDigits[index]"
            @input="handleInput($event, index)"
            @keydown="handleKeydown($event, index)"
            @paste="handlePaste($event)"
            @focus="handleFocus(index)"
            autocomplete="one-time-code"
          />
        </div>

        <!-- Timer -->
        <div class="otp-timer">
          <div class="timer-bar">
            <div class="timer-progress" :style="{ width: timerPercentage + '%' }"></div>
          </div>
          <p class="timer-text" v-if="countdown > 0">
            Kode berlaku <strong>{{ formattedCountdown }}</strong>
          </p>
          <p class="timer-text expired" v-else>
            Kode telah kedaluwarsa
          </p>
        </div>

        <!-- Submit Button -->
        <button
          type="submit"
          class="btn-verify"
          :disabled="!isOtpComplete || authStore.loading"
        >
          <span v-if="authStore.loading" class="btn-loading">
            <span class="spinner"></span>
            Memverifikasi...
          </span>
          <span v-else>Verifikasi Kode</span>
        </button>
      </form>

      <!-- Resend Section -->
      <div class="otp-resend">
        <p class="resend-text">Tidak menerima kode?</p>
        <button
          class="btn-resend"
          @click="handleResend"
          :disabled="resendCooldown > 0 || resending"
        >
          <span v-if="resending">Mengirim ulang...</span>
          <span v-else-if="resendCooldown > 0">Kirim ulang dalam {{ resendCooldown }}s</span>
          <span v-else>Kirim Ulang Kode</span>
        </button>
      </div>

      <!-- Back Link -->
      <div class="otp-footer">
        <button class="btn-back" @click="goBack">
          ← Kembali ke {{ pendingMode === 'login' ? 'Login' : 'Register' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore.js'

const router = useRouter()
const authStore = useAuthStore()

// OTP input state
const otpDigits = ref(['', '', '', '', '', ''])
const inputRefs = ref([])
const hasError = ref(false)

// UI state
const error = ref('')
const success = ref('')
const resending = ref(false)

// Timer state
const OTP_VALIDITY_SECONDS = 300 // 5 minutes
const countdown = ref(OTP_VALIDITY_SECONDS)
const resendCooldown = ref(60) // initial cooldown since OTP was just sent
let timerInterval = null
let resendInterval = null

// Computed
const pendingEmail = computed(() => authStore.pendingAuth?.email || '')
const pendingMode = computed(() => authStore.pendingAuth?.mode || 'login')

const maskedEmail = computed(() => {
  const email = pendingEmail.value
  if (!email) return '***'
  const [name, domain] = email.split('@')
  if (!domain) return email
  const masked = name.length > 3
    ? name.slice(0, 2) + '***' + name.slice(-1)
    : name.slice(0, 1) + '***'
  return masked + '@' + domain
})

const isOtpComplete = computed(() => otpDigits.value.every(d => d !== ''))

const timerPercentage = computed(() => (countdown.value / OTP_VALIDITY_SECONDS) * 100)

const formattedCountdown = computed(() => {
  const min = Math.floor(countdown.value / 60)
  const sec = countdown.value % 60
  return `${min}:${sec.toString().padStart(2, '0')}`
})

// Input handlers
function handleInput(event, index) {
  const value = event.target.value.replace(/\D/g, '')
  otpDigits.value[index] = value.slice(-1)
  hasError.value = false
  error.value = ''

  if (value && index < 5) {
    nextTick(() => inputRefs.value[index + 1]?.focus())
  }
}

function handleKeydown(event, index) {
  if (event.key === 'Backspace') {
    if (!otpDigits.value[index] && index > 0) {
      otpDigits.value[index - 1] = ''
      nextTick(() => inputRefs.value[index - 1]?.focus())
    } else {
      otpDigits.value[index] = ''
    }
  } else if (event.key === 'ArrowLeft' && index > 0) {
    inputRefs.value[index - 1]?.focus()
  } else if (event.key === 'ArrowRight' && index < 5) {
    inputRefs.value[index + 1]?.focus()
  }
}

function handlePaste(event) {
  event.preventDefault()
  const pasted = event.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6)
  if (pasted.length > 0) {
    for (let i = 0; i < 6; i++) {
      otpDigits.value[i] = pasted[i] || ''
    }
    const focusIndex = Math.min(pasted.length, 5)
    nextTick(() => inputRefs.value[focusIndex]?.focus())
  }
}

function handleFocus(index) {
  inputRefs.value[index]?.select()
}

// Actions
async function handleVerify() {
  if (!isOtpComplete.value) return
  error.value = ''
  success.value = ''
  hasError.value = false

  try {
    const otp = otpDigits.value.join('')
    await authStore.verifyPendingAuth(otp)
  } catch (e) {
    hasError.value = true
    error.value = e.response?.data?.error || 'Verifikasi OTP gagal. Periksa kode Anda.'
    // Shake animation handled by CSS
    otpDigits.value = ['', '', '', '', '', '']
    nextTick(() => inputRefs.value[0]?.focus())
  }
}

async function handleResend() {
  if (resendCooldown.value > 0) return
  error.value = ''
  success.value = ''
  resending.value = true

  try {
    const message = await authStore.resendOtp()
    success.value = message || 'Kode OTP baru telah dikirim!'
    // Reset timers
    countdown.value = OTP_VALIDITY_SECONDS
    resendCooldown.value = 60
    startResendCooldown()
    // Clear inputs
    otpDigits.value = ['', '', '', '', '', '']
    nextTick(() => inputRefs.value[0]?.focus())
  } catch (e) {
    error.value = e.response?.data?.error || 'Gagal kirim ulang OTP'
  } finally {
    resending.value = false
  }
}

function goBack() {
  const mode = pendingMode.value
  sessionStorage.removeItem('pendingAuth')
  router.push(mode === 'login' ? '/login' : '/register')
}

// Timer management
function startCountdown() {
  timerInterval = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      clearInterval(timerInterval)
    }
  }, 1000)
}

function startResendCooldown() {
  if (resendInterval) clearInterval(resendInterval)
  resendInterval = setInterval(() => {
    if (resendCooldown.value > 0) {
      resendCooldown.value--
    } else {
      clearInterval(resendInterval)
    }
  }, 1000)
}

// Lifecycle
onMounted(() => {
  if (!authStore.pendingAuth) {
    router.push('/login')
    return
  }
  startCountdown()
  startResendCooldown()
  nextTick(() => inputRefs.value[0]?.focus())
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
  if (resendInterval) clearInterval(resendInterval)
})
</script>

<style scoped>
.otp-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.otp-card {
  background: var(--bg-card, #ffffff);
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 20px;
  padding: 40px 36px;
  width: 100%;
  max-width: 440px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.4s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Header */
.otp-header {
  text-align: center;
  margin-bottom: 28px;
}

.otp-icon {
  margin-bottom: 16px;
  display: inline-block;
}

.otp-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary, #1a202c);
  margin: 0 0 8px;
}

.otp-subtitle {
  font-size: 14px;
  color: var(--text-secondary, #718096);
  margin: 0;
  line-height: 1.5;
}

.otp-email {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary, #667eea);
  margin: 4px 0 0;
}

/* Alerts */
.alert {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

.alert-danger {
  background: #fff5f5;
  border: 1px solid #fed7d7;
  color: #c53030;
}

.alert-success {
  background: #f0fff4;
  border: 1px solid #c6f6d5;
  color: #276749;
}

.alert-icon {
  font-size: 16px;
  flex-shrink: 0;
}

/* OTP Inputs */
.otp-form {
  margin-bottom: 24px;
}

.otp-inputs {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-bottom: 20px;
}

.otp-input {
  width: 50px;
  height: 56px;
  border: 2px solid var(--border, #e2e8f0);
  border-radius: 12px;
  text-align: center;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary, #1a202c);
  background: var(--bg-card, #ffffff);
  transition: all 0.2s ease;
  outline: none;
  caret-color: var(--primary, #667eea);
}

.otp-input:focus {
  border-color: var(--primary, #667eea);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.otp-input.filled {
  border-color: var(--primary, #667eea);
  background: rgba(102, 126, 234, 0.04);
}

.otp-input.error {
  border-color: #e53e3e;
  animation: shake 0.4s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-4px); }
  40% { transform: translateX(4px); }
  60% { transform: translateX(-4px); }
  80% { transform: translateX(4px); }
}

/* Timer */
.otp-timer {
  margin-bottom: 20px;
}

.timer-bar {
  height: 4px;
  background: var(--border, #e2e8f0);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 8px;
}

.timer-progress {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 2px;
  transition: width 1s linear;
}

.timer-text {
  font-size: 13px;
  color: var(--text-secondary, #718096);
  text-align: center;
  margin: 0;
}

.timer-text.expired {
  color: #e53e3e;
  font-weight: 500;
}

/* Verify Button */
.btn-verify {
  width: 100%;
  padding: 14px 24px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-verify:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.btn-verify:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn-loading {
  display: flex;
  align-items: center;
  gap: 8px;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Resend Section */
.otp-resend {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid var(--border, #e2e8f0);
}

.resend-text {
  font-size: 13px;
  color: var(--text-secondary, #718096);
  margin: 0 0 8px;
}

.btn-resend {
  background: none;
  border: none;
  color: var(--primary, #667eea);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.btn-resend:hover:not(:disabled) {
  background: rgba(102, 126, 234, 0.08);
}

.btn-resend:disabled {
  color: var(--text-secondary, #718096);
  cursor: not-allowed;
}

/* Footer */
.otp-footer {
  text-align: center;
  margin-top: 16px;
}

.btn-back {
  background: none;
  border: none;
  color: var(--text-secondary, #718096);
  font-size: 13px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: color 0.2s ease;
}

.btn-back:hover {
  color: var(--text-primary, #1a202c);
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Responsive */
@media (max-width: 480px) {
  .otp-card {
    padding: 32px 24px;
  }

  .otp-inputs {
    gap: 8px;
  }

  .otp-input {
    width: 44px;
    height: 50px;
    font-size: 20px;
  }
}
</style>
