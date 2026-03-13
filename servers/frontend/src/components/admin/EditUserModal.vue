<template>
  <Transition name="modal">
    <div
      v-if="isOpen"
      class="modal-overlay"
      @click.self="$emit('close')"
    >
      <div class="modal-content max-w-lg max-h-[90vh] overflow-y-auto scrollbar-thin">
        <!-- Header -->
        <div class="sticky top-0 bg-white z-10 px-6 py-5 border-b border-slate-100">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div
                class="w-12 h-12 rounded-full flex items-center justify-center text-white font-bold"
                :class="getAvatarClass(user?.role)"
              >
                {{ getInitials(user?.username) }}
              </div>
              <div>
                <h2 class="text-lg font-bold text-slate-900">Edit User</h2>
                <p class="text-sm text-slate-500">{{ user?.username }}</p>
              </div>
            </div>
            <button
              @click="$emit('close')"
              class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
            >
              <i class="fas fa-times text-lg"></i>
            </button>
          </div>
        </div>

        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="p-6 space-y-6">
          <!-- Email Edit -->
          <div>
            <label class="label">
              <i class="fas fa-envelope mr-1.5 text-slate-400"></i>
              Email Address
            </label>
            <input
              v-model="editEmail"
              type="email"
              class="input"
              placeholder="user@example.com"
            />
          </div>

          <!-- Role Selection -->
          <div>
            <label class="label">
              <i class="fas fa-user-tag mr-1.5 text-slate-400"></i>
              Role
            </label>
            <div class="grid grid-cols-3 gap-3">
              <button
                v-for="role in availableRoles"
                :key="role.value"
                type="button"
                @click="selectedRole = role.value"
                class="p-3 rounded-xl border-2 transition-all text-center"
                :class="selectedRole === role.value
                  ? 'border-primary-500 bg-primary-50 text-primary-700'
                  : 'border-slate-200 hover:border-slate-300 text-slate-600'"
              >
                <i :class="role.icon" class="text-lg mb-1"></i>
                <p class="text-sm font-semibold">{{ role.label }}</p>
              </button>
            </div>
          </div>

          <!-- Service Selection -->
          <div v-if="selectedRole !== 'ADMIN'">
            <label class="label">
              <i class="fas fa-cogs mr-1.5 text-slate-400"></i>
              Service
            </label>
            <div class="grid grid-cols-2 gap-3">
              <button
                v-for="service in availableServices"
                :key="service.value"
                type="button"
                @click="selectedService = service.value"
                class="p-3 rounded-xl border-2 transition-all flex items-center gap-3"
                :class="selectedService === service.value
                  ? 'border-primary-500 bg-primary-50 text-primary-700'
                  : 'border-slate-200 hover:border-slate-300 text-slate-600'"
              >
                <i :class="service.icon" class="text-lg"></i>
                <span class="text-sm font-semibold">{{ service.label }}</span>
              </button>
            </div>
          </div>

          <!-- Resource Assignment -->
          <div v-if="selectedRole !== 'ADMIN' && selectedService === 'BAR'">
            <label class="label">
              <i class="fas fa-map-marker-alt mr-1.5 text-slate-400"></i>
              Assigned Bar (Optional)
            </label>
            <select v-model="selectedResource" class="input w-full">
              <option value="">No resource</option>
              <option v-for="resource in availableResources" :key="resource.value" :value="resource.value">
                {{ resource.label }}
              </option>
            </select>
            <p class="text-xs text-slate-500 mt-1">Leave empty to allow access to all bars</p>
          </div>

          <!-- Status Toggle -->
          <div class="flex items-center justify-between p-4 rounded-xl bg-slate-50 border border-slate-200">
            <div>
              <p class="text-sm font-semibold text-slate-700">Account Status</p>
              <p class="text-xs text-slate-500">
                {{ isEnabled ? 'User can log in' : 'User login disabled' }}
              </p>
            </div>
            <button
              type="button"
              @click="isEnabled = !isEnabled"
              class="relative w-14 h-8 rounded-full transition-colors"
              :class="isEnabled ? 'bg-emerald-500' : 'bg-slate-300'"
            >
              <span
                class="absolute top-1 w-6 h-6 bg-white rounded-full shadow transition-transform"
                :class="isEnabled ? 'left-7' : 'left-1'"
              ></span>
            </button>
          </div>

          <!-- Error Message -->
          <div v-if="errorMessage" class="p-4 rounded-xl bg-red-50 border border-red-200">
            <p class="text-sm text-red-600">
              <i class="fas fa-exclamation-circle mr-2"></i>
              {{ errorMessage }}
            </p>
          </div>

          <!-- Primary Actions -->
          <div class="flex items-center gap-3 pt-2">
            <button
              type="button"
              @click="$emit('close')"
              class="btn-secondary flex-1"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="btn-primary flex-1"
              :disabled="saving"
            >
              <template v-if="saving">
                <i class="fas fa-spinner fa-spin"></i>
                <span>Saving...</span>
              </template>
              <template v-else>
                <i class="fas fa-save"></i>
                <span>Save Changes</span>
              </template>
            </button>
          </div>

          <!-- Danger Zone -->
          <div class="border-t border-slate-200 pt-5">
            <button
              type="button"
              @click="showDangerZone = !showDangerZone"
              class="flex items-center gap-2 text-sm text-slate-400 hover:text-red-500 transition-colors w-full"
            >
              <i class="fas fa-exclamation-triangle"></i>
              <span class="font-medium">Danger Zone</span>
              <i :class="showDangerZone ? 'fas fa-chevron-up' : 'fas fa-chevron-down'" class="ml-auto text-xs"></i>
            </button>

            <Transition name="slide">
              <div v-if="showDangerZone" class="mt-4 p-4 rounded-xl border-2 border-red-200 bg-red-50/50 space-y-3">
                <button
                  type="button"
                  @click="$emit('reset-password', user)"
                  class="w-full px-4 py-2.5 rounded-lg border border-amber-200 bg-white text-sm font-medium text-amber-700 hover:bg-amber-50 transition-colors flex items-center gap-2"
                >
                  <i class="fas fa-key"></i>
                  Reset Password
                </button>
                <button
                  type="button"
                  @click="$emit('delete', user)"
                  class="w-full px-4 py-2.5 rounded-lg border border-red-200 bg-white text-sm font-medium text-red-600 hover:bg-red-50 transition-colors flex items-center gap-2"
                >
                  <i class="fas fa-trash-alt"></i>
                  Delete Account
                </button>
              </div>
            </Transition>
          </div>
        </form>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { authApi, barApi } from '@/api';
import { availableRoles, availableServices, getInitials, getAvatarClass } from '@/composables/useRoleStyling';

const props = defineProps({
  isOpen: Boolean,
  user: Object,
});

const emit = defineEmits(['close', 'updated', 'reset-password', 'delete']);

const editEmail = ref('');
const selectedRole = ref('');
const selectedService = ref('');
const selectedResource = ref('');
const isEnabled = ref(true);
const saving = ref(false);
const errorMessage = ref('');
const bars = ref([]);
const showDangerZone = ref(false);

const availableResources = computed(() => {
  if (selectedService.value !== 'BAR') return [];
  return (bars.value || []).map((bar) => ({ value: bar.id, label: bar.name }));
});

async function loadResources() {
  try {
    const [barsResponse] = await Promise.all([barApi.getBars()]);
    bars.value = barsResponse.data || [];
  } catch (e) {
    console.error('Failed to load resources:', e);
  }
}

watch(() => props.user, (newUser) => {
  if (newUser) {
    const assignments = newUser._raw?.assignments || [];
    const primary = assignments[0] || null;

    editEmail.value = newUser.email || newUser._raw?.email || '';

    const hasAdmin = assignments.some(a => String(a?.role || '').toUpperCase().includes('ADMIN'));
    if (hasAdmin) {
      selectedRole.value = 'ADMIN';
      selectedService.value = '';
    } else if (primary) {
      const roleName = String(primary.role || '').toUpperCase();
      if (roleName.includes('MANAGER')) selectedRole.value = 'MANAGER';
      else if (roleName.includes('BARTENDER') || roleName.includes('OPERATOR')) selectedRole.value = 'OPERATOR';
      else selectedRole.value = '';

      const serviceCode = String(primary.service || '').toUpperCase();
      if (serviceCode.includes('BAR')) selectedService.value = 'BAR';
      else if (serviceCode.includes('EVENT')) selectedService.value = 'EVENT';
      else if (serviceCode.includes('DROP_POINT')) selectedService.value = 'DROP_POINT';
      else if (serviceCode.includes('WAREHOUSE')) selectedService.value = 'WAREHOUSE';
      else selectedService.value = '';

      selectedResource.value = primary.resourceId || '';
    } else {
      selectedRole.value = '';
      selectedService.value = '';
      selectedResource.value = '';
    }

    isEnabled.value = newUser._raw?.enabled ?? true;
    errorMessage.value = '';
    showDangerZone.value = false;

    loadResources();
  }
}, { immediate: true });

async function handleSubmit() {
  if (!props.user) return;

  saving.value = true;
  errorMessage.value = '';

  try {
    const rawUser = props.user._raw;

    let newAssignments = [];

    if (selectedRole.value === 'ADMIN') {
      newAssignments = [{ roleName: 'ADMIN', serviceCode: null, resourceId: null }];
    } else if (selectedRole.value && selectedService.value) {
      const resourceId = selectedService.value === 'BAR' ? (selectedResource.value || null) : null;
      newAssignments = [{ roleName: selectedRole.value, serviceCode: selectedService.value, resourceId }];
    }

    await authApi.updateUser(rawUser.id, {
      id: rawUser.id,
      username: rawUser.username,
      email: editEmail.value || rawUser.email,
      enabled: isEnabled.value,
    });

    await authApi.setUserAssignments(rawUser.id, newAssignments);

    emit('updated');
  } catch (e) {
    errorMessage.value = e?.message ?? 'Failed to update user';
  } finally {
    saving.value = false;
  }
}
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

.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  max-height: 0;
  overflow: hidden;
}

.slide-enter-to,
.slide-leave-from {
  max-height: 200px;
}
</style>
