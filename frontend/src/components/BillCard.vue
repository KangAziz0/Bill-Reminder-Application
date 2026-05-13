<template>
  <div class="bill-card" :class="cardClass">
    <div class="bill-card-header">
      <div>
        <div class="bill-title">{{ bill.title }}</div>
        <div class="bill-category" v-if="bill.category">{{ bill.category }}</div>
      </div>
      <StatusBadge :status="bill.status" />
    </div>

    <div class="bill-amount">Rp {{ formatAmount(bill.amount) }}</div>

    <div class="bill-meta">
      <span>📅 {{ $t('bill.dueDateLabel') }}: {{ formatDate(bill.dueDate) }}</span>
      <span v-if="bill.isRecurring">🔁 {{ bill.recurringType }}</span>
    </div>

    <div class="bill-actions">
      <button v-if="bill.status !== 'PAID'" class="btn btn-success btn-sm" @click="$emit('markPaid', bill)">
        ✓ {{ $t('bill.markPaid') }}
      </button>
      <button class="btn btn-outline btn-sm" @click="$emit('edit', bill)">✏️ {{ $t('edit') }}</button>
      <button class="btn btn-danger btn-sm" @click="$emit('delete', bill)">🗑️ {{ $t('delete') }}</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import StatusBadge from './StatusBadge.vue'

const { locale } = useI18n()

const props = defineProps({
  bill: { type: Object, required: true }
})

defineEmits(['markPaid', 'edit', 'delete'])

const cardClass = computed(() => ({
  'border-overdue': props.bill.status === 'OVERDUE',
  'border-due-soon': props.bill.status === 'DUE_SOON',
  'border-paid': props.bill.status === 'PAID'
}))

function formatAmount(amount) {
  return Number(amount).toLocaleString('id-ID')
}

function formatDate(date) {
  if (!date) return '-'
  const lang = locale.value === 'en' ? 'en-US' : 'id-ID'
  return new Date(date).toLocaleDateString(lang, { day: 'numeric', month: 'long', year: 'numeric' })
}
</script>

<style scoped>
.bill-card {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 20px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border);
  border-left: 4px solid var(--border);
  transition: box-shadow 0.2s, background-color 0.3s, border-color 0.3s;
}
.bill-card:hover { box-shadow: var(--shadow-lg); }
.border-overdue { border-left-color: var(--danger); }
.border-due-soon { border-left-color: var(--warning); }
.border-paid { border-left-color: var(--success); }
.bill-card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.bill-title { font-size: 16px; font-weight: 600; color: var(--text-primary); }
.bill-category { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }
.bill-amount { font-size: 22px; font-weight: 700; color: var(--primary); margin-bottom: 10px; }
.bill-meta { font-size: 13px; color: var(--text-secondary); display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 14px; }
.bill-actions { display: flex; gap: 8px; flex-wrap: wrap; }
</style>
