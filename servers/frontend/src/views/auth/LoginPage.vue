<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-100 via-slate-50 to-primary-50 p-4">
    <!-- Decorative Background Elements -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-80 h-80 bg-primary-200 rounded-full opacity-30 blur-3xl"></div>
      <div class="absolute -bottom-40 -left-40 w-80 h-80 bg-bar-200 rounded-full opacity-30 blur-3xl"></div>
    </div>

    <!-- Login Card -->
    <div class="relative w-full max-w-md">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-20 h-20 rounded-2xl 
                    bg-gradient-to-br from-primary-500 to-primary-600 shadow-xl shadow-primary-500/30 mb-4">
          <i class="fas fa-glass-martini-alt text-2xl text-white"></i>
        </div>
        <h1 class="text-2xl font-bold text-slate-900">Welcome back</h1>
        <p class="text-slate-500 mt-1">Sign in to your NextBar account</p>
      </div>

      <!-- Card -->
      <div class="bg-white/80 backdrop-blur-xl rounded-2xl shadow-xl shadow-slate-200/50 
                  border border-white/50 p-8">
        
        <!-- Error Message -->
        <Transition name="fade">
          <div 
            v-if="authStore.error" 
            class="mb-6 px-4 py-3 rounded-xl bg-red-50 border border-red-100 
                   flex items-center gap-3 text-red-700"
          >
            <i class="fas fa-exclamation-circle"></i>
            <span class="text-sm">{{ authStore.error }}</span>
          </div>
        </Transition>

        <!-- Form -->
        <form @submit.prevent="handleLogin" class="space-y-5">
          <!-- Username Field -->
          <div>
            <label for="username" class="label">Username</label>
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">
                <i class="fas fa-user"></i>
              </span>
              <input
                v-model="username"
                type="text"
                id="username"
                class="input pl-11"
                placeholder="Enter your username"
                required
                autocomplete="username"
              />
            </div>
          </div>

          <!-- Password Field -->
          <div>
            <div class="flex items-center justify-between mb-1.5">
              <label for="password" class="label mb-0">Password</label>
              <a href="#" class="text-xs text-primary-600 hover:text-primary-700 font-medium">
                Forgot password?
              </a>
            </div>
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">
                <i class="fas fa-lock"></i>
              </span>
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                id="password"
                class="input pl-11 pr-11"
                placeholder="Enter your password"
                required
                autocomplete="current-password"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 
                       hover:text-slate-600 transition-colors"
              >
                <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
              </button>
            </div>
          </div>

          <!-- Remember Me -->
          <div class="flex items-center gap-2">
            <input
              type="checkbox"
              id="remember"
              v-model="rememberMe"
              class="w-4 h-4 rounded border-slate-300 text-primary-600 
                     focus:ring-primary-500 focus:ring-offset-0"
            />
            <label for="remember" class="text-sm text-slate-600">
              Remember me for 30 days
            </label>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="authStore.loading"
            class="w-full btn-primary h-12 text-base"
          >
            <template v-if="authStore.loading">
              <i class="fas fa-spinner fa-spin"></i>
              <span>Signing in...</span>
            </template>
            <template v-else>
              <span>Sign in</span>
              <i class="fas fa-arrow-right"></i>
            </template>
          </button>
        </form>
      </div>
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
const showPassword = ref(false);
const rememberMe = ref(false);

const handleLogin = async () => {
  await authStore.login(username.value, password.value);
  if (authStore.token) {
    router.push(authStore.getDefaultRoute());
  }
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>