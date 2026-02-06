<template>
  <div class="flex min-h-screen bg-slate-50">
    <Sidebar />

    <div class="flex-1 ml-72">
      <Navbar />

      <main class="p-6">
        <!-- Page Header -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-bold text-slate-900">Manage Accounts</h1>
            <p class="text-slate-500 mt-1">User administration and access control</p>
          </div>
          <button
            @click="showCreateUser = true"
            class="btn-primary"
          >
            <i class="fas fa-user-plus"></i>
            <span>Create User</span>
          </button>
        </div>

        <!-- Stats Cards -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Total Users</p>
                <p class="text-2xl font-bold text-slate-900 mt-1">{{ users.length }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-admin-100 flex items-center justify-center">
                <i class="fas fa-users text-xl text-admin-600"></i>
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
        <div v-if="loading" class="flex items-center justify-center py-20">
          <div class="text-center">
            <div class="w-12 h-12 border-4 border-admin-200 border-t-admin-600 rounded-full animate-spin mx-auto mb-4"></div>
            <p class="text-slate-500">Loading users...</p>
          </div>
        </div>

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
          <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
            <h2 class="font-semibold text-slate-900">All Users</h2>
            <div class="flex items-center gap-2">
              <div class="relative">
                <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"></i>
                <input
                  type="text"
                  v-model="searchQuery"
                  placeholder="Search users..."
                  class="input pl-10 pr-4 py-2 w-64"
                />
              </div>
            </div>
          </div>

          <div class="overflow-x-auto">
            <table class="table-modern">
              <thead>
                <tr>
                  <th>User</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Service</th>
                  <th>Assigned To</th>
                  <th>Status</th>
                  <th class="text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in filteredUsers" :key="user.id">
                  <td>
                    <div class="flex items-center gap-3">
                      <div 
                        class="w-10 h-10 rounded-full flex items-center justify-center 
                               text-white font-bold text-sm"
                        :class="getAvatarClass(user.role)"
                      >
                        {{ getInitials(user.username) }}
                      </div>
                      <div>
                        <p class="font-medium text-slate-900">{{ user.username }}</p>
                        <p class="text-xs text-slate-500">#{{ user.id?.slice(0, 8) }}...</p>
                      </div>
                    </div>
                  </td>
                  <td>{{ user.email || '—' }}</td>
                  <td>
                    <span :class="getRoleBadgeClass(user.role)">
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
                    <div class="flex items-center justify-end gap-2">
                      <!-- Edit Button -->
                      <button
                        @click="openEditModal(user)"
                        class="p-2 rounded-lg text-slate-500 hover:text-admin-600 
                               hover:bg-admin-50 transition-colors"
                        title="Edit user"
                      >
                        <i class="fas fa-edit"></i>
                      </button>
                      <!-- Toggle Status Button -->
                      <button
                        @click="toggleUserStatus(user)"
                        class="p-2 rounded-lg text-slate-500 hover:text-slate-700 
                               hover:bg-slate-100 transition-colors"
                        :title="user.status === 'ACTIVE' ? 'Deactivate' : 'Activate'"
                      >
                        <i :class="user.status === 'ACTIVE' ? 'fas fa-toggle-on text-emerald-500' : 'fas fa-toggle-off'"></i>
                      </button>
                      <!-- Delete Button -->
                      <button
                        @click="deleteUser(user.id)"
                        class="p-2 rounded-lg text-slate-500 hover:text-red-600 
                               hover:bg-red-50 transition-colors"
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

          <!-- Empty Search State -->
          <div v-if="filteredUsers.length === 0 && searchQuery" class="p-8 text-center">
            <i class="fas fa-search text-4xl text-slate-300 mb-4"></i>
            <p class="text-slate-500">No users found matching "{{ searchQuery }}"</p>
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
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import Sidebar from '../../components/common/Sidebar.vue';
import Navbar from '../../components/common/Navbar.vue';
import CreateUser from '../../components/admin/CreateUser.vue';
import EditUserModal from '../../components/admin/EditUserModal.vue';
import { authApi, barApi } from '@/api';

const loading = ref(false);
const error = ref('');
const showCreateUser = ref(false);
const showEditUser = ref(false);
const selectedUser = ref(null);
const rawUsers = ref([]);
const bars = ref([]);
const searchQuery = ref('');

function normalizeService(code) {
  if (!code) return '';
  const s = String(code).toUpperCase();
  return s.endsWith('_SERVICE') ? s.slice(0, -'_SERVICE'.length) : s;
}

function normalizeRole(name) {
  if (!name) return '';
  const r = String(name).toUpperCase();
  if (r === 'ADMIN') return 'ADMIN';
  if (r.includes('MANAGER')) return 'MANAGER';
  if (r.includes('BARTENDER') || r.includes('OPERATOR')) return 'OPERATOR';
  return r;
}

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
  if (!searchQuery.value) return users.value;
  const query = searchQuery.value.toLowerCase();
  return users.value.filter(u => 
    u.username.toLowerCase().includes(query) ||
    u.email?.toLowerCase().includes(query) ||
    u.role.toLowerCase().includes(query)
  );
});

const activeUsers = computed(() => users.value.filter(u => u.status === 'ACTIVE').length);
const inactiveUsers = computed(() => users.value.filter(u => u.status === 'INACTIVE').length);
const adminCount = computed(() => users.value.filter(u => u.role === 'ADMIN').length);

function getInitials(name) {
  if (!name) return '?';
  return name.charAt(0).toUpperCase();
}

function getAvatarClass(role) {
  const classes = {
    'ADMIN': 'bg-gradient-to-br from-violet-500 to-violet-600',
    'MANAGER': 'bg-gradient-to-br from-blue-500 to-blue-600',
    'OPERATOR': 'bg-gradient-to-br from-emerald-500 to-emerald-600',
  };
  return classes[role] || 'bg-gradient-to-br from-slate-400 to-slate-500';
}

function getRoleBadgeClass(role) {
  const classes = {
    'ADMIN': 'badge badge-info bg-violet-100 text-violet-700',
    'MANAGER': 'badge badge-info bg-blue-100 text-blue-700',
    'OPERATOR': 'badge badge-info bg-emerald-100 text-emerald-700',
  };
  return classes[role] || 'badge badge-neutral';
}

function getServiceIcon(service) {
  const icons = {
    'ALL': 'fas fa-globe text-violet-500',
    'BAR': 'fas fa-glass-martini text-bar-500',
    'DROP_POINT': 'fas fa-map-marker-alt text-droppoint-500',
    'WAREHOUSE': 'fas fa-warehouse text-warehouse-500',
    'EVENT': 'fas fa-calendar text-event-500',
  };
  return icons[service] || 'fas fa-circle text-slate-400';
}

function getResourceName(resourceId, service) {
  if (!resourceId || resourceId === '*') return resourceId || '—';
  
  if (service === 'BAR' || service === 'ALL') {
    const bar = bars.value.find(b => b.id === resourceId);
    if (bar) return bar.name;
  }
  // Can add similar lookups for other services (warehouse, droppoints, events) if needed
  
  return resourceId?.slice(0, 8) + '...'; // Fallback to truncated ID
}

async function loadUsers() {
  loading.value = true;
  error.value = '';
  try {
    const [usersResponse, barsResponse] = await Promise.all([
      authApi.getUsers(),
      barApi.getBars()
    ]);
    rawUsers.value = usersResponse.data;
    bars.value = barsResponse.data || [];
  } catch (e) {
    error.value = e?.message ?? 'Failed to load users';
  } finally {
    loading.value = false;
  }
}

async function deleteUser(userId) {
  if (!confirm('Are you sure you want to delete this user?')) return;
  try {
    await authApi.deleteUser(userId);
    await loadUsers();
  } catch (e) {
    error.value = e?.message ?? 'Failed to delete user';
  }
}

async function toggleUserStatus(user) {
  const nextEnabled = user.status !== 'ACTIVE';
  const action = nextEnabled ? 'activate' : 'deactivate';
  if (!confirm(`Do you want to ${action} ${user.username}?`)) return;

  try {
    const dto = user._raw;
    await authApi.updateUser(user.id, {
      id: dto.id,
      username: dto.username,
      email: dto.email,
      enabled: nextEnabled,
      assignments: dto.assignments,
    });
    await loadUsers();
  } catch (e) {
    error.value = e?.message ?? 'Failed to update user';
  }
}

function openEditModal(user) {
  selectedUser.value = user;
  showEditUser.value = true;
}

function handleCreated() {
  showCreateUser.value = false;
  loadUsers();
}

function handleUpdated() {
  showEditUser.value = false;
  loadUsers();
}

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
