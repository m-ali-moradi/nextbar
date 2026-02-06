<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-100 via-slate-50 to-primary-50 p-4">
    <!-- Decorative Background Elements -->
    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-40 -right-40 w-80 h-80 bg-event-200 rounded-full opacity-30 blur-3xl"></div>
      <div class="absolute -bottom-40 -left-40 w-80 h-80 bg-primary-200 rounded-full opacity-30 blur-3xl"></div>
    </div>

    <!-- Register Card -->
    <div class="relative w-full max-w-md">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl 
                    bg-gradient-to-br from-primary-500 to-primary-600 shadow-xl shadow-primary-500/30 mb-4">
          <i class="fas fa-glass-martini-alt text-2xl text-white"></i>
        </div>
        <h1 class="text-2xl font-bold text-slate-900">Create an account</h1>
        <p class="text-slate-500 mt-1">Join NextBar to manage your events</p>
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

        <!-- Success Message -->
        <Transition name="fade">
          <div 
            v-if="successMessage" 
            class="mb-6 px-4 py-3 rounded-xl bg-emerald-50 border border-emerald-100 
                   flex items-center gap-3 text-emerald-700"
          >
            <i class="fas fa-check-circle"></i>
            <span class="text-sm">{{ successMessage }}</span>
          </div>
        </Transition>

        <!-- Form -->
        <form @submit.prevent="handleRegister" class="space-y-5">
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
                placeholder="Choose a username"
                required
                autocomplete="username"
              />
            </div>
          </div>

          <!-- Email Field -->
          <div>
            <label for="email" class="label">Email</label>
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">
                <i class="fas fa-envelope"></i>
              </span>
              <input
                v-model="email"
                type="email"
                id="email"
                class="input pl-11"
                placeholder="Enter your email"
                required
                autocomplete="email"
              />
            </div>
          </div>

          <!-- Password Field -->
          <div>
            <label for="password" class="label">Password</label>
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">
                <i class="fas fa-lock"></i>
              </span>
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                id="password"
                class="input pl-11 pr-11"
                placeholder="Create a password"
                required
                minlength="6"
                autocomplete="new-password"
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
            <p class="text-xs text-slate-500 mt-1.5">Must be at least 6 characters</p>
          </div>

          <!-- Confirm Password Field -->
          <div>
            <label for="confirmPassword" class="label">Confirm Password</label>
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">
                <i class="fas fa-lock"></i>
              </span>
              <input
                v-model="confirmPassword"
                :type="showPassword ? 'text' : 'password'"
                id="confirmPassword"
                class="input pl-11"
                placeholder="Confirm your password"
                required
                minlength="6"
                autocomplete="new-password"
              />
            </div>
            <p 
              v-if="confirmPassword && password !== confirmPassword" 
              class="text-xs text-red-500 mt-1.5"
            >
              Passwords do not match
            </p>
          </div>

          <!-- Terms -->
          <div class="flex items-start gap-2">
            <input
              type="checkbox"
              id="terms"
              v-model="acceptTerms"
              required
              class="mt-0.5 w-4 h-4 rounded border-slate-300 text-primary-600 
                     focus:ring-primary-500 focus:ring-offset-0"
            />
            <label for="terms" class="text-sm text-slate-600">
              I agree to the 
              <a href="#" class="text-primary-600 hover:text-primary-700 font-medium">Terms of Service</a>
              and 
              <a href="#" class="text-primary-600 hover:text-primary-700 font-medium">Privacy Policy</a>
            </label>
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            :disabled="authStore.loading || password !== confirmPassword"
            class="w-full btn-primary h-12 text-base"
          >
            <template v-if="authStore.loading">
              <i class="fas fa-spinner fa-spin"></i>
              <span>Creating account...</span>
            </template>
            <template v-else>
              <span>Create account</span>
              <i class="fas fa-arrow-right"></i>
            </template>
          </button>
        </form>
      </div>

      <!-- Login Link -->
      <p class="text-center mt-6 text-slate-600">
        Already have an account?
        <router-link 
          to="/login" 
          class="text-primary-600 hover:text-primary-700 font-semibold ml-1"
        >
          Sign in
        </router-link>
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
const confirmPassword = ref('');
const showPassword = ref(false);
const acceptTerms = ref(false);
const successMessage = ref('');

const handleRegister = async () => {
  if (password.value !== confirmPassword.value) {
    return;
  }
  
  try {
    await authStore.register(username.value, email.value, password.value);
    if (!authStore.error) {
      successMessage.value = 'Account created successfully! Redirecting to login...';
      setTimeout(() => {
        router.push('/login');
      }, 2000);
    }
  } catch (error) {
    console.error('Registration error:', error);
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