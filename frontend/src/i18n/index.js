import { createI18n } from 'vue-i18n'
import id from './locales/id.js'
import en from './locales/en.js'

const savedLocale = localStorage.getItem('locale') || 'id'

const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: 'id',
  messages: { id, en }
})

export default i18n
