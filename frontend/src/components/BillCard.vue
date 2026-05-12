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
      <span>📅 Jatuh tempo: {{ formatDate(bill.dueDate) }}</span>
      <span v-if="bill.isRecurring">🔁 {{ bill.recurringType }}</span>
    </div>

    <div class="bill-actions">
      <button v-if="bill.status !== 'PAID'" class="btn btn-success btn-sm" @click="$emit('markPaid', bill)">
        ✓ Tandai Lunas
      </button>
      <button class="btn btn-outline btn-sm" @click="$emit('edit', bill)">✏️ Edit</button>
      <button class="btn btn-danger btn-sm" @click="$emit('delete', bill)">🗑️ Hapus</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import StatusBadge from './StatusBadge.vue'

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
  return new Date(date).toLocaleDateString('id-ID', { day: 'numeric', month: 'long', year: 'numeric' })
}
</script>

<style scoped>
.bill-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
  border-left: 4px solid #e5e7eb;
  transition: box-shadow 0.2s;
}
.bill-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.border-overdue { border-left-color: #ef4444; }
.border-due-soon { border-left-color: #f59e0b; }
.border-paid { border-left-color: #10b981; }
.bill-card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.bill-title { font-size: 16px; font-weight: 600; color: #1f2937; }
.bill-category { font-size: 12px; color: #6b7280; margin-top: 2px; }
.bill-amount { font-size: 22px; font-weight: 700; color: #4f46e5; margin-bottom: 10px; }
.bill-meta { font-size: 13px; color: #6b7280; display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 14px; }
.bill-actions { display: flex; gap: 8px; flex-wrap: wrap; }
</style>
