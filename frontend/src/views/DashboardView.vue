<template>
  <div class="dashboard">
    <!-- Header -->
    <div class="dashboard-header">
      <div>
        <h1 class="dashboard-title">Dashboard</h1>
        <p class="dashboard-subtitle">Ringkasan keuangan & tagihan Anda</p>
      </div>
      <RouterLink to="/bills/new" class="btn-add">
        <span class="btn-icon">+</span> Tambah Tagihan
      </RouterLink>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Memuat data...</p>
    </div>

    <template v-else>
      <!-- Summary Cards -->
      <div class="summary-grid">
        <div class="summary-card card-total">
          <div class="card-icon">💰</div>
          <div class="card-content">
            <span class="card-label">Total Tagihan Bulan Ini</span>
            <span class="card-value">Rp {{ formatAmount(summary.totalBillsThisMonth) }}</span>
          </div>
        </div>
        <div class="summary-card card-paid">
          <div class="card-icon">✅</div>
          <div class="card-content">
            <span class="card-label">Sudah Dibayar</span>
            <span class="card-value">Rp {{ formatAmount(summary.totalPaid) }}</span>
          </div>
        </div>
        <div class="summary-card card-unpaid">
          <div class="card-icon">⏳</div>
          <div class="card-content">
            <span class="card-label">Belum Dibayar</span>
            <span class="card-value">Rp {{ formatAmount(summary.totalUnpaid) }}</span>
          </div>
        </div>
        <div class="summary-card card-overdue">
          <div class="card-icon">🚨</div>
          <div class="card-content">
            <span class="card-label">Terlambat</span>
            <span class="card-value">{{ summary.overdueCount }} tagihan</span>
          </div>
        </div>
      </div>

      <!-- Status Badges -->
      <div class="status-badges">
        <div class="badge badge-warning">
          <span class="badge-dot"></span>
          <span>{{ summary.dueSoonCount }} Mendekati Jatuh Tempo</span>
        </div>
        <div class="badge badge-info">
          <span class="badge-dot"></span>
          <span>{{ summary.upcomingCount }} Akan Datang</span>
        </div>
        <div class="badge badge-success">
          <span class="badge-dot"></span>
          <span>{{ summary.paidCount }} Lunas</span>
        </div>
      </div>

      <!-- Charts Section -->
      <div class="charts-section">
        <!-- Line Chart - Monthly Trends -->
        <div class="chart-card chart-wide">
          <div class="chart-header">
            <h3 class="chart-title">Tren Tagihan & Pembayaran (6 Bulan)</h3>
            <span class="chart-badge">Line Chart</span>
          </div>
          <div class="chart-body">
            <Line v-if="lineChartData" :data="lineChartData" :options="lineChartOptions" />
            <div v-else class="chart-empty">Belum ada data tren</div>
          </div>
        </div>

        <!-- Pie Chart - Category Breakdown -->
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">Distribusi Kategori</h3>
            <span class="chart-badge">Pie Chart</span>
          </div>
          <div class="chart-body chart-body-pie">
            <Pie v-if="pieChartData" :data="pieChartData" :options="pieChartOptions" />
            <div v-else class="chart-empty">Belum ada data kategori</div>
          </div>
        </div>

        <!-- Doughnut Chart - Status Distribution -->
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">Status Tagihan</h3>
            <span class="chart-badge">Doughnut</span>
          </div>
          <div class="chart-body chart-body-pie">
            <Doughnut v-if="doughnutChartData" :data="doughnutChartData" :options="doughnutChartOptions" />
            <div v-else class="chart-empty">Belum ada data status</div>
          </div>
        </div>

        <!-- Stacked Bar Chart - Monthly Comparison -->
        <div class="chart-card chart-wide">
          <div class="chart-header">
            <h3 class="chart-title">Perbandingan Tagihan vs Pembayaran</h3>
            <span class="chart-badge">Stacked Bar</span>
          </div>
          <div class="chart-body">
            <Bar v-if="barChartData" :data="barChartData" :options="barChartOptions" />
            <div v-else class="chart-empty">Belum ada data perbandingan</div>
          </div>
        </div>
      </div>

      <!-- Upcoming Bills -->
      <div class="upcoming-section">
        <div class="section-header">
          <h2 class="section-title">📅 Tagihan Mendekati Jatuh Tempo</h2>
          <RouterLink to="/bills" class="btn-link">Lihat Semua →</RouterLink>
        </div>

        <div v-if="upcomingBills.length === 0" class="empty-state">
          <div class="empty-icon">🎉</div>
          <h3>Tidak ada tagihan mendekati jatuh tempo</h3>
          <p>Semua tagihan Anda aman!</p>
        </div>

        <div v-else class="bills-list">
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
import { ref, onMounted, computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js'
import { Line, Pie, Bar, Doughnut } from 'vue-chartjs'
import BillCard from '@/components/BillCard.vue'
import dashboardService from '@/services/dashboardService.js'
import billService from '@/services/billService.js'

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
)

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
const chartData = ref(null)

// Color palette
const colors = {
  primary: '#6366f1',
  primaryLight: 'rgba(99, 102, 241, 0.1)',
  success: '#10b981',
  successLight: 'rgba(16, 185, 129, 0.1)',
  warning: '#f59e0b',
  warningLight: 'rgba(245, 158, 11, 0.1)',
  danger: '#ef4444',
  dangerLight: 'rgba(239, 68, 68, 0.1)',
  info: '#3b82f6',
  infoLight: 'rgba(59, 130, 246, 0.1)',
  purple: '#8b5cf6',
  pink: '#ec4899',
  teal: '#14b8a6',
  orange: '#f97316',
  cyan: '#06b6d4',
  lime: '#84cc16'
}

const categoryColors = [
  '#6366f1', '#10b981', '#f59e0b', '#ef4444',
  '#8b5cf6', '#ec4899', '#14b8a6', '#f97316',
  '#06b6d4', '#84cc16', '#3b82f6', '#d946ef'
]

const statusColorMap = {
  'PAID': '#10b981',
  'UPCOMING': '#3b82f6',
  'DUE_SOON': '#f59e0b',
  'OVERDUE': '#ef4444'
}

const statusLabelMap = {
  'PAID': 'Lunas',
  'UPCOMING': 'Akan Datang',
  'DUE_SOON': 'Mendekati Jatuh Tempo',
  'OVERDUE': 'Terlambat'
}

// Chart data computations
const lineChartData = computed(() => {
  if (!chartData.value || !chartData.value.monthlyTrends || chartData.value.monthlyTrends.length === 0) return null
  const trends = chartData.value.monthlyTrends
  return {
    labels: trends.map(t => t.month),
    datasets: [
      {
        label: 'Total Tagihan',
        data: trends.map(t => Number(t.totalBills)),
        borderColor: colors.primary,
        backgroundColor: colors.primaryLight,
        fill: true,
        tension: 0.4,
        pointBackgroundColor: colors.primary,
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        pointRadius: 5,
        pointHoverRadius: 7
      },
      {
        label: 'Total Dibayar',
        data: trends.map(t => Number(t.totalPaid)),
        borderColor: colors.success,
        backgroundColor: colors.successLight,
        fill: true,
        tension: 0.4,
        pointBackgroundColor: colors.success,
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        pointRadius: 5,
        pointHoverRadius: 7
      }
    ]
  }
})

const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  interaction: { intersect: false, mode: 'index' },
  plugins: {
    legend: {
      position: 'top',
      labels: { usePointStyle: true, padding: 20, font: { size: 12, weight: '500' } }
    },
    tooltip: {
      backgroundColor: 'rgba(17, 24, 39, 0.9)',
      titleFont: { size: 13 },
      bodyFont: { size: 12 },
      padding: 12,
      cornerRadius: 8,
      callbacks: {
        label: function(context) {
          return `${context.dataset.label}: Rp ${Number(context.raw).toLocaleString('id-ID')}`
        }
      }
    }
  },
  scales: {
    x: {
      grid: { display: false },
      ticks: { font: { size: 11 } }
    },
    y: {
      grid: { color: 'rgba(0,0,0,0.05)' },
      ticks: {
        font: { size: 11 },
        callback: function(value) {
          if (value >= 1000000) return 'Rp ' + (value / 1000000).toFixed(1) + 'jt'
          if (value >= 1000) return 'Rp ' + (value / 1000).toFixed(0) + 'rb'
          return 'Rp ' + value
        }
      }
    }
  }
}

const pieChartData = computed(() => {
  if (!chartData.value || !chartData.value.categoryBreakdown || chartData.value.categoryBreakdown.length === 0) return null
  const categories = chartData.value.categoryBreakdown
  return {
    labels: categories.map(c => c.category),
    datasets: [{
      data: categories.map(c => Number(c.amount)),
      backgroundColor: categoryColors.slice(0, categories.length),
      borderColor: '#fff',
      borderWidth: 2,
      hoverOffset: 8
    }]
  }
})

const pieChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom',
      labels: { usePointStyle: true, padding: 16, font: { size: 11, weight: '500' } }
    },
    tooltip: {
      backgroundColor: 'rgba(17, 24, 39, 0.9)',
      padding: 12,
      cornerRadius: 8,
      callbacks: {
        label: function(context) {
          const total = context.dataset.data.reduce((a, b) => a + b, 0)
          const percentage = ((context.raw / total) * 100).toFixed(1)
          return `${context.label}: Rp ${Number(context.raw).toLocaleString('id-ID')} (${percentage}%)`
        }
      }
    }
  }
}

const doughnutChartData = computed(() => {
  if (!chartData.value || !chartData.value.statusDistribution || chartData.value.statusDistribution.length === 0) return null
  const statuses = chartData.value.statusDistribution
  return {
    labels: statuses.map(s => statusLabelMap[s.status] || s.status),
    datasets: [{
      data: statuses.map(s => s.count),
      backgroundColor: statuses.map(s => statusColorMap[s.status] || '#9ca3af'),
      borderColor: '#fff',
      borderWidth: 3,
      hoverOffset: 6
    }]
  }
})

const doughnutChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: '65%',
  plugins: {
    legend: {
      position: 'bottom',
      labels: { usePointStyle: true, padding: 16, font: { size: 11, weight: '500' } }
    },
    tooltip: {
      backgroundColor: 'rgba(17, 24, 39, 0.9)',
      padding: 12,
      cornerRadius: 8,
      callbacks: {
        label: function(context) {
          const total = context.dataset.data.reduce((a, b) => a + b, 0)
          const percentage = ((context.raw / total) * 100).toFixed(1)
          return `${context.label}: ${context.raw} tagihan (${percentage}%)`
        }
      }
    }
  }
}

const barChartData = computed(() => {
  if (!chartData.value || !chartData.value.monthlyTrends || chartData.value.monthlyTrends.length === 0) return null
  const trends = chartData.value.monthlyTrends
  return {
    labels: trends.map(t => t.month),
    datasets: [
      {
        label: 'Tagihan',
        data: trends.map(t => Number(t.totalBills)),
        backgroundColor: 'rgba(99, 102, 241, 0.8)',
        borderColor: colors.primary,
        borderWidth: 1,
        borderRadius: 6,
        borderSkipped: false
      },
      {
        label: 'Dibayar',
        data: trends.map(t => Number(t.totalPaid)),
        backgroundColor: 'rgba(16, 185, 129, 0.8)',
        borderColor: colors.success,
        borderWidth: 1,
        borderRadius: 6,
        borderSkipped: false
      }
    ]
  }
})

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  interaction: { intersect: false, mode: 'index' },
  plugins: {
    legend: {
      position: 'top',
      labels: { usePointStyle: true, padding: 20, font: { size: 12, weight: '500' } }
    },
    tooltip: {
      backgroundColor: 'rgba(17, 24, 39, 0.9)',
      padding: 12,
      cornerRadius: 8,
      callbacks: {
        label: function(context) {
          return `${context.dataset.label}: Rp ${Number(context.raw).toLocaleString('id-ID')}`
        }
      }
    }
  },
  scales: {
    x: {
      stacked: true,
      grid: { display: false },
      ticks: { font: { size: 11 } }
    },
    y: {
      stacked: true,
      grid: { color: 'rgba(0,0,0,0.05)' },
      ticks: {
        font: { size: 11 },
        callback: function(value) {
          if (value >= 1000000) return 'Rp ' + (value / 1000000).toFixed(1) + 'jt'
          if (value >= 1000) return 'Rp ' + (value / 1000).toFixed(0) + 'rb'
          return 'Rp ' + value
        }
      }
    }
  }
}

// Lifecycle
onMounted(async () => {
  try {
    const [s, ub, cd] = await Promise.all([
      dashboardService.getSummary(),
      dashboardService.getUpcomingBills(),
      dashboardService.getChartData()
    ])
    summary.value = s
    upcomingBills.value = ub
    chartData.value = cd
  } catch (error) {
    console.error('Failed to load dashboard data:', error)
  } finally {
    loading.value = false
  }
})

async function quickMarkPaid(bill) {
  await billService.markAsPaid(bill.id)
  upcomingBills.value = upcomingBills.value.filter(b => b.id !== bill.id)
  const [s, cd] = await Promise.all([
    dashboardService.getSummary(),
    dashboardService.getChartData()
  ])
  summary.value = s
  chartData.value = cd
}

function goToEdit(bill) {
  router.push(`/bills/${bill.id}/edit`)
}

function formatAmount(val) {
  return Number(val || 0).toLocaleString('id-ID')
}
</script>

<style scoped>
/* Dashboard Layout */
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.dashboard-title {
  font-size: 28px;
  font-weight: 800;
  color: #111827;
  margin: 0;
}

.dashboard-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 4px 0 0 0;
}

.btn-add {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border-radius: 10px;
  font-weight: 600;
  font-size: 14px;
  text-decoration: none;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.btn-add:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
}

.btn-icon {
  font-size: 18px;
  font-weight: 700;
}

/* Loading State */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: #6b7280;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #6366f1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Summary Cards */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #f3f4f6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.card-icon {
  font-size: 32px;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  flex-shrink: 0;
}

.card-total .card-icon { background: rgba(99, 102, 241, 0.1); }
.card-paid .card-icon { background: rgba(16, 185, 129, 0.1); }
.card-unpaid .card-icon { background: rgba(245, 158, 11, 0.1); }
.card-overdue .card-icon { background: rgba(239, 68, 68, 0.1); }

.card-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-label {
  font-size: 12px;
  font-weight: 500;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.card-value {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.card-total .card-value { color: #6366f1; }
.card-paid .card-value { color: #10b981; }
.card-unpaid .card-value { color: #f59e0b; }
.card-overdue .card-value { color: #ef4444; }

/* Status Badges */
.status-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 32px;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.badge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.badge-warning {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}
.badge-warning .badge-dot { background: #f59e0b; }

.badge-info {
  background: rgba(59, 130, 246, 0.1);
  color: #2563eb;
}
.badge-info .badge-dot { background: #3b82f6; }

.badge-success {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
}
.badge-success .badge-dot { background: #10b981; }

/* Charts Section */
.charts-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.chart-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #f3f4f6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  transition: all 0.2s ease;
}

.chart-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.chart-wide {
  grid-column: span 2;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px 0;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.chart-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  background: rgba(99, 102, 241, 0.1);
  color: #6366f1;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.chart-body {
  padding: 16px 20px 20px;
  height: 280px;
  position: relative;
}

.chart-body-pie {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #9ca3af;
  font-size: 14px;
  font-style: italic;
}

/* Upcoming Bills Section */
.upcoming-section {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #f3f4f6;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.btn-link {
  font-size: 13px;
  font-weight: 600;
  color: #6366f1;
  text-decoration: none;
  transition: color 0.2s;
}

.btn-link:hover {
  color: #4f46e5;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 40px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-state h3 {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 6px;
}

.empty-state p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* Bills List */
.bills-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

/* Responsive */
@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .charts-section {
    grid-template-columns: 1fr;
  }

  .chart-wide {
    grid-column: span 1;
  }

  .status-badges {
    flex-direction: column;
  }

  .bills-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .dashboard-title {
    font-size: 22px;
  }

  .card-value {
    font-size: 16px;
  }

  .chart-body {
    height: 220px;
  }
}
</style>
