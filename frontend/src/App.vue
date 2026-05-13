<template>
  <div id="app">
    <Navbar v-if="authStore.isAuthenticated" />
    <main
      class="main-content"
      :class="{ 'with-nav': authStore.isAuthenticated }"
    >
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { onMounted } from "vue";
import { RouterView } from "vue-router";
import Navbar from "@/components/Navbar.vue";
import { useAuthStore } from "@/stores/authStore.js";
import { useTheme } from "@/composables/useTheme.js";

const authStore = useAuthStore();
// Initialize theme on app load
useTheme();

onMounted(() => {
  authStore.initAuth();
});
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
#app {
  min-height: 100vh;
  background-color: var(--bg-body);
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  color: var(--text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
}
.main-content.with-nav {
  padding: 80px 30px 10px 30px;
}
</style>
