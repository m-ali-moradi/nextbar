<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="bg-white p-6 rounded-lg shadow-lg w-full max-w-md">
      <h2 class="text-2xl font-bold text-admin-primary mb-4">Register</h2>
      <div v-if="authStore.loading" class="text-center">Loading...</div>
      <div v-if="authStore.error" class="text-red-500 mb-4">{{ authStore.error }}</div>
      <form @submit.prevent="handleRegister">
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
          <label class="block text-admin-primary mb-2" for="email">Email</label>
          <input
            v-model="email"
            type="email"
            id="email"
            class="w-full p-2 border rounded"
            required
            aria-label="Email"
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
          Register
        </button>
      </form>
      <p class="mt-4 text-center">
        Already have an account? <router-link to="/login" class="text-admin-primary">Login</router-link>
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
const email = ref('');
const password = ref('');

const handleRegister = async () => {
  await authStore.register(username.value, password.value, email.value);
  if (authStore.token) {
    router.push('/bars');
  }
};
</script>

<style scoped>
/* Tailwind handles styling */
</style>