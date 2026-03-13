<template>
  <Transition name="modal">
    <div
      v-if="isOpen"
      class="modal-overlay"
      @click.self="handleClose"
    >
      <div class="modal-content max-w-md">
        <div class="p-6">
          <!-- Header -->
          <div class="flex items-center gap-3 mb-6">
            <div class="w-11 h-11 rounded-xl bg-amber-100 flex items-center justify-center">
              <i class="fas fa-key text-lg text-amber-600"></i>
            </div>
            <div class="flex-1 min-w-0">
              <h2 class="text-lg font-bold text-slate-900">Reset Password</h2>
              <p class="text-sm text-slate-500 truncate">
                for <span class="font-medium text-slate-700">{{ username }}</span>
              </p>
            </div>
            <button
              @click="handleClose"
              class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>

          <!-- Error -->
          <div
            v-if="errorMessage"
            class="mb-4 px-4 py-3 rounded-xl bg-red-50 border border-red-100 flex items-center gap-2 text-red-700 text-sm"
          >
            <i class="fas fa-exclamation-circle"></i>
            <span>{{ errorMessage }}</span>
          </div>

          <!-- Form -->
          <form @submit.prevent="handleSubmit" class="space-y-4">
            <!-- New Password -->
            <div>
              <label class="label">New Password</label>
              <div class="relative">
                <input
                  ref="passwordInput"
                  v-model="newPassword"
                  :type="showPassword ? 'text' : 'password'"
                  class="input pr-11"
                  placeholder="Enter new password"
                  required
                  minlength="6"
                />
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                  tabindex="-1"
                >
                  <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                </button>
              </div>
              <!-- Strength Indicator -->
              <div class="mt-2" v-if="newPassword">
                <div class="flex gap-1 mb-1">
                  <div
                    v-for="i in 4"
                    :key="i"
                    class="h-1.5 flex-1 rounded-full transition-all duration-300"
                    :class="i <= passwordStrength.level
                      ? passwordStrength.color
                      : 'bg-slate-200'"
                  ></div>
                </div>
                <p
                  class="text-xs font-medium"
                  :class="passwordStrength.textColor"
                >
                  {{ passwordStrength.label }}
                </p>
              </div>
            </div>

            <!-- Confirm Password -->
            <div>
              <label class="label">Confirm Password</label>
              <div class="relative">
                <input
                  v-model="confirmPassword"
                  :type="showConfirm ? 'text' : 'password'"
                  class="input pr-11"
                  :class="confirmPassword && confirmPassword !== newPassword
                    ? 'border-red-300 focus:ring-red-500 focus:border-red-500'
                    : confirmPassword && confirmPassword === newPassword
                      ? 'border-emerald-300 focus:ring-emerald-500 focus:border-emerald-500'
                      : ''"
                  placeholder="Confirm new password"
                  required
                />
                <button
                  type="button"
                  @click="showConfirm = !showConfirm"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                  tabindex="-1"
                >
                  <i :class="showConfirm ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                </button>
              </div>
              <p
                v-if="confirmPassword && confirmPassword !== newPassword"
                class="text-xs text-red-500 mt-1"
              >
                Passwords do not match
              </p>
              <p
                v-else-if="confirmPassword && confirmPassword === newPassword"
                class="text-xs text-emerald-600 mt-1"
              >
                <i class="fas fa-check mr-1"></i> Passwords match
              </p>
            </div>

            <!-- Actions -->
            <div class="flex items-center gap-3 pt-3 border-t border-slate-100">
              <button
                type="button"
                class="btn-secondary flex-1"
                @click="handleClose"
                :disabled="saving"
              >
                Cancel
              </button>
              <button
                type="submit"
                class="btn-warning flex-1"
                :disabled="saving || !isFormValid"
              >
                <template v-if="saving">
                  <i class="fas fa-spinner fa-spin"></i>
                  <span>Resetting...</span>
                </template>
                <template v-else>
                  <i class="fas fa-key"></i>
                  <span>Reset Password</span>
                </template>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue';
import { usePasswordStrength } from '@/composables/usePasswordStrength';

const props = defineProps({
  isOpen: { type: Boolean, default: false },
  username: { type: String, default: '' },
});

const emit = defineEmits(['submit', 'close']);

const newPassword = ref('');
const confirmPassword = ref('');
const showPassword = ref(false);
const showConfirm = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const passwordInput = ref(null);

const { strength: passwordStrength } = usePasswordStrength(newPassword);

const isFormValid = computed(() => {
  return newPassword.value.length >= 6
    && confirmPassword.value === newPassword.value;
});

function resetForm() {
  newPassword.value = '';
  confirmPassword.value = '';
  showPassword.value = false;
  showConfirm.value = false;
  saving.value = false;
  errorMessage.value = '';
}

function handleClose() {
  if (!saving.value) {
    resetForm();
    emit('close');
  }
}

async function handleSubmit() {
  if (!isFormValid.value) return;

  saving.value = true;
  errorMessage.value = '';

  try {
    await new Promise((resolve, reject) => {
      emit('submit', {
        password: newPassword.value,
        resolve,
        reject,
      });
    });
    resetForm();
    emit('close');
  } catch (e) {
    errorMessage.value = e?.message || 'Failed to reset password';
  } finally {
    saving.value = false;
  }
}

// Auto-focus password input when modal opens
watch(() => props.isOpen, async (val) => {
  if (val) {
    resetForm();
    await nextTick();
    passwordInput.value?.focus();
  }
});
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.95);
}
</style>
