<template>
  <!-- Error Banner -->
  <Transition name="fade">
    <div
      v-if="managementError"
      class="mb-6 px-4 py-3 rounded-xl bg-red-50 border border-red-100 flex items-center gap-3 text-red-700 text-sm"
    >
      <i class="fas fa-exclamation-circle"></i>
      <span class="flex-1">{{ managementError }}</span>
      <button @click="managementError = ''" class="text-red-400 hover:text-red-600">
        <i class="fas fa-times"></i>
      </button>
    </div>
  </Transition>

  <!-- Lists Section -->
  <div class="grid grid-cols-1 xl:grid-cols-2 gap-6 mb-8">
    <!-- Roles List -->
    <div class="card overflow-hidden">
      <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <h3 class="font-semibold text-slate-900">Roles</h3>
          <span class="badge badge-neutral text-xs">{{ roles.length }}</span>
        </div>
        <button
          class="btn-secondary py-1.5 px-3 text-sm"
          @click="loadManagementData"
          :disabled="managementLoading"
        >
          <i class="fas fa-sync-alt text-xs"></i>
        </button>
      </div>
      <div class="divide-y divide-slate-100 max-h-80 overflow-auto scrollbar-thin">
        <div
          v-for="role in roles"
          :key="role.id"
          class="px-6 py-4 flex items-center justify-between hover:bg-slate-50 transition-colors group"
        >
          <div class="flex items-center gap-3">
            <div
              class="w-9 h-9 rounded-lg flex items-center justify-center text-sm font-bold"
              :class="role.global
                ? 'bg-violet-100 text-violet-600'
                : 'bg-slate-100 text-slate-600'"
            >
              <i :class="role.global ? 'fas fa-globe' : 'fas fa-lock'"></i>
            </div>
            <div>
              <p class="font-medium text-slate-900 text-sm">{{ role.roleName }}</p>
              <p class="text-xs text-slate-500">
                {{ role.global ? 'Global' : 'Scoped' }}
                <span v-if="role.permissionCodes?.length"> · {{ role.permissionCodes.length }} permissions</span>
              </p>
            </div>
          </div>
        </div>
        <div v-if="roles.length === 0" class="px-6 py-8 text-center">
          <p class="text-sm text-slate-500">No roles defined yet</p>
        </div>
      </div>
    </div>

    <!-- Services List -->
    <div class="card overflow-hidden">
      <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
        <div class="flex items-center gap-2">
          <h3 class="font-semibold text-slate-900">Services</h3>
          <span class="badge badge-neutral text-xs">{{ services.length }}</span>
        </div>
        <button
          class="btn-secondary py-1.5 px-3 text-sm"
          @click="loadManagementData"
          :disabled="managementLoading"
        >
          <i class="fas fa-sync-alt text-xs"></i>
        </button>
      </div>
      <div class="divide-y divide-slate-100 max-h-80 overflow-auto scrollbar-thin">
        <div
          v-for="service in services"
          :key="service.id"
          class="px-6 py-4 flex items-center justify-between hover:bg-slate-50 transition-colors group"
        >
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-lg bg-blue-100 flex items-center justify-center">
              <i :class="getServiceIcon(service.code)" class="text-sm"></i>
            </div>
            <div>
              <p class="font-medium text-slate-900 text-sm">{{ service.code }}</p>
              <p class="text-xs text-slate-500">{{ service.description || 'No description' }}</p>
            </div>
          </div>
        </div>
        <div v-if="services.length === 0" class="px-6 py-8 text-center">
          <p class="text-sm text-slate-500">No services defined yet</p>
        </div>
      </div>
    </div>
  </div>

  <!-- Assign Role Section -->
  <div class="card overflow-hidden mb-8">
    <div class="px-6 py-4 border-b border-slate-100 flex items-center gap-3">
      <div class="w-9 h-9 rounded-lg bg-emerald-100 flex items-center justify-center">
        <i class="fas fa-user-plus text-emerald-600"></i>
      </div>
      <div>
        <h3 class="font-semibold text-slate-900">Assign Role to User</h3>
        <p class="text-xs text-slate-500">Bind a role and service scope to a user</p>
      </div>
    </div>
    <div class="p-6">
      <form @submit.prevent="handleAssignRole" class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <div>
          <label class="label">User</label>
          <select v-model="assignmentForm.userId" class="input" required>
            <option value="" disabled>Select user</option>
            <option v-for="user in users" :key="user.id" :value="user.id">{{ user.username }}</option>
          </select>
        </div>
        <div>
          <label class="label">Role</label>
          <select v-model="assignmentForm.roleName" class="input" required>
            <option value="" disabled>Select role</option>
            <option v-for="role in roles" :key="role.id" :value="role.roleName">{{ role.roleName }}</option>
          </select>
        </div>
        <div>
          <label class="label">Service</label>
          <select v-model="assignmentForm.serviceCode" class="input" required>
            <option value="" disabled>Select service</option>
            <option v-for="service in services" :key="service.id" :value="service.code">{{ service.code }}</option>
          </select>
        </div>
        <div>
          <label class="label">Resource ID</label>
          <input v-model="assignmentForm.resourceId" class="input" placeholder="UUID (optional)" />
        </div>
        <div class="sm:col-span-2 xl:col-span-4">
          <button class="btn-success w-full" type="submit" :disabled="managementLoading">
            <i class="fas fa-link"></i>
            Assign Role
          </button>
        </div>
      </form>
    </div>
  </div>

  <!-- Assignments By Role -->
  <div class="card overflow-hidden">
    <div class="px-6 py-4 border-b border-slate-100">
      <div class="flex flex-wrap items-center gap-3">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-lg bg-amber-100 flex items-center justify-center">
            <i class="fas fa-list text-amber-600"></i>
          </div>
          <h3 class="font-semibold text-slate-900">Assignments by Role</h3>
        </div>
        <select
          v-model="selectedRoleForAssignments"
          class="input py-2 max-w-xs"
          @change="loadRoleAssignments"
        >
          <option value="">Select a role to view</option>
          <option v-for="role in roles" :key="role.id" :value="role.roleName">{{ role.roleName }}</option>
        </select>
      </div>
    </div>

    <div v-if="!selectedRoleForAssignments" class="p-8 text-center">
      <div class="w-14 h-14 rounded-2xl bg-slate-100 flex items-center justify-center mx-auto mb-3">
        <i class="fas fa-arrow-up text-xl text-slate-400"></i>
      </div>
      <p class="text-sm text-slate-500">Select a role above to view its assignments</p>
    </div>

    <div v-else class="divide-y divide-slate-100 max-h-80 overflow-auto scrollbar-thin">
      <div
        v-for="a in roleAssignments"
        :key="a.assignmentId"
        class="px-6 py-4 flex items-center justify-between hover:bg-slate-50 transition-colors group"
      >
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-full bg-gradient-to-br from-primary-400 to-primary-600 flex items-center justify-center text-white font-bold text-xs">
            {{ a.username?.charAt(0)?.toUpperCase() || '?' }}
          </div>
          <div>
            <p class="font-medium text-slate-900 text-sm">
              {{ a.username }}
              <span class="text-slate-400 mx-1">·</span>
              <span class="text-primary-600">{{ a.roleName }}</span>
            </p>
            <p class="text-xs text-slate-500">
              {{ a.serviceCode }}
              <span v-if="a.resourceId"> · {{ a.resourceId.slice(0, 8) }}...</span>
              <span v-else> · Global resource</span>
            </p>
          </div>
        </div>
        <button
          class="text-sm text-slate-400 hover:text-red-600 transition-colors opacity-0 group-hover:opacity-100"
          @click="confirmRemoveAssignment(a)"
        >
          <i class="fas fa-unlink"></i>
        </button>
      </div>
      <div v-if="roleAssignments.length === 0" class="px-6 py-8 text-center">
        <p class="text-sm text-slate-500">No assignments for this role</p>
      </div>
    </div>
  </div>

  <!-- Confirm Dialog -->
  <ConfirmDialog
    :isOpen="confirmDialog.show"
    :title="confirmDialog.title"
    :message="confirmDialog.message"
    :confirmText="confirmDialog.confirmText"
    :variant="confirmDialog.variant"
    :loading="confirmDialog.loading"
    @confirm="confirmDialog.onConfirm"
    @cancel="confirmDialog.show = false"
  />
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import ConfirmDialog from '@/components/common/ConfirmDialog.vue';
import { authApi } from '@/api';
import { toast } from 'vue3-toastify';

const managementLoading = ref(false);
const managementError = ref('');

const roles = ref([]);
const services = ref([]);
const users = ref([]);
const selectedRoleForAssignments = ref('');
const roleAssignments = ref([]);


const assignmentForm = ref({
  userId: '',
  roleName: '',
  serviceCode: '',
  resourceId: '',
});

const confirmDialog = reactive({
  show: false,
  title: '',
  message: '',
  confirmText: 'Confirm',
  variant: 'danger',
  loading: false,
  onConfirm: () => {},
});

function getServiceIcon(code) {
  const icons = {
    'BAR': 'fas fa-glass-martini text-bar-500',
    'BAR_SERVICE': 'fas fa-glass-martini text-bar-500',
    'DROP_POINT': 'fas fa-map-marker-alt text-droppoint-500',
    'DROP_POINT_SERVICE': 'fas fa-map-marker-alt text-droppoint-500',
    'WAREHOUSE': 'fas fa-warehouse text-warehouse-500',
    'WAREHOUSE_SERVICE': 'fas fa-warehouse text-warehouse-500',
    'EVENT': 'fas fa-calendar text-event-500',
    'EVENT_SERVICE': 'fas fa-calendar text-event-500',
  };
  return icons[code] || 'fas fa-cube text-slate-500';
}

function parsePermissionCodes(value) {
  return String(value || '')
    .split(',')
    .map((v) => v.trim())
    .filter(Boolean);
}

function getApiError(e, fallbackMessage) {
  return e?.response?.data?.message || e?.message || fallbackMessage;
}

// Loads all management data (roles, services, users) from the API.
async function loadManagementData() {
  managementLoading.value = true;
  managementError.value = '';
  try {
    const [rolesResponse, servicesResponse, usersResponse] = await Promise.all([
      authApi.getRoles(),
      authApi.getServices(),
      authApi.getUsers(),
    ]);
    roles.value = rolesResponse?.data || [];
    services.value = servicesResponse?.data || [];
    const usersData = usersResponse?.data;
    users.value = Array.isArray(usersData)
      ? usersData
      : (usersData?.content ?? []);
  } catch (e) {
    managementError.value = getApiError(e, 'Failed to load management data');
    toast.error(managementError.value);
  } finally {
    managementLoading.value = false;
  }
}

// Handles the role assignment form submission.
async function handleAssignRole() {
  try {
    managementLoading.value = true;
    await authApi.assignRoleToUser({
      userId: assignmentForm.value.userId,
      roleName: assignmentForm.value.roleName,
      serviceCode: assignmentForm.value.serviceCode,
      resourceId: assignmentForm.value.resourceId || null,
    });
    const assignedRole = assignmentForm.value.roleName;
    assignmentForm.value = { userId: '', roleName: '', serviceCode: '', resourceId: '' };
    await loadManagementData();
    if (selectedRoleForAssignments.value) {
      await loadRoleAssignments();
    }
    toast.success(`Role ${assignedRole} assigned successfully`);
  } catch (e) {
    managementError.value = getApiError(e, 'Failed to assign role');
    toast.error(managementError.value);
  } finally {
    managementLoading.value = false;
  }
}

// Loads role assignments for a specific role.
async function loadRoleAssignments() {
  if (!selectedRoleForAssignments.value) {
    roleAssignments.value = [];
    return;
  }
  try {
    managementLoading.value = true;
    const response = await authApi.getRoleAssignments(selectedRoleForAssignments.value);
    roleAssignments.value = response?.data || [];
  } catch (e) {
    managementError.value = getApiError(e, 'Failed to load role assignments');
    roleAssignments.value = [];
    toast.error(managementError.value);
  } finally {
    managementLoading.value = false;
  }
}

// Confirms and removes a role assignment.
function confirmRemoveAssignment(assignment) {
  confirmDialog.title = 'Remove Assignment';
  confirmDialog.message = `Remove the "${assignment.roleName}" assignment from "${assignment.username}"?`;
  confirmDialog.confirmText = 'Remove';
  confirmDialog.variant = 'warning';
  confirmDialog.loading = false;
  confirmDialog.onConfirm = async () => {
    confirmDialog.loading = true;
    try {
      await authApi.removeAssignment(assignment.assignmentId);
      await loadRoleAssignments();
      toast.success('Assignment removed successfully');
    } catch (e) {
      toast.error(getApiError(e, 'Failed to remove assignment'));
    } finally {
      confirmDialog.loading = false;
      confirmDialog.show = false;
    }
  };
  confirmDialog.show = true;
}

onMounted(() => {
  loadManagementData();
});
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
