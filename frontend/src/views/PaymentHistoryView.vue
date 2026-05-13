<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">💳 {{ $t('payment.title') }}</h1>
    </div>

    <div v-if="loading" class="loading-spinner"><div class="spinner"></div></div>

    <template v-else>
      <div v-if="payments.length === 0" class="empty-state card">
        <div class="icon">💸</div>
        <h3>{{ $t('payment.noPayments') }}</h3>
        <p>{{ $t('payment.noPaymentsDesc') }}</p>
        <RouterLink to="/bills" class="btn btn-primary" style="margin-top: 16px;">{{ $t('payment.viewBills') }}</RouterLink>
      </div>

      <div v-else class="card">
        <!-- Summary -->
        <div class="payment-summary">
          <div class="summary-item">
            <span class="summary-label">{{ $t('payment.totalThisMonth') }}</span>
            <span class="summary-value">Rp {{ formatAmount(currentMonthTotal) }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">{{ $t('payment.totalTransactions') }}</span>
            <span class="summary-value">{{ payments.length }}</span>
          </div>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>{{ $t('payment.thBill') }}</th>
                <th>{{ $t('payment.thAmount') }}</th>
                <th>{{ $t('payment.thDate') }}</th>
                <th>{{ $t('payment.thMethod') }}</th>
                <th>{{ $t('payment.thNotes') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="payment in payments" :key="payment.id">
                <td>
                  <div class="bill-name">{{ payment.billTitle }}</div>
                </td>
                <td class="amount-cell">Rp {{ formatAmount(payment.paidAmount) }}</td>
                <td>{{ formatDate(payment.paidDate) }}</td>
                <td>
                  <span v-if="payment.paymentMethod" class="method-badge">{{ payment.paymentMethod }}</span>
                  <span v-else class="text-muted">-</span>
                </td>
                <td class="notes-cell">{{ payment.notes || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useI18n } from 'vue-i18n'
import paymentService from '@/services/paymentService.js'

const { locale } = useI18n()
const payments = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    payments.value = await paymentService.getAll()
  } finally {
    loading.value = false
  }
})

const currentMonthTotal = computed(() => {
  const now = new Date()
  return payments.value
    .filter(p => {
      const d = new Date(p.paidDate)
      return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
    })
    .reduce((sum, p) => sum + Number(p.paidAmount), 0)
})

function formatAmount(val) {
  return Number(val || 0).toLocaleString('id-ID')
}

function formatDate(date) {
  if (!date) return '-'
  const lang = locale.value === 'en' ? 'en-US' : 'id-ID'
  return new Date(date).toLocaleDateString(lang, { day: 'numeric', month: 'long', year: 'numeric' })
}
</script>

<style scoped>
.payment-summary {
  display: flex;
  gap: 32px;
  padding: 16px 0;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--border);
  flex-wrap: wrap;
}
.summary-item { display: flex; flex-direction: column; gap: 4px; }
.summary-label { font-size: 13px; color: var(--text-secondary); }
.summary-value { font-size: 22px; font-weight: 700; color: var(--primary); }
.bill-name { font-weight: 500; color: var(--text-primary); }
.amount-cell { font-weight: 600; color: var(--success); }
.method-badge {
  background: var(--primary-light);
  color: var(--primary);
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}
.notes-cell { max-width: 200px; color: var(--text-secondary); font-size: 13px; }
</style>
