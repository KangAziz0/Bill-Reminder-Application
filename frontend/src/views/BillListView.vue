<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">📋 {{ $t('bill.title') }}</h1>
      <RouterLink to="/bills/new" class="btn btn-primary">+ {{ $t('bill.addBill') }}</RouterLink>
    </div>

    <!-- Filters -->
    <div class="filters">
      <button
        v-for="f in filterOptions"
        :key="f.value"
        class="filter-btn"
        :class="{ active: activeFilter === f.value }"
        @click="setFilter(f.value)"
      >{{ f.label }}</button>
    </div>

    <div v-if="loading" class="loading-spinner"><div class="spinner"></div></div>

    <template v-else>
      <div v-if="filteredBills.length === 0" class="empty-state card">
        <div class="icon">📭</div>
        <h3>{{ $t('bill.noBills') }}</h3>
        <p>{{ $t('bill.addFirstBill') }}</p>
        <RouterLink to="/bills/new" class="btn btn-primary" style="margin-top: 16px;">+ {{ $t('bill.addBill') }}</RouterLink>
      </div>

      <div v-else class="bills-grid">
        <BillCard
          v-for="bill in filteredBills"
          :key="bill.id"
          :bill="bill"
          @markPaid="handleMarkPaid"
          @edit="goToEdit"
          @delete="confirmDelete"
        />
      </div>
    </template>

    <!-- Pay Modal -->
    <div v-if="showPayModal" class="modal-overlay" @click.self="showPayModal = false">
      <div class="modal">
        <div class="modal-header">
          <span class="modal-title">💳 {{ $t('payment.recordPayment') }}</span>
          <button class="modal-close" @click="showPayModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <p class="pay-bill-name">{{ selectedBill?.title }}</p>
          <p class="pay-bill-amount">Rp {{ formatAmount(selectedBill?.amount) }}</p>
          <div class="form-group">
            <label class="form-label">{{ $t('payment.paidAmount') }}</label>
            <input v-model="payForm.paidAmount" type="number" class="form-control" :placeholder="selectedBill?.amount" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('payment.paidDate') }}</label>
            <input v-model="payForm.paidDate" type="date" class="form-control" />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('payment.paymentMethod') }}</label>
            <select v-model="payForm.paymentMethod" class="form-control">
              <option value="">{{ $t('payment.selectMethod') }}</option>
              <option value="Transfer Bank">{{ $t('payment.methodTransfer') }}</option>
              <option value="Virtual Account">{{ $t('payment.methodVA') }}</option>
              <option value="QRIS">{{ $t('payment.methodQRIS') }}</option>
              <option value="Tunai">{{ $t('payment.methodCash') }}</option>
              <option value="Kartu Kredit">{{ $t('payment.methodCC') }}</option>
              <option value="Kartu Debit">{{ $t('payment.methodDebit') }}</option>
              <option value="E-Wallet">{{ $t('payment.methodEwallet') }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('payment.notes') }}</label>
            <textarea v-model="payForm.notes" class="form-control" rows="2" :placeholder="$t('payment.notesPlaceholder')"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showPayModal = false">{{ $t('cancel') }}</button>
          <button class="btn btn-success" @click="submitPayment" :disabled="paying">
            <span v-if="paying">⏳</span>
            <span v-else>✓ {{ $t('payment.confirmPay') }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Delete Confirm Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal">
        <div class="modal-header">
          <span class="modal-title">🗑️ {{ $t('bill.deleteBill') }}</span>
          <button class="modal-close" @click="showDeleteModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <p>{{ $t('bill.deleteConfirm') }} <strong>{{ selectedBill?.title }}</strong>?</p>
          <p style="color: var(--text-secondary); font-size: 13px; margin-top: 8px;">{{ $t('bill.deleteWarning') }}</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showDeleteModal = false">{{ $t('cancel') }}</button>
          <button class="btn btn-danger" @click="deleteBill" :disabled="deleting">
            <span v-if="deleting">⏳</span>
            <span v-else>{{ $t('delete') }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import BillCard from '@/components/BillCard.vue'
import billService from '@/services/billService.js'
import paymentService from '@/services/paymentService.js'

const { t } = useI18n()
const router = useRouter()
const bills = ref([])
const loading = ref(true)
const activeFilter = ref('ALL')
const showPayModal = ref(false)
const showDeleteModal = ref(false)
const selectedBill = ref(null)
const paying = ref(false)
const deleting = ref(false)
const payForm = ref({ paidAmount: '', paidDate: new Date().toISOString().split('T')[0], paymentMethod: '', notes: '' })

const filterOptions = computed(() => [
  { label: `📋 ${t('bill.filterAll')}`, value: 'ALL' },
  { label: `📅 ${t('bill.filterUpcoming')}`, value: 'UPCOMING' },
  { label: `⚠️ ${t('bill.filterDueSoon')}`, value: 'DUE_SOON' },
  { label: `🚨 ${t('bill.filterOverdue')}`, value: 'OVERDUE' },
  { label: `✅ ${t('bill.filterPaid')}`, value: 'PAID' }
])

const filteredBills = computed(() =>
  activeFilter.value === 'ALL' ? bills.value : bills.value.filter(b => b.status === activeFilter.value)
)

onMounted(loadBills)

async function loadBills() {
  loading.value = true
  try {
    bills.value = await billService.getAll()
  } finally {
    loading.value = false
  }
}

function setFilter(f) { activeFilter.value = f }

function goToEdit(bill) { router.push(`/bills/${bill.id}/edit`) }

function handleMarkPaid(bill) {
  selectedBill.value = bill
  payForm.value = {
    paidAmount: bill.amount,
    paidDate: new Date().toISOString().split('T')[0],
    paymentMethod: '',
    notes: ''
  }
  showPayModal.value = true
}

async function submitPayment() {
  paying.value = true
  try {
    await paymentService.create({
      billId: selectedBill.value.id,
      paidAmount: Number(payForm.value.paidAmount),
      paidDate: payForm.value.paidDate,
      paymentMethod: payForm.value.paymentMethod,
      notes: payForm.value.notes
    })
    showPayModal.value = false
    await loadBills()
  } finally {
    paying.value = false
  }
}

function confirmDelete(bill) {
  selectedBill.value = bill
  showDeleteModal.value = true
}

async function deleteBill() {
  deleting.value = true
  try {
    await billService.delete(selectedBill.value.id)
    showDeleteModal.value = false
    bills.value = bills.value.filter(b => b.id !== selectedBill.value.id)
  } finally {
    deleting.value = false
  }
}

function formatAmount(val) {
  return Number(val || 0).toLocaleString('id-ID')
}
</script>

<style scoped>
.bills-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }
.pay-bill-name { font-size: 18px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
.pay-bill-amount { font-size: 22px; font-weight: 700; color: var(--primary); margin-bottom: 20px; }
</style>
