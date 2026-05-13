import { ref, watch } from 'vue'

const isDark = ref(localStorage.getItem('theme') === 'dark')

function applyTheme() {
  if (isDark.value) {
    document.documentElement.classList.add('dark')
  } else {
    document.documentElement.classList.remove('dark')
  }
}

// Apply on initial load
applyTheme()

function toggleTheme() {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  applyTheme()
}

export function useTheme() {
  return { isDark, toggleTheme }
}
