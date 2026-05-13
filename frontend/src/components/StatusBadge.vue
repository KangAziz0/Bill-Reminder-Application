<template>
  <span class="badge" :class="badgeClass">{{ label }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

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
  const map = {
    UPCOMING: 'status.upcoming',
    DUE_SOON: 'status.dueSoon',
    OVERDUE: 'status.overdue',
    PAID: 'status.paid'
  }
  return t(map[props.status] || props.status)
})
</script>
