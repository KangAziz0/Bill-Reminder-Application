<template>
  <div id="app">
    <Navbar v-if="authStore.isAuthenticated" />
    <main class="main-content" :class="{ 'with-nav': authStore.isAuthenticated }">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import { useAuthStore } from '@/stores/authStore.js'

const authStore = useAuthStore()

onMounted(() => {
  authStore.initAuth()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
#app {
  min-height: 100vh;
  background-color: #f0f2f5;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}
.main-content {
  padding: 20px;
}
.main-content.with-nav {
  padding-top: 80px;
}
</style>
