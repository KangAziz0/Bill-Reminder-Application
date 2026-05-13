<template>
  <div class="dashboard">
    <!-- Header -->
    <div class="dashboard-header">
      <div>
        <h1 class="dashboard-title">{{ $t("dashboard.title") }}</h1>
        <p class="dashboard-subtitle">{{ $t("dashboard.subtitle") }}</p>
      </div>
      <RouterLink to="/bills/new" class="btn-add">
        <span class="btn-icon">+</span> {{ $t("dashboard.addBill") }}
      </RouterLink>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>{{ $t("loading") }}</p>
    </div>

    <template v-else>
      <!-- Summary Cards -->
      <div class="summary-grid">
        <div class="summary-card card-total">
          <div class="card-icon">💰</div>
          <div class="card-content">
            <span class="card-label">{{
              $t("dashboard.totalBillsThisMonth")
            }}</span>
            <span class="card-value"
              >Rp {{ formatAmount(summary.totalBillsThisMonth) }}</span
            >
          </div>
        </div>
        <div class="summary-card card-paid">
          <div class="card-icon">✅</div>
          <div class="card-content">
            <span class="card-label">{{ $t("dashboard.paid") }}</span>
            <span class="card-value"
              >Rp {{ formatAmount(summary.totalPaid) }}</span
            >
          </div>
        </div>
        <div class="summary-card card-unpaid">
          <div class="card-icon">⏳</div>
          <div class="card-content">
            <span class="card-label">{{ $t("dashboard.unpaid") }}</span>
            <span class="card-value"
              >Rp {{ formatAmount(summary.totalUnpaid) }}</span
            >
          </div>
        </div>
        <div class="summary-card card-overdue">
          <div class="card-icon">🚨</div>
          <div class="card-content">
            <span class="card-label">{{ $t("dashboard.overdue") }}</span>
            <span class="card-value"
              >{{ summary.overdueCount }} {{ $t("dashboard.bills") }}</span
            >
          </div>
        </div>
      </div>

      <!-- Status Badges -->
      <div class="status-badges">
        <div class="badge badge-warning">
          <span class="badge-dot"></span>
          <span>{{ summary.dueSoonCount }} {{ $t("dashboard.dueSoon") }}</span>
        </div>
        <div class="badge badge-info">
          <span class="badge-dot"></span>
          <span
            >{{ summary.upcomingCount }} {{ $t("dashboard.upcoming") }}</span
          >
        </div>
        <div class="badge badge-success">
          <span class="badge-dot"></span>
          <span>{{ summary.paidCount }} {{ $t("dashboard.paidBills") }}</span>
        </div>
      </div>

      <!-- Charts Section -->
      <div class="charts-section">
        <!-- Line Chart - Monthly Trends -->
        <div class="chart-card chart-wide">
          <div class="chart-header">
            <h3 class="chart-title">{{ $t("dashboard.monthlyTrend") }}</h3>
            <span class="chart-badge">{{ $t("dashboard.lineChart") }}</span>
          </div>
          <div class="chart-body">
            <Line
              v-if="lineChartData"
              :data="lineChartData"
              :options="lineChartOptions"
            />
            <div v-else class="chart-empty">
              {{ $t("dashboard.noTrendData") }}
            </div>
          </div>
        </div>

        <!-- Pie Chart - Category Breakdown -->
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">
              {{ $t("dashboard.categoryDistribution") }}
            </h3>
            <span class="chart-badge">{{ $t("dashboard.pieChart") }}</span>
          </div>
          <div class="chart-body chart-body-pie">
            <Pie
              v-if="pieChartData"
              :data="pieChartData"
              :options="pieChartOptions"
            />
            <div v-else class="chart-empty">
              {{ $t("dashboard.noCategoryData") }}
            </div>
          </div>
        </div>

        <!-- Doughnut Chart - Status Distribution -->
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">
              {{ $t("dashboard.statusDistribution") }}
            </h3>
            <span class="chart-badge">{{ $t("dashboard.doughnut") }}</span>
          </div>
          <div class="chart-body chart-body-pie">
            <Doughnut
              v-if="doughnutChartData"
              :data="doughnutChartData"
              :options="doughnutChartOptions"
            />
            <div v-else class="chart-empty">
              {{ $t("dashboard.noStatusData") }}
            </div>
          </div>
        </div>

        <!-- Stacked Bar Chart - Monthly Comparison -->
        <div class="chart-card chart-wide">
          <div class="chart-header">
            <h3 class="chart-title">{{ $t("dashboard.billVsPayment") }}</h3>
            <span class="chart-badge">{{ $t("dashboard.stackedBar") }}</span>
          </div>
          <div class="chart-body">
            <Bar
              v-if="barChartData"
              :data="barChartData"
              :options="barChartOptions"
            />
            <div v-else class="chart-empty">
              {{ $t("dashboard.noComparisonData") }}
            </div>
          </div>
        </div>
      </div>

      <!-- Upcoming Bills -->
      <div class="upcoming-section">
        <div class="section-header">
          <h2 class="section-title">
            📅 {{ $t("dashboard.upcomingBillsTitle") }}
          </h2>
          <RouterLink to="/bills" class="btn-link"
            >{{ $t("viewAll") }} →</RouterLink
          >
        </div>

        <div v-if="upcomingBills.length === 0" class="empty-state">
          <div class="empty-icon">🎉</div>
          <h3>{{ $t("dashboard.noUpcomingBills") }}</h3>
          <p>{{ $t("dashboard.allSafe") }}</p>
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
import { ref, onMounted, computed } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
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
  Filler,
} from "chart.js";
import { Line, Pie, Bar, Doughnut } from "vue-chartjs";
import BillCard from "@/components/BillCard.vue";
import dashboardService from "@/services/dashboardService.js";
import billService from "@/services/billService.js";

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
  Filler,
);

const { t } = useI18n();
const router = useRouter();
const loading = ref(true);
const summary = ref({
  totalBillsThisMonth: 0,
  totalPaid: 0,
  totalUnpaid: 0,
  overdueCount: 0,
  dueSoonCount: 0,
  upcomingCount: 0,
  paidCount: 0,
});

const upcomingBills = ref([]);
const chartData = ref(null);

// Color palette
const colors = {
  primary: "#6366f1",
  primaryLight: "rgba(99, 102, 241, 0.1)",
  success: "#10b981",
  successLight: "rgba(16, 185, 129, 0.1)",
  warning: "#f59e0b",
  danger: "#ef4444",
  info: "#3b82f6",
};

const categoryColors = [
  "#6366f1",
  "#10b981",
  "#f59e0b",
  "#ef4444",
  "#8b5cf6",
  "#ec4899",
  "#14b8a6",
  "#f97316",
  "#06b6d4",
  "#84cc16",
  "#3b82f6",
  "#d946ef",
];

const statusColorMap = {
  PAID: "#10b981",
  UPCOMING: "#3b82f6",
  DUE_SOON: "#f59e0b",
  OVERDUE: "#ef4444",
};

// Chart data computations
const lineChartData = computed(() => {
  if (
    !chartData.value ||
    !chartData.value.monthlyTrends ||
    chartData.value.monthlyTrends.length === 0
  )
    return null;
  const trends = chartData.value.monthlyTrends;
  return {
    labels: trends.map((item) => item.month),
    datasets: [
      {
        label: t("dashboard.totalBills"),
        data: trends.map((item) => Number(item.totalBills)),
        borderColor: colors.primary,
        backgroundColor: colors.primaryLight,
        fill: true,
        tension: 0.4,
        pointBackgroundColor: colors.primary,
        pointBorderColor: "#fff",
        pointBorderWidth: 2,
        pointRadius: 5,
        pointHoverRadius: 7,
      },
      {
        label: t("dashboard.totalPaid"),
        data: trends.map((item) => Number(item.totalPaid)),
        borderColor: colors.success,
        backgroundColor: colors.successLight,
        fill: true,
        tension: 0.4,
        pointBackgroundColor: colors.success,
        pointBorderColor: "#fff",
        pointBorderWidth: 2,
        pointRadius: 5,
        pointHoverRadius: 7,
      },
    ],
  };
});

const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  interaction: { intersect: false, mode: "index" },
  plugins: {
    legend: {
      position: "top",
      labels: {
        usePointStyle: true,
        padding: 20,
        font: { size: 12, weight: "500" },
        color: "inherit",
      },
    },
    tooltip: {
      backgroundColor: "rgba(17, 24, 39, 0.9)",
      titleFont: { size: 13 },
      bodyFont: { size: 12 },
      padding: 12,
      cornerRadius: 8,
      callbacks: {
        label: function (context) {
          return `${context.dataset.label}: Rp ${Number(context.raw).toLocaleString("id-ID")}`;
        },
      },
    },
  },
  scales: {
    x: { grid: { display: false }, ticks: { font: { size: 11 } } },
    y: {
      grid: { color: "rgba(0,0,0,0.05)" },
      ticks: {
        font: { size: 11 },
        callback: function (value) {
          if (value >= 1000000)
            return "Rp " + (value / 1000000).toFixed(1) + "jt";
          if (value >= 1000) return "Rp " + (value / 1000).toFixed(0) + "rb";
          return "Rp " + value;
        },
      },
    },
  },
};

const pieChartData = computed(() => {
  if (
    !chartData.value ||
    !chartData.value.categoryBreakdown ||
    chartData.value.categoryBreakdown.length === 0
  )
    return null;
  const categories = chartData.value.categoryBreakdown;
  return {
    labels: categories.map((c) => c.category),
    datasets: [
      {
        data: categories.map((c) => Number(c.amount)),
        backgroundColor: categoryColors.slice(0, categories.length),
        borderColor: "#fff",
        borderWidth: 2,
        hoverOffset: 8,
      },
    ],
  };
});

const pieChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: "bottom",
      labels: {
        usePointStyle: true,
        padding: 16,
        font: { size: 11, weight: "500" },
      },
    },
    tooltip: {
      backgroundColor: "rgba(17, 24, 39, 0.9)",
      padding: 12,
      cornerRadius: 8,
      callbacks: {
        label: function (context) {
          const total = context.dataset.data.reduce((a, b) => a + b, 0);
          const percentage = ((context.raw / total) * 100).toFixed(1);
          return `${context.label}: Rp ${Number(context.raw).toLocaleString("id-ID")} (${percentage}%)`;
        },
      },
    },
  },
};

const doughnutChartData = computed(() => {
  if (
    !chartData.value ||
    !chartData.value.statusDistribution ||
    chartData.value.statusDistribution.length === 0
  )
    return null;
  const statuses = chartData.value.statusDistribution;
  return {
    labels: statuses.map((s) =>
      t(
        `status.${s.status === "DUE_SOON" ? "dueSoon" : s.status.toLowerCase()}`,
      ),
    ),
    datasets: [
      {
        data: statuses.map((s) => s.count),
        backgroundColor: statuses.map(
          (s) => statusColorMap[s.status] || "#9ca3af",
        ),
        borderColor: "#fff",
        borderWidth: 3,
        hoverOffset: 6,
      },
    ],
  };
});

const doughnutChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: "65%",
  plugins: {
    legend: {
      position: "bottom",
      labels: {
        usePointStyle: true,
        padding: 16,
        font: { size: 11, weight: "500" },
      },
    },
    tooltip: {
      backgroundColor: "rgba(17, 24, 39, 0.9)",
      padding: 12,
      cornerRadius: 8,
      callbacks: {
        label: function (context) {
          const total = context.dataset.data.reduce((a, b) => a + b, 0);
          const percentage = ((context.raw / total) * 100).toFixed(1);
          return `${context.label}: ${context.raw} (${percentage}%)`;
        },
      },
    },
  },
};

const barChartData = computed(() => {
  if (
    !chartData.value ||
    !chartData.value.monthlyTrends ||
    chartData.value.monthlyTrends.length === 0
  )
    return null;
  const trends = chartData.value.monthlyTrends;
  return {
    labels: trends.map((item) => item.month),
    datasets: [
      {
        label: t("dashboard.billsLabel"),
        data: trends.map((item) => Number(item.totalBills)),
        backgroundColor: "rgba(99, 102, 241, 0.8)",
        borderColor: colors.primary,
        borderWidth: 1,
        borderRadius: 6,
        borderSkipped: false,
      },
      {
        label: t("dashboard.paidLabel"),
        data: trends.map((item) => Number(item.totalPaid)),
        backgroundColor: "rgba(16, 185, 129, 0.8)",
        borderColor: colors.success,
        borderWidth: 1,
        borderRadius: 6,
        borderSkipped: false,
      },
    ],
  };
});

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  interaction: { intersect: false, mode: "index" },
  plugins: {
    legend: {
      position: "top",
      labels: {
        usePointStyle: true,
        padding: 20,
        font: { size: 12, weight: "500" },
      },
    },
    tooltip: {
      backgroundColor: "rgba(17, 24, 39, 0.9)",
      padding: 12,
      cornerRadius: 8,
      callbacks: {
        label: function (context) {
          return `${context.dataset.label}: Rp ${Number(context.raw).toLocaleString("id-ID")}`;
        },
      },
    },
  },
  scales: {
    x: {
      stacked: true,
      grid: { display: false },
      ticks: { font: { size: 11 } },
    },
    y: {
      stacked: true,
      grid: { color: "rgba(0,0,0,0.05)" },
      ticks: {
        font: { size: 11 },
        callback: function (value) {
          if (value >= 1000000)
            return "Rp " + (value / 1000000).toFixed(1) + "jt";
          if (value >= 1000) return "Rp " + (value / 1000).toFixed(0) + "rb";
          return "Rp " + value;
        },
      },
    },
  },
};

// Lifecycle
onMounted(async () => {
  try {
    const [s, ub, cd] = await Promise.all([
      dashboardService.getSummary(),
      dashboardService.getUpcomingBills(),
      dashboardService.getChartData(),
    ]);

    console.log("summary api:", s);

    summary.value = s;
    upcomingBills.value = ub;
    chartData.value = cd;
  } catch (error) {
    console.error("Failed to load dashboard data:", error);
  } finally {
    loading.value = false;
  }
});

async function quickMarkPaid(bill) {
  await billService.markAsPaid(bill.id);
  upcomingBills.value = upcomingBills.value.filter((b) => b.id !== bill.id);
  const [s, cd] = await Promise.all([
    dashboardService.getSummary(),
    dashboardService.getChartData(),
  ]);
  summary.value = s;
  chartData.value = cd;
}

function goToEdit(bill) {
  router.push(`/bills/${bill.id}/edit`);
}

function formatAmount(val) {
  return Number(val || 0).toLocaleString("id-ID");
}
</script>

<style scoped>
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
  color: var(--text-primary);
  margin: 0;
}

.dashboard-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
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

/* Loading */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  color: var(--text-secondary);
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
  background: var(--bg-card);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  transition: all 0.2s ease;
}

.summary-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
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

.card-total .card-icon {
  background: rgba(99, 102, 241, 0.1);
}
.card-paid .card-icon {
  background: rgba(16, 185, 129, 0.1);
}
.card-unpaid .card-icon {
  background: rgba(245, 158, 11, 0.1);
}
.card-overdue .card-icon {
  background: rgba(239, 68, 68, 0.1);
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.card-value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}
.card-total .card-value {
  color: var(--primary);
}
.card-paid .card-value {
  color: var(--success);
}
.card-unpaid .card-value {
  color: var(--warning);
}
.card-overdue .card-value {
  color: var(--danger);
}

/* Status Badges */
.status-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 32px;
}

.status-badges .badge {
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
  color: var(--warning);
}
.badge-warning .badge-dot {
  background: var(--warning);
}
.badge-info {
  background: rgba(59, 130, 246, 0.1);
  color: var(--info);
}
.badge-info .badge-dot {
  background: var(--info);
}
.badge-success {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success);
}
.badge-success .badge-dot {
  background: var(--success);
}

/* Charts */
.charts-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.chart-card {
  background: var(--bg-card);
  border-radius: 14px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  overflow: hidden;
  transition: all 0.2s ease;
}

.chart-card:hover {
  box-shadow: var(--shadow-lg);
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
  color: var(--text-primary);
  margin: 0;
}

.chart-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
  background: var(--primary-light);
  color: var(--primary);
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
  color: var(--text-muted);
  font-size: 14px;
  font-style: italic;
}

/* Upcoming Bills */
.upcoming-section {
  background: var(--bg-card);
  border-radius: 14px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  padding: 24px;
  transition:
    background-color 0.3s ease,
    border-color 0.3s ease;
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
  color: var(--text-primary);
  margin: 0;
}

.btn-link {
  font-size: 13px;
  font-weight: 600;
  color: var(--primary);
  text-decoration: none;
  transition: color 0.2s;
}

.btn-link:hover {
  color: var(--primary-dark);
}

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
  color: var(--text-primary);
  margin: 0 0 6px;
}
.empty-state p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

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
