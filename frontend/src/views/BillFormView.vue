<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">{{ isEdit ? `✏️ ${$t('bill.editBill')}` : `➕ ${$t('bill.addBill')}` }}</h1>
      <RouterLink to="/bills" class="btn btn-outline">← {{ $t('back') }}</RouterLink>
    </div>

    <div class="card form-card">
      <div v-if="error" class="alert alert-danger">{{ error }}</div>
      <div v-if="success" class="alert alert-success">{{ success }}</div>

      <form @submit.prevent="handleSubmit">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('bill.billName') }} *</label>
            <input v-model="form.title" type="text" class="form-control" :placeholder="$t('bill.billNamePlaceholder')" required />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('bill.category') }}</label>
            <select v-model="form.category" class="form-control">
              <option value="">{{ $t('bill.selectCategory') }}</option>
              <option v-for="cat in categories" :key="cat.value" :value="cat.value">{{ cat.label }}</option>
            </select>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('bill.amount') }} *</label>
            <input v-model="form.amount" type="number" class="form-control" :placeholder="$t('bill.amountPlaceholder')" min="1" required />
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('bill.dueDate') }} *</label>
            <input v-model="form.dueDate" type="date" class="form-control" required />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">{{ $t('bill.reminder') }}</label>
            <select v-model="form.reminderDaysBefore" class="form-control">
              <option :value="1">{{ $t('bill.reminderH1') }}</option>
              <option :value="3">{{ $t('bill.reminderH3') }}</option>
              <option :value="7">{{ $t('bill.reminderH7') }}</option>
              <option :value="14">{{ $t('bill.reminderH14') }}</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">{{ $t('bill.recurring') }}</label>
            <select v-model="form.isRecurring" class="form-control">
              <option :value="false">{{ $t('bill.recurringNo') }}</option>
              <option :value="true">{{ $t('bill.recurringYes') }}</option>
            </select>
          </div>
        </div>

        <div v-if="form.isRecurring" class="form-group">
          <label class="form-label">{{ $t('bill.recurringType') }}</label>
          <select v-model="form.recurringType" class="form-control">
            <option value="WEEKLY">{{ $t('bill.weekly') }}</option>
            <option value="MONTHLY">{{ $t('bill.monthly') }}</option>
            <option value="YEARLY">{{ $t('bill.yearly') }}</option>
          </select>
        </div>

        <div class="form-actions">
          <RouterLink to="/bills" class="btn btn-outline">{{ $t('cancel') }}</RouterLink>
          <button type="submit" class="btn btn-primary" :disabled="saving">
            <span v-if="saving">⏳ {{ $t('bill.saving') }}</span>
            <span v-else>{{ isEdit ? `💾 ${$t('bill.updateBill')}` : `➕ ${$t('bill.saveBill')}` }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import billService from '@/services/billService.js'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const saving = ref(false)
const error = ref('')
const success = ref('')

const isEdit = computed(() => !!route.params.id)

const categories = computed(() => [
  { value: 'Internet', label: t('bill.catInternet') },
  { value: 'Listrik', label: t('bill.catElectricity') },
  { value: 'Air', label: t('bill.catWater') },
  { value: 'Gas', label: t('bill.catGas') },
  { value: 'Telepon', label: t('bill.catPhone') },
  { value: 'Sewa', label: t('bill.catRent') },
  { value: 'Cicilan', label: t('bill.catInstallment') },
  { value: 'Asuransi', label: t('bill.catInsurance') },
  { value: 'Subscription', label: t('bill.catSubscription') },
  { value: 'Lainnya', label: t('bill.catOther') }
])

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
      error.value = t('bill.loadFailed')
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
      success.value = t('bill.billUpdated')
    } else {
      await billService.create(payload)
      success.value = t('bill.billSaved')
    }

    setTimeout(() => router.push('/bills'), 1000)
  } catch (e) {
    const errors = e.response?.data
    if (typeof errors === 'object') {
      error.value = Object.values(errors).join(', ')
    } else {
      error.value = errors?.error || t('bill.saveFailed')
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
