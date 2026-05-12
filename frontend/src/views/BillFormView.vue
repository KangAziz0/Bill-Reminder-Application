<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">{{ isEdit ? '✏️ Edit Tagihan' : '➕ Tambah Tagihan' }}</h1>
      <RouterLink to="/bills" class="btn btn-outline">← Kembali</RouterLink>
    </div>

    <div class="card form-card">
      <div v-if="error" class="alert alert-danger">{{ error }}</div>
      <div v-if="success" class="alert alert-success">{{ success }}</div>

      <form @submit.prevent="handleSubmit">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Nama Tagihan *</label>
            <input v-model="form.title" type="text" class="form-control" placeholder="Internet, Listrik, Sewa..." required />
          </div>
          <div class="form-group">
            <label class="form-label">Kategori</label>
            <select v-model="form.category" class="form-control">
              <option value="">Pilih kategori</option>
              <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Nominal (Rp) *</label>
            <input v-model="form.amount" type="number" class="form-control" placeholder="150000" min="1" required />
          </div>
          <div class="form-group">
            <label class="form-label">Tanggal Jatuh Tempo *</label>
            <input v-model="form.dueDate" type="date" class="form-control" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Reminder (hari sebelum jatuh tempo)</label>
            <select v-model="form.reminderDaysBefore" class="form-control">
              <option :value="1">H-1 (1 hari sebelum)</option>
              <option :value="3">H-3 (3 hari sebelum)</option>
              <option :value="7">H-7 (7 hari sebelum)</option>
              <option :value="14">H-14 (2 minggu sebelum)</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Tagihan Berulang?</label>
            <select v-model="form.isRecurring" class="form-control">
              <option :value="false">Tidak</option>
              <option :value="true">Ya</option>
            </select>
          </div>
        </div>

        <div v-if="form.isRecurring" class="form-group">
          <label class="form-label">Tipe Pengulangan</label>
          <select v-model="form.recurringType" class="form-control">
            <option value="WEEKLY">Mingguan</option>
            <option value="MONTHLY">Bulanan</option>
            <option value="YEARLY">Tahunan</option>
          </select>
        </div>

        <div class="form-actions">
          <RouterLink to="/bills" class="btn btn-outline">Batal</RouterLink>
          <button type="submit" class="btn btn-primary" :disabled="saving">
            <span v-if="saving">⏳ Menyimpan...</span>
            <span v-else>{{ isEdit ? '💾 Update Tagihan' : '➕ Simpan Tagihan' }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import billService from '@/services/billService.js'

const route = useRoute()
const router = useRouter()
const saving = ref(false)
const error = ref('')
const success = ref('')

const isEdit = computed(() => !!route.params.id)

const categories = ['Internet', 'Listrik', 'Air', 'Gas', 'Telepon', 'Sewa', 'Cicilan', 'Asuransi', 'Subscription', 'Lainnya']

const form = ref({
  title: '',
  category: '',
  amount: '',
  dueDate: '',
  reminderDaysBefore: 3,
  isRecurring: false,
  recurringType: 'MONTHLY'
})

onMounted(async () => {
  if (isEdit.value) {
    try {
      const bill = await billService.getById(route.params.id)
      form.value = {
        title: bill.title,
        category: bill.category || '',
        amount: bill.amount,
        dueDate: bill.dueDate,
        reminderDaysBefore: bill.reminderDaysBefore,
        isRecurring: bill.isRecurring,
        recurringType: bill.recurringType || 'MONTHLY'
      }
    } catch (e) {
      error.value = 'Gagal memuat data tagihan.'
    }
  }
})

async function handleSubmit() {
  saving.value = true
  error.value = ''
  success.value = ''
  try {
    const payload = {
      title: form.value.title,
      category: form.value.category || null,
      amount: Number(form.value.amount),
      dueDate: form.value.dueDate,
      reminderDaysBefore: Number(form.value.reminderDaysBefore),
      isRecurring: form.value.isRecurring,
      recurringType: form.value.isRecurring ? form.value.recurringType : null
    }

    if (isEdit.value) {
      await billService.update(route.params.id, payload)
      success.value = 'Tagihan berhasil diperbarui!'
    } else {
      await billService.create(payload)
      success.value = 'Tagihan berhasil disimpan!'
    }

    setTimeout(() => router.push('/bills'), 1000)
  } catch (e) {
    const errors = e.response?.data
    if (typeof errors === 'object') {
      error.value = Object.values(errors).join(', ')
    } else {
      error.value = errors?.error || 'Gagal menyimpan tagihan.'
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.form-card { max-width: 700px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
@media (max-width: 600px) { .form-row { grid-template-columns: 1fr; } }
.form-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
</style>
