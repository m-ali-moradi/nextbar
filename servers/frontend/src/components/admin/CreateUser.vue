<template>
  <div class="p-6">
    <div class="flex items-center gap-3 mb-6">
      <div class="w-10 h-10 rounded-xl bg-admin-100 flex items-center justify-center">
        <i class="fas fa-user-plus text-admin-600"></i>
      </div>
      <div>
        <h2 class="text-lg font-semibold text-slate-900">Create New User</h2>
        <p class="text-sm text-slate-500">Add a new user to the system</p>
      </div>
    </div>

    <Transition name="fade">
      <div 
        v-if="error" 
        class="mb-6 px-4 py-3 rounded-xl bg-red-50 border border-red-100 
               flex items-center gap-3 text-red-700"
      >
        <i class="fas fa-exclamation-circle"></i>
        <span class="text-sm">{{ error }}</span>
      </div>
    </Transition>

    <form @submit.prevent="handleSubmit" class="space-y-5">
      <!-- Basic User Info -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label for="username" class="label">Username</label>
          <input
            v-model="form.username"
            id="username"
            type="text"
            class="input"
            placeholder="Enter username"
            required
          />
        </div>

        <div>
          <label for="email" class="label">Email</label>
          <input
            v-model="form.email"
            id="email"
            type="email"
            class="input"
            placeholder="user@example.com"
            required
          />
        </div>
      </div>

      <div>
        <label for="password" class="label">Password</label>
        <input
          v-model="form.password"
          id="password"
          type="password"
          class="input"
          placeholder="Create a password"
          required
        />
      </div>

      <!-- Role & Service -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label for="role" class="label">Role</label>
          <select
            v-model="form.role"
            id="role"
            class="input"
            required
          >
            <option value="" disabled>Select Role</option>
            <option value="ADMIN">Administrator</option>
            <option value="MANAGER">Manager</option>
            <option value="BARTENDER">Operator</option>
          </select>
        </div>

        <div>
          <label for="service" class="label">Service</label>
          <select
            v-model="form.service"
            id="service"
            class="input"
            required
          >
            <option value="" disabled>Select Service</option>
            <option value="BAR_SERVICE">Bar</option>
            <option value="DROP_POINT">Drop Point</option>
            <option value="WAREHOUSE">Warehouse</option>
            <option value="EVENT">Event</option>
          </select>
        </div>
      </div>

      <!-- Permissions -->
      <div>
        <label class="label">Permissions</label>
        <div class="grid grid-cols-2 md:grid-cols-3 gap-2">
          <label
            v-for="perm in ['CREATE', 'VIEW', 'EDIT', 'DELETE', 'OPERATE']"
            :key="perm"
            class="flex items-center gap-2 p-3 bg-slate-50 rounded-lg cursor-pointer
                   hover:bg-slate-100 transition-colors"
            :class="{ 'bg-primary-50 border-primary-200': form.permissions.includes(perm) }"
          >
            <input
              type="checkbox"
              :value="perm"
              v-model="form.permissions"
              class="w-4 h-4 rounded border-slate-300 text-primary-600 
                     focus:ring-primary-500 focus:ring-offset-0"
            />
            <span class="text-sm font-medium text-slate-700">{{ perm }}</span>
          </label>
        </div>
      </div>

      <!-- Assigned Resource (for non-admin/manager) -->
      <div v-if="form.role && form.role !== 'ADMIN' && form.role !== 'MANAGER'">
        <label for="assignedTo" class="label">Assign To Resource</label>
        <select
          v-model="form.assignedTo"
          id="assignedTo"
          class="input"
          required
        >
          <option value="" disabled>Select Resource</option>
          <option value="22222222-2222-2222-2222-222222222222">Bar 1 (demo)</option>
        </select>
        <p class="text-xs text-slate-500 mt-1.5">
          Operators must be assigned to a specific resource
        </p>
      </div>

      <!-- Actions -->
      <div class="flex gap-3 pt-4 border-t border-slate-100">
        <button
          type="submit"
          class="btn-primary flex-1"
          :disabled="loading"
        >
          <template v-if="loading">
            <i class="fas fa-spinner fa-spin"></i>
            <span>Creating...</span>
          </template>
          <template v-else>
            <i class="fas fa-user-plus"></i>
            <span>Create User</span>
          </template>
        </button>
        <button
          type="button"
          class="btn-secondary flex-1"
          @click="emit('cancel')"
          :disabled="loading"
        >
          Cancel
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue';
import { authApi } from '@/api';

const emit = defineEmits(['created', 'cancel']);

const loading = ref(false);
const error = ref('');

const form = reactive({
  username: '',
  email: '',
  password: '',
  role: '',
  service: '',
  permissions: [],
  assignedTo: '',
});

watch(
  () => form.role,
  () => {
    // For admin/manager, resource scoping is not required.
    if (form.role === 'ADMIN' || form.role === 'MANAGER') {
      form.assignedTo = '';
    }
  }
);

async function handleSubmit() {
  loading.value = true;
  error.value = '';

  try {
    const resourceId =
      form.role !== 'ADMIN' && form.role !== 'MANAGER'
        ? (form.assignedTo || null)
        : null;

    await authApi.createUser({
      username: form.username,
      email: form.email,
      password: form.password,
      roleName: form.role,
      serviceCode: form.service,
      resourceId,
    });

    emit('created');
  } catch (e) {
    error.value = e?.message ?? 'Failed to create user';
  } finally {
    loading.value = false;
  }
}
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
