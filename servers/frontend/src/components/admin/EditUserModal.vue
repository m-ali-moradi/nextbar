<template>
  <Transition name="modal">
    <div
      v-if="isOpen"
      class="modal-overlay"
      @click.self="$emit('close')"
    >
      <div class="modal-content max-w-lg">
        <!-- Header -->
        <div class="flex items-center justify-between mb-6">
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 rounded-xl bg-admin-100 flex items-center justify-center">
              <i class="fas fa-user-edit text-xl text-admin-600"></i>
            </div>
            <div>
              <h2 class="text-xl font-bold text-slate-900">Edit User</h2>
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

        <!-- Form -->
        <form @submit.prevent="handleSubmit" class="space-y-5">
          <!-- User Info (Read Only) -->
          <div class="p-4 rounded-xl bg-slate-50 border border-slate-200">
            <div class="grid grid-cols-2 gap-4">
              <div>
                <p class="text-xs text-slate-500 font-medium mb-1">Username</p>
                <p class="text-base font-semibold text-slate-900">{{ user?.username }}</p>
              </div>
              <div>
                <p class="text-xs text-slate-500 font-medium mb-1">Email</p>
                <p class="text-base text-slate-700">{{ user?.email || '—' }}</p>
              </div>
            </div>
          </div>

          <!-- Role Selection -->
          <div>
            <label class="block text-sm font-semibold text-slate-700 mb-2">
              <i class="fas fa-user-tag mr-2 text-slate-400"></i>
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
                  ? 'border-admin-500 bg-admin-50 text-admin-700' 
                  : 'border-slate-200 hover:border-slate-300 text-slate-600'"
              >
                <i :class="role.icon" class="text-lg mb-1"></i>
                <p class="text-sm font-semibold">{{ role.label }}</p>
              </button>
            </div>
          </div>

          <!-- Service Selection (only for non-admin) -->
          <div v-if="selectedRole !== 'ADMIN'">
            <label class="block text-sm font-semibold text-slate-700 mb-2">
              <i class="fas fa-cogs mr-2 text-slate-400"></i>
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

          <!-- Resource Assignment (only for OPERATOR role and BAR service) -->
          <div v-if="selectedRole === 'OPERATOR' && selectedService === 'BAR'">
            <label class="block text-sm font-semibold text-slate-700 mb-2">
              <i class="fas fa-map-marker-alt mr-2 text-slate-400"></i>
              Assigned Bar
            </label>
            <select v-model="selectedResource" class="input w-full">
              <option value="">Select a bar...</option>
              <option v-for="bar in bars" :key="bar.id" :value="bar.id">
                {{ bar.name }}
              </option>
            </select>
            <p class="text-xs text-slate-500 mt-1">
              Leave empty to allow access to all bars
            </p>
          </div>

          <!-- Status Toggle -->
          <div class="flex items-center justify-between p-4 rounded-xl bg-slate-50 border border-slate-200">
            <div>
              <p class="text-sm font-semibold text-slate-700">Account Status</p>
              <p class="text-xs text-slate-500">Enable or disable this account</p>
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

          <!-- Actions -->
          <div class="flex items-center gap-3 pt-4">
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
        </form>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { authApi, barApi } from '@/api';

const props = defineProps({
  isOpen: Boolean,
  user: Object,
});

const emit = defineEmits(['close', 'updated']);

// Available options based on DummyUsersInitializer
const availableRoles = [
  { value: 'ADMIN', label: 'Admin', icon: 'fas fa-user-shield text-violet-500' },
  { value: 'MANAGER', label: 'Manager', icon: 'fas fa-user-tie text-blue-500' },
  { value: 'OPERATOR', label: 'Operator', icon: 'fas fa-user text-emerald-500' },
];

const availableServices = [
  { value: 'BAR', label: 'Bar', icon: 'fas fa-glass-martini text-bar-500' },
  { value: 'EVENT', label: 'Event', icon: 'fas fa-calendar text-event-500' },
  { value: 'DROP_POINT', label: 'Drop Point', icon: 'fas fa-map-marker-alt text-droppoint-500' },
  { value: 'WAREHOUSE', label: 'Warehouse', icon: 'fas fa-warehouse text-warehouse-500' },
];

// Form state
const selectedRole = ref('');
const selectedService = ref('');
const selectedResource = ref('');
const isEnabled = ref(true);
const saving = ref(false);
const errorMessage = ref('');
const bars = ref([]);

// Load bars for resource dropdown
async function loadBars() {
  try {
    const response = await barApi.getBars();
    bars.value = response.data || [];
  } catch (e) {
    console.error('Failed to load bars:', e);
  }
}

// Map display role to DB role name
function getRoleNameForDb(role, serviceCode) {
  if (role === 'ADMIN') return 'ADMIN';
  
  const servicePrefix = serviceCode || 'BAR';
  
  if (role === 'MANAGER') {
    return `${servicePrefix}_MANAGER`;
  }
  
  if (role === 'OPERATOR') {
    if (servicePrefix === 'BAR') return 'BARTENDER';
    return `${servicePrefix}_OPERATOR`;
  }
  
  return role;
}

// Map display service to DB service code
function getServiceCodeForDb(service) {
  if (!service) return 'BAR_SERVICE';
  return `${service}_SERVICE`;
}

// Watch for user changes to initialize form
watch(() => props.user, (newUser) => {
  if (newUser) {
    // Extract current values from user
    const assignments = newUser._raw?.assignments || [];
    const primary = assignments[0] || null;
    
    // Determine role from assignments
    const hasAdmin = assignments.some(a => String(a?.role || '').toUpperCase().includes('ADMIN'));
    if (hasAdmin) {
      selectedRole.value = 'ADMIN';
      selectedService.value = '';
    } else if (primary) {
      const roleName = String(primary.role || '').toUpperCase();
      if (roleName.includes('MANAGER')) {
        selectedRole.value = 'MANAGER';
      } else if (roleName.includes('BARTENDER') || roleName.includes('OPERATOR')) {
        selectedRole.value = 'OPERATOR';
      } else {
        selectedRole.value = '';
      }
      
      // Extract service
      const serviceCode = String(primary.service || '').toUpperCase();
      if (serviceCode.includes('BAR')) selectedService.value = 'BAR';
      else if (serviceCode.includes('EVENT')) selectedService.value = 'EVENT';
      else if (serviceCode.includes('DROP_POINT')) selectedService.value = 'DROP_POINT';
      else if (serviceCode.includes('WAREHOUSE')) selectedService.value = 'WAREHOUSE';
      else selectedService.value = '';
      
      // Extract resource
      selectedResource.value = primary.resourceId || '';
    } else {
      selectedRole.value = '';
      selectedService.value = '';
      selectedResource.value = '';
    }
    
    isEnabled.value = newUser._raw?.enabled ?? true;
    errorMessage.value = '';
    
    // Load bars for dropdown
    loadBars();
  }
}, { immediate: true });

async function handleSubmit() {
  if (!props.user) return;
  
  saving.value = true;
  errorMessage.value = '';
  
  try {
    const rawUser = props.user._raw;
    
    // Build new assignments array
    let newAssignments = [];
    
    if (selectedRole.value === 'ADMIN') {
      // Admin gets all services
      newAssignments = [
        { role: 'ADMIN', service: 'BAR_SERVICE', resourceId: null },
        { role: 'ADMIN', service: 'EVENT_SERVICE', resourceId: null },
        { role: 'ADMIN', service: 'DROP_POINT_SERVICE', resourceId: null },
      ];
    } else if (selectedRole.value && selectedService.value) {
      const roleName = getRoleNameForDb(selectedRole.value, selectedService.value);
      const serviceCode = getServiceCodeForDb(selectedService.value);
      const resourceId = selectedRole.value === 'OPERATOR' && selectedResource.value 
        ? selectedResource.value 
        : null;
      
      newAssignments = [{
        role: roleName,
        service: serviceCode,
        resourceId: resourceId,
      }];
    }
    
    // Update user with new data
    await authApi.updateUser(rawUser.id, {
      id: rawUser.id,
      username: rawUser.username,
      email: rawUser.email,
      enabled: isEnabled.value,
      assignments: newAssignments,
    });
    
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
</style>
