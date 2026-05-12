<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">📋 Daftar Tagihan</h1>
      <RouterLink to="/bills/new" class="btn btn-primary">+ Tambah Tagihan</RouterLink>
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
        <h3>Tidak ada tagihan</h3>
        <p>Tambahkan tagihan pertama Anda!</p>
        <RouterLink to="/bills/new" class="btn btn-primary" style="margin-top: 16px;">+ Tambah Tagihan</RouterLink>
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
          <span class="modal-title">💳 Catat Pembayaran</span>
          <button class="modal-close" @click="showPayModal = false">×</button>
        </div>
        <div class="modal-body">
          <p class="pay-bill-name">{{ selectedBill?.title }}</p>
          <p class="pay-bill-amount">Rp {{ formatAmount(selectedBill?.amount) }}</p>
          <div class="form-group">
            <label class="form-label">Nominal Bayar</label>
            <input v-model="payForm.paidAmount" type="number" class="form-control" :placeholder="selectedBill?.amount" />
          </div>
          <div class="form-group">
            <label class="form-label">Tanggal Bayar</label>
            <input v-model="payForm.paidDate" type="date" class="form-control" />
          </div>
          <div class="form-group">
            <label class="form-label">Metode Pembayaran</label>
            <select v-model="payForm.paymentMethod" class="form-control">
              <option value="">Pilih metode</option>
              <option value="Transfer Bank">Transfer Bank</option>
              <option value="Virtual Account">Virtual Account</option>
              <option value="QRIS">QRIS</option>
              <option value="Tunai">Tunai</option>
              <option value="Kartu Kredit">Kartu Kredit</option>
              <option value="Kartu Debit">Kartu Debit</option>
              <option value="E-Wallet">E-Wallet</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Catatan (opsional)</label>
            <textarea v-model="payForm.notes" class="form-control" rows="2" placeholder="Catatan pembayaran..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showPayModal = false">Batal</button>
          <button class="btn btn-success" @click="submitPayment" :disabled="paying">
            <span v-if="paying">⏳</span>
            <span v-else>✓ Konfirmasi Bayar</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Delete Confirm Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal">
        <div class="modal-header">
          <span class="modal-title">🗑️ Hapus Tagihan</span>
          <button class="modal-close" @click="showDeleteModal = false">×</button>
        </div>
        <div class="modal-body">
          <p>Apakah Anda yakin ingin menghapus tagihan <strong>{{ selectedBill?.title }}</strong>?</p>
          <p style="color: #6b7280; font-size: 13px; margin-top: 8px;">Tindakan ini tidak dapat dibatalkan.</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="showDeleteModal = false">Batal</button>
          <button class="btn btn-danger" @click="deleteBill" :disabled="deleting">
            <span v-if="deleting">⏳</span>
            <span v-else>Hapus</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import BillCard from '@/components/BillCard.vue'
import billService from '@/services/billService.js'
import paymentService from '@/services/paymentService.js'

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

const filterOptions = [
  { label: '📋 Semua', value: 'ALL' },
  { label: '📅 Upcoming', value: 'UPCOMING' },
  { label: '⚠️ Mendekati', value: 'DUE_SOON' },
  { label: '🚨 Terlambat', value: 'OVERDUE' },
  { label: '✅ Lunas', value: 'PAID' }
]

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
.pay-bill-name { font-size: 18px; font-weight: 600; margin-bottom: 4px; }
.pay-bill-amount { font-size: 22px; font-weight: 700; color: #4f46e5; margin-bottom: 20px; }
</style>
