<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">💳 Riwayat Pembayaran</h1>
    </div>

    <div v-if="loading" class="loading-spinner"><div class="spinner"></div></div>

    <template v-else>
      <div v-if="payments.length === 0" class="empty-state card">
        <div class="icon">💸</div>
        <h3>Belum ada riwayat pembayaran</h3>
        <p>Tandai tagihan sebagai lunas untuk memulai!</p>
        <RouterLink to="/bills" class="btn btn-primary" style="margin-top: 16px;">Lihat Tagihan</RouterLink>
      </div>

      <div v-else class="card">
        <!-- Summary -->
        <div class="payment-summary">
          <div class="summary-item">
            <span class="summary-label">Total Pembayaran Bulan Ini</span>
            <span class="summary-value">Rp {{ formatAmount(currentMonthTotal) }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">Total Transaksi</span>
            <span class="summary-value">{{ payments.length }}</span>
          </div>
        </div>

        <div class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Tagihan</th>
                <th>Nominal</th>
                <th>Tanggal Bayar</th>
                <th>Metode</th>
                <th>Catatan</th>
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
                  <span v-else class="text-gray">-</span>
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
import paymentService from '@/services/paymentService.js'

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
  return new Date(date).toLocaleDateString('id-ID', { day: 'numeric', month: 'long', year: 'numeric' })
}
</script>

<style scoped>
.payment-summary {
  display: flex;
  gap: 32px;
  padding: 16px 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
  flex-wrap: wrap;
}
.summary-item { display: flex; flex-direction: column; gap: 4px; }
.summary-label { font-size: 13px; color: #6b7280; }
.summary-value { font-size: 22px; font-weight: 700; color: #4f46e5; }
.bill-name { font-weight: 500; }
.amount-cell { font-weight: 600; color: #10b981; }
.method-badge {
  background: #e0e7ff;
  color: #4f46e5;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
}
.notes-cell { max-width: 200px; color: #6b7280; font-size: 13px; }
.text-gray { color: #9ca3af; }
</style>
