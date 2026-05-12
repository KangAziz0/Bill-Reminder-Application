<template>
  <span class="badge" :class="badgeClass">{{ label }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, required: true }
})

const badgeClass = computed(() => ({
  'badge-upcoming': props.status === 'UPCOMING',
  'badge-due-soon': props.status === 'DUE_SOON',
  'badge-overdue': props.status === 'OVERDUE',
  'badge-paid': props.status === 'PAID'
}))

const label = computed(() => {
  const labels = {
    UPCOMING: 'Upcoming',
    DUE_SOON: 'Mendekati',
    OVERDUE: 'Terlambat',
    PAID: 'Lunas'
  }
  return labels[props.status] || props.status
})
</script>
