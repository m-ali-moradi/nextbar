<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
      <h2 class="text-2xl font-bold text-admin-primary mb-4">Login</h2>
      <div v-if="authStore.loading" class="text-center">Loading...</div>
      <div v-if="authStore.error" class="text-red-500 mb-4">{{ authStore.error }}</div>
      <form @submit.prevent="handleLogin">
        <div class="mb-4">
          <label class="block text-admin-primary mb-2" for="username">Username</label>
          <input
            v-model="username"
            type="text"
            id="username"
            class="w-full p-2 border rounded"
            required
            aria-label="Username"
          />
        </div>
        <div class="mb-4">
          <label class="block text-admin-primary mb-2" for="password">Password</label>
          <input
            v-model="password"
            type="password"
            id="password"
            class="w-full p-2 border rounded"
            required
            aria-label="Password"
          />
        </div>
        <button
          type="submit"
          class="w-full bg-admin-primary text-white p-2 rounded hover:bg-admin-secondary"
          :disabled="authStore.loading"
        >
          Login
        </button>
      </form>
      <p class="mt-4 text-center">
        Don't have an account? <router-link to="/register" class="text-admin-primary">Register</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();
const router = useRouter();
const username = ref('');
const password = ref('');

const handleLogin = async () => {
  await authStore.login(username.value, password.value);
  if (authStore.token) {
    router.push('/bars');
  }
};
</script>

<style scoped>
/* Tailwind handles styling */
</style>