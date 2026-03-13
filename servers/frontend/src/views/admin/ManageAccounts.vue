<template>
  <!-- Stats Cards -->
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Total Users</p>
          <p class="text-2xl font-bold text-slate-900 mt-1">{{ users.length }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-primary-100 flex items-center justify-center">
          <i class="fas fa-users text-xl text-primary-600"></i>
        </div>
      </div>
    </div>

    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Active</p>
          <p class="text-2xl font-bold text-emerald-600 mt-1">{{ activeUsers }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-emerald-100 flex items-center justify-center">
          <i class="fas fa-check-circle text-xl text-emerald-600"></i>
        </div>
      </div>
    </div>

    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Inactive</p>
          <p class="text-2xl font-bold text-slate-600 mt-1">{{ inactiveUsers }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-slate-100 flex items-center justify-center">
          <i class="fas fa-user-slash text-xl text-slate-500"></i>
        </div>
      </div>
    </div>

    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Admins</p>
          <p class="text-2xl font-bold text-violet-600 mt-1">{{ adminCount }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-violet-100 flex items-center justify-center">
          <i class="fas fa-user-shield text-xl text-violet-600"></i>
        </div>
      </div>
    </div>
  </div>

  <!-- Loading State -->
  <BaseLoadingSpinner v-if="loading" color="primary" message="Loading users..." />

  <!-- Error State -->
  <div v-else-if="error" class="card p-8 text-center">
    <div class="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4">
      <i class="fas fa-exclamation-triangle text-2xl text-red-600"></i>
    </div>
    <h3 class="text-lg font-semibold text-slate-900 mb-2">Error Loading Users</h3>
    <p class="text-slate-500 mb-4">{{ error }}</p>
    <button @click="loadUsers" class="btn-primary">
      <i class="fas fa-redo"></i>
      <span>Try Again</span>
    </button>
  </div>

  <!-- Users Table -->
  <div v-else class="card overflow-hidden">
    <!-- Toolbar -->
    <div class="px-6 py-4 border-b border-slate-100 space-y-3">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <h2 class="font-semibold text-slate-900">All Users</h2>
        <div class="text-sm text-slate-500">
          {{ totalFilteredUsers }} result{{ totalFilteredUsers === 1 ? '' : 's' }}
        </div>
      </div>

      <div class="flex flex-wrap items-center gap-2">
        <div class="relative">
          <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"></i>
          <input
            type="text"
            v-model="searchQuery"
            placeholder="Search users..."
            class="input pl-10 pr-4 py-2 w-64"
          />
        </div>

        <select v-model="roleFilter" class="input py-2 w-40">
          <option value="">All roles</option>
          <option v-for="role in roleFilterOptions" :key="role" :value="role">{{ role }}</option>
        </select>

        <select v-model="statusFilter" class="input py-2 w-36">
          <option value="">All status</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
        </select>

        <select v-model="serviceFilter" class="input py-2 w-44">
          <option value="">All services</option>
          <option v-for="service in serviceFilterOptions" :key="service" :value="service">{{ service }}</option>
        </select>

        <button class="btn-secondary py-2 px-3 text-sm" @click="clearUserFilters">
          <i class="fas fa-times text-xs"></i>
          Clear
        </button>
        <button class="btn-secondary py-2 px-3 text-sm" @click="loadUsers" :disabled="loading || userActionLoading">
          <i class="fas fa-sync-alt text-xs"></i>
          Refresh
        </button>
      </div>
    </div>

    <!-- Table -->
    <div class="overflow-x-auto">
      <table class="table-modern">
        <thead>
          <tr>
            <th>Full Name</th>
            <th>Email</th>
            <th>Role</th>
            <th>Service</th>
            <th>Assigned To</th>
            <th>Status</th>
            <th class="text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in paginatedUsers" :key="user.id">
            <td>
              {{ user.firstName + ' ' + user.lastName }}
            </td>
            <td>{{ user.email || '—' }}</td>
            <td>
              <span :class="getRoleBadgeClassFull(user.role)">
                {{ user.role || '—' }}
              </span>
            </td>
            <td>
              <span class="flex items-center gap-2">
                <i :class="getServiceIcon(user.service)"></i>
                {{ user.service || '—' }}
              </span>
            </td>
            <td>{{ getResourceName(user.assignedTo, user.service) }}</td>
            <td>
              <span :class="user.status === 'ACTIVE' ? 'badge-success' : 'badge-neutral'" class="badge">
                <span
                  class="w-1.5 h-1.5 rounded-full mr-1.5"
                  :class="user.status === 'ACTIVE' ? 'bg-emerald-500' : 'bg-slate-400'"
                ></span>
                {{ user.status }}
              </span>
            </td>
            <td>
              <div class="flex items-center justify-end gap-1">
                <button
                  @click="openEditModal(user)"
                  class="p-2 rounded-lg text-slate-400 hover:text-primary-600 hover:bg-primary-50 transition-colors"
                  :disabled="userActionLoading"
                  title="Edit user"
                >
                  <i class="fas fa-edit"></i>
                </button>
                <button
                  @click="confirmToggleStatus(user)"
                  class="p-2 rounded-lg text-slate-400 hover:text-slate-700 hover:bg-slate-100 transition-colors"
                  :disabled="userActionLoading"
                  :title="user.status === 'ACTIVE' ? 'Deactivate' : 'Activate'"
                >
                  <i :class="user.status === 'ACTIVE' ? 'fas fa-toggle-on text-emerald-500' : 'fas fa-toggle-off'"></i>
                </button>
                <button
                  @click="openResetPassword(user)"
                  class="p-2 rounded-lg text-slate-400 hover:text-amber-600 hover:bg-amber-50 transition-colors"
                  :disabled="userActionLoading"
                  title="Reset password"
                >
                  <i class="fas fa-key"></i>
                </button>
                <button
                  @click="confirmDeleteUser(user)"
                  class="p-2 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors"
                  :disabled="userActionLoading"
                  title="Delete user"
                >
                  <i class="fas fa-trash-alt"></i>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Empty States -->
    <div v-if="totalFilteredUsers === 0 && !searchQuery && !roleFilter && !statusFilter && !serviceFilter" class="p-12 text-center">
      <div class="w-16 h-16 rounded-2xl bg-slate-100 flex items-center justify-center mx-auto mb-4">
        <i class="fas fa-users text-2xl text-slate-400"></i>
      </div>
      <p class="text-slate-500 font-medium">No users found yet</p>
      <p class="text-sm text-slate-400 mt-1">Create your first user to get started</p>
    </div>

    <div v-if="totalFilteredUsers === 0 && (searchQuery || roleFilter || statusFilter || serviceFilter)" class="p-12 text-center">
      <div class="w-16 h-16 rounded-2xl bg-slate-100 flex items-center justify-center mx-auto mb-4">
        <i class="fas fa-search text-2xl text-slate-400"></i>
      </div>
      <p class="text-slate-500 font-medium">No users match your filters</p>
      <button @click="clearUserFilters" class="text-sm text-primary-600 hover:text-primary-700 mt-2 font-medium">
        Clear all filters
      </button>
    </div>

    <!-- Pagination -->
    <div v-if="totalFilteredUsers > 0" class="px-6 py-4 border-t border-slate-100 flex flex-wrap items-center justify-between gap-3">
      <div class="text-sm text-slate-500">
        Showing {{ pageStart }}-{{ pageEnd }} of {{ totalFilteredUsers }}
      </div>
      <div class="flex items-center gap-2">
        <label class="text-sm text-slate-500">Rows</label>
        <select v-model.number="pageSize" class="input py-1.5 w-20 text-sm">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
        <button class="btn-secondary py-1.5 px-3 text-sm" @click="prevPage" :disabled="currentPage <= 1">
          <i class="fas fa-chevron-left text-xs"></i>
        </button>
        <span class="text-sm text-slate-600 min-w-[80px] text-center">
          Page {{ currentPage }} / {{ totalPages }}
        </span>
        <button class="btn-secondary py-1.5 px-3 text-sm" @click="nextPage" :disabled="currentPage >= totalPages">
          <i class="fas fa-chevron-right text-xs"></i>
        </button>
      </div>
    </div>
  </div>

  <!-- Create User Modal -->
  <Transition name="modal">
    <div
      v-if="showCreateUser"
      class="modal-overlay"
      @click.self="showCreateUser = false"
    >
      <div class="modal-content max-w-2xl">
        <CreateUser @created="handleCreated" @cancel="showCreateUser = false" />
      </div>
    </div>
  </Transition>

  <!-- Edit User Modal -->
  <EditUserModal
    :isOpen="showEditUser"
    :user="selectedUser"
    @close="showEditUser = false"
    @updated="handleUpdated"
  />

  <!-- Reset Password Modal -->
  <ResetPasswordModal
    :isOpen="showResetPassword"
    :username="resetPasswordUser?.username || ''"
    @close="showResetPassword = false"
    @submit="handleResetPassword"
  />

  <!-- Confirm Dialog -->
  <ConfirmDialog
    :isOpen="confirmDialog.show"
    :title="confirmDialog.title"
    :message="confirmDialog.message"
    :confirmText="confirmDialog.confirmText"
    :variant="confirmDialog.variant"
    :loading="confirmDialog.loading"
    @confirm="confirmDialog.onConfirm"
    @cancel="closeConfirm"
  />
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import CreateUser from '@/components/admin/CreateUser.vue';
import EditUserModal from '@/components/admin/EditUserModal.vue';
import ResetPasswordModal from '@/components/admin/ResetPasswordModal.vue';
import ConfirmDialog from '@/components/common/ConfirmDialog.vue';
import { useConfirmDialog } from '@/composables/useConfirmDialog';
import { authApi, barApi } from '@/api';
import { droppointApi } from '@/api/droppointApi';
import { eventApi } from '@/api/eventApi';
import { toast } from 'vue3-toastify';
import { useRoute } from 'vue-router';
import { getInitials, getAvatarClass, getRoleBadgeClassFull, getServiceIcon, normalizeRole, normalizeService } from '@/composables/useRoleStyling';
import BaseLoadingSpinner from '@/components/common/BaseLoadingSpinner.vue';

const route = useRoute();
const emit = defineEmits(['user-count']);

const loading = ref(false);
const error = ref('');
const showCreateUser = ref(false);
const showEditUser = ref(false);
const showResetPassword = ref(false);
const resetPasswordUser = ref(null);
const selectedUser = ref(null);
const rawUsers = ref([]);
const bars = ref([]);
const events = ref([]);
const dropPoints = ref([]);
const searchQuery = ref('');
const roleFilter = ref('');
const statusFilter = ref('');
const serviceFilter = ref('');
const pageSize = ref(10);
const currentPage = ref(1);
const userActionLoading = ref(false);

const { state: confirmDialog, open: openConfirm, close: closeConfirm } = useConfirmDialog();



const users = computed(() => {
  return (rawUsers.value || []).map((u) => {
    const assignments = Array.isArray(u.assignments) ? u.assignments : [];
    const adminAssignment = assignments.find((a) => String(a?.role || '').toUpperCase().includes('ADMIN'));
    const primary = adminAssignment || assignments[0] || null;

    const isAdmin = !!adminAssignment;
    const role = isAdmin ? 'ADMIN' : normalizeRole(primary?.role);
    const service = isAdmin ? 'ALL' : normalizeService(primary?.service);
    const assignedTo = isAdmin ? '*' : (primary?.resourceId ?? null);
    const status = u.enabled ? 'ACTIVE' : 'INACTIVE';

    return {
      id: u.id,
      username: u.username,
      firstName: u.firstName || '',
      lastName: u.lastName || '',
      fullName: [u.firstName, u.lastName].filter(Boolean).join(' ').trim(),
      email: u.email,
      role,
      service,
      assignedTo,
      status,
      _raw: u,
    };
  });
});

const filteredUsers = computed(() => {
  const query = searchQuery.value?.toLowerCase?.() || '';
  return users.value.filter(u => {
    const matchesQuery = !query
      || u.username.toLowerCase().includes(query)
      || `${u.firstName || ''} ${u.lastName || ''}`.toLowerCase().includes(query)
      || u.email?.toLowerCase().includes(query)
      || u.role.toLowerCase().includes(query);
    const matchesRole = !roleFilter.value || u.role === roleFilter.value;
    const matchesStatus = !statusFilter.value || u.status === statusFilter.value;
    const matchesService = !serviceFilter.value || u.service === serviceFilter.value;
    return matchesQuery && matchesRole && matchesStatus && matchesService;
  });
});

const roleFilterOptions = computed(() => {
  return [...new Set(users.value.map(u => u.role).filter(Boolean))];
});

const serviceFilterOptions = computed(() => {
  return [...new Set(users.value.map(u => u.service).filter(Boolean))];
});

const totalFilteredUsers = computed(() => filteredUsers.value.length);
const totalPages = computed(() => Math.max(1, Math.ceil(totalFilteredUsers.value / pageSize.value)));

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filteredUsers.value.slice(start, end);
});

const pageStart = computed(() => {
  if (totalFilteredUsers.value === 0) return 0;
  return (currentPage.value - 1) * pageSize.value + 1;
});

const pageEnd = computed(() => {
  return Math.min(currentPage.value * pageSize.value, totalFilteredUsers.value);
});

const activeUsers = computed(() => users.value.filter(u => u.status === 'ACTIVE').length);
const inactiveUsers = computed(() => users.value.filter(u => u.status === 'INACTIVE').length);
const adminCount = computed(() => users.value.filter(u => u.role === 'ADMIN').length);



function getResourceName(resourceId, service) {
  if (!resourceId || resourceId === '*') return resourceId || '—';

  if (service === 'BAR' || service === 'ALL') {
    const bar = bars.value.find(b => String(b.id) === String(resourceId));
    if (bar) return bar.name;
  }

  if (service === 'EVENT') {
    const event = events.value.find(e => String(e.id) === String(resourceId));
    if (event) return event.name;
  }

  if (service === 'DROP_POINT') {
    const dropPoint = dropPoints.value.find(p => String(p.id) === String(resourceId));
    if (dropPoint) return dropPoint.location;
  }

  return resourceId?.slice(0, 8) + '...';
}

async function loadUsers() {
  loading.value = true;
  error.value = '';
  try {
    const usersResponse = await authApi.getUsers();
    const usersData = usersResponse?.data;
    rawUsers.value = Array.isArray(usersData)
      ? usersData
      : (usersData?.content ?? []);

    emit('user-count', rawUsers.value.length);

    const [barsResult, eventsResult, dropPointsResult] = await Promise.allSettled([
      barApi.getBars(),
      eventApi.getEvents(),
      droppointApi.getDropPoints(),
    ]);

    bars.value = barsResult.status === 'fulfilled' ? (barsResult.value.data || []) : [];
    events.value = eventsResult.status === 'fulfilled' ? (eventsResult.value.data || []) : [];
    dropPoints.value = dropPointsResult.status === 'fulfilled' ? (dropPointsResult.value.data || []) : [];
  } catch (e) {
    error.value = e?.message ?? 'Failed to load users';
  } finally {
    loading.value = false;
  }
}

function confirmDeleteUser(user) {
  openConfirm({
    title: 'Delete User',
    message: `Are you sure you want to permanently delete "${user.username}"? This action cannot be undone.`,
    confirmText: 'Delete User',
    variant: 'danger',
    onConfirm: async () => {
      await authApi.deleteUser(user.id);
      await loadUsers();
      toast.success('User deleted successfully');
    },
  });
}

function confirmToggleStatus(user) {
  const nextEnabled = user.status !== 'ACTIVE';
  const action = nextEnabled ? 'activate' : 'deactivate';

  openConfirm({
    title: `${nextEnabled ? 'Activate' : 'Deactivate'} User`,
    message: `Do you want to ${action} "${user.username}"?`,
    confirmText: nextEnabled ? 'Activate' : 'Deactivate',
    variant: nextEnabled ? 'info' : 'warning',
    onConfirm: async () => {
      const dto = user._raw;
      await authApi.updateUser(user.id, {
        id: dto.id,
        username: dto.username,
        firstName: dto.firstName,
        lastName: dto.lastName,
        email: dto.email,
        enabled: nextEnabled,
        assignments: dto.assignments,
      });
      await loadUsers();
      toast.success(`User ${user.username} ${nextEnabled ? 'activated' : 'deactivated'}`);
    },
  });
}

function openResetPassword(user) {
  resetPasswordUser.value = user;
  showResetPassword.value = true;
}

async function handleResetPassword({ password, resolve, reject }) {
  try {
    await authApi.adminResetUserPassword(resetPasswordUser.value.id, password);
    toast.success(`Password reset for ${resetPasswordUser.value.username}`);
    resolve();
  } catch (e) {
    reject(e);
  }
}

function clearUserFilters() {
  searchQuery.value = '';
  roleFilter.value = '';
  statusFilter.value = '';
  serviceFilter.value = '';
}

function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value += 1;
}

function prevPage() {
  if (currentPage.value > 1) currentPage.value -= 1;
}

watch([searchQuery, roleFilter, statusFilter, serviceFilter, pageSize], () => {
  currentPage.value = 1;
});

watch(totalPages, (value) => {
  if (currentPage.value > value) currentPage.value = value;
});

function openEditModal(user) {
  selectedUser.value = user;
  showEditUser.value = true;
}

function handleCreated() {
  showCreateUser.value = false;
  loadUsers();
  toast.success('User created successfully');
}

function handleUpdated() {
  showEditUser.value = false;
  loadUsers();
  toast.success('User updated successfully');
}

// Watch for ?create=1 query param (from AdminLayout button)
watch(() => route.query.create, (val) => {
  if (val === '1') {
    showCreateUser.value = true;
    // Clean up the URL
    window.history.replaceState({}, '', route.path);
  }
}, { immediate: true });

onMounted(() => {
  loadUsers();
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
