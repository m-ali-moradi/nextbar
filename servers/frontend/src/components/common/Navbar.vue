<template>
  <header class="bg-gray-800 text-white p-4 flex justify-between items-center">
    <div class="text-xl font-bold">Bottle Drop Dashboard</div>
    <div class="flex items-center">
      <span class="mr-4">{{ authStore.user?.username || 'Guest' }}</span>
      <button
        v-if="authStore.token"
        class="bg-admin-primary text-white px-4 py-2 rounded hover:bg-admin-secondary"
        @click="handleLogout"
      >
        Logout
      </button>
    </div>
  </header>
</template>

<script setup>
import { useAuthStore } from '@/stores/authStore';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
/* Tailwind handles styling */
</style>