<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">Dashboard</h1>
      <RouterLink to="/bills/new" class="btn btn-primary">+ Tambah Tagihan</RouterLink>
    </div>

    <div v-if="loading" class="loading-spinner"><div class="spinner"></div></div>

    <template v-else>
      <!-- Summary Cards -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">💰 Total Tagihan Bulan Ini</div>
          <div class="stat-value primary">Rp {{ formatAmount(summary.totalBillsThisMonth) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">✅ Sudah Dibayar</div>
          <div class="stat-value success">Rp {{ formatAmount(summary.totalPaid) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">⏳ Belum Dibayar</div>
          <div class="stat-value warning">Rp {{ formatAmount(summary.totalUnpaid) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">🚨 Terlambat</div>
          <div class="stat-value danger">{{ summary.overdueCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">⚠️ Mendekati Jatuh Tempo</div>
          <div class="stat-value warning-text">{{ summary.dueSoonCount }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">📅 Upcoming</div>
          <div class="stat-value info">{{ summary.upcomingCount }}</div>
        </div>
      </div>

      <!-- Upcoming Bills -->
      <div class="card">
        <div class="section-header">
          <h2 class="section-title">📅 Tagihan Mendekati Jatuh Tempo</h2>
          <RouterLink to="/bills" class="btn btn-outline btn-sm">Lihat Semua</RouterLink>
        </div>

        <div v-if="upcomingBills.length === 0" class="empty-state">
          <div class="icon">🎉</div>
          <h3>Tidak ada tagihan mendekati jatuh tempo</h3>
          <p>Semua tagihan Anda aman!</p>
        </div>

        <div v-else class="bills-grid">
          <BillCard
            v-for="bill in upcomingBills"
            :key="bill.id"
            :bill="bill"
            @markPaid="quickMarkPaid"
            @edit="goToEdit"
            @delete="() => {}"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import BillCard from '@/components/BillCard.vue'
import dashboardService from '@/services/dashboardService.js'
import billService from '@/services/billService.js'

const router = useRouter()
const loading = ref(true)
const summary = ref({
  totalBillsThisMonth: 0,
  totalPaid: 0,
  totalUnpaid: 0,
  overdueCount: 0,
  dueSoonCount: 0,
  upcomingCount: 0,
  paidCount: 0
})
const upcomingBills = ref([])

onMounted(async () => {
  try {
    const [s, ub] = await Promise.all([
      dashboardService.getSummary(),
      dashboardService.getUpcomingBills()
    ])
    summary.value = s
    upcomingBills.value = ub
  } finally {
    loading.value = false
  }
})

async function quickMarkPaid(bill) {
  await billService.markAsPaid(bill.id)
  upcomingBills.value = upcomingBills.value.filter(b => b.id !== bill.id)
  const [s] = await Promise.all([dashboardService.getSummary()])
  summary.value = s
}

function goToEdit(bill) {
  router.push(`/bills/${bill.id}/edit`)
}

function formatAmount(val) {
  return Number(val || 0).toLocaleString('id-ID')
}
</script>

<style scoped>
.stat-value { font-size: 24px; font-weight: 700; }
.stat-value.primary { color: #4f46e5; }
.stat-value.success { color: #10b981; }
.stat-value.warning { color: #f59e0b; }
.stat-value.warning-text { color: #f59e0b; }
.stat-value.danger { color: #ef4444; }
.stat-value.info { color: #3b82f6; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-title { font-size: 18px; font-weight: 600; }
.bills-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; }
</style>
