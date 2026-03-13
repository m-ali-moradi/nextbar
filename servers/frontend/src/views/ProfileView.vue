<template>
  <div>
        <!-- Page Header -->
        <div class="mb-8">
          <h1 class="text-2xl font-bold text-slate-900">Profile</h1>
          <p class="text-slate-500 mt-1">Manage your account settings</p>
        </div>

        <!-- Loading -->
        <BaseLoadingSpinner v-if="loading" color="primary" message="Loading profile..." />

        <div v-else class="space-y-6">
          <!-- Profile Card Header -->
          <div class="card overflow-hidden">
            <div class="h-24 bg-gradient-to-r from-primary-500 via-primary-600 to-violet-600"></div>
            <div class="px-6 pb-6">
              <div class="flex flex-col sm:flex-row sm:items-end gap-4 -mt-10">
                <div
                  class="w-20 h-20 rounded-2xl border-4 border-white shadow-lg flex items-center justify-center text-white font-bold text-2xl bg-gradient-to-br from-primary-500 to-primary-700"
                >
                  {{ getInitials((profileForm.firstName + ' ' + profileForm.lastName).trim() || profileForm.username) }}
                </div>
                <div class="flex-1 pt-2">
                  <h2 class="text-xl font-bold text-slate-900">{{ (profileForm.firstName + ' ' + profileForm.lastName).trim() || profileForm.username }}</h2>
                  <p class="text-sm text-slate-500">{{ profileForm.email }}</p>
                </div>
                <div class="flex items-center gap-2 flex-wrap">
                  <span
                    v-if="userRole"
                    :class="getRoleBadgeClass(userRole)"
                    class="badge"
                  >
                    <i :class="getRoleIcon(userRole)" class="mr-1.5"></i>
                    {{ userRole }}
                  </span>
                  <span
                    v-if="userService"
                    class="badge badge-neutral"
                  >
                    <i :class="getServiceIcon(userService)" class="mr-1.5"></i>
                    {{ userService }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Content Grid -->
          <div class="grid grid-cols-1 xl:grid-cols-2 gap-6">
            <!-- Profile Info -->
            <div class="card overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100 flex items-center gap-3">
                <div class="w-9 h-9 rounded-lg bg-primary-100 flex items-center justify-center">
                  <i class="fas fa-user text-primary-600"></i>
                </div>
                <h2 class="font-semibold text-slate-900">Profile Information</h2>
              </div>
              <div class="p-6">
                <form class="space-y-4" @submit.prevent="saveProfile">
                  <div>
                    <label class="label">Username</label>
                    <input
                      v-model="profileForm.username"
                      class="input bg-slate-50 cursor-not-allowed"
                      readonly
                    />
                    <p class="text-xs text-slate-400 mt-1">Username cannot be changed</p>
                  </div>
                  <div>
                    <label class="label">Email</label>
                    <input
                      v-model="profileForm.email"
                      type="email"
                      class="input"
                      required
                    />
                  </div>
                  <div>
                    <label class="label">First Name</label>
                    <input
                      v-model="profileForm.firstName"
                      type="text"
                      class="input"
                      required
                    />
                  </div>
                  <div>
                    <label class="label">Last Name</label>
                    <input
                      v-model="profileForm.lastName"
                      type="text"
                      class="input"
                      required
                    />
                  </div>
                  <button class="btn-primary w-full" type="submit" :disabled="savingProfile">
                    <template v-if="savingProfile">
                      <i class="fas fa-spinner fa-spin"></i>
                      <span>Saving...</span>
                    </template>
                    <template v-else>
                      <i class="fas fa-save"></i>
                      <span>Save Profile</span>
                    </template>
                  </button>
                </form>
              </div>
            </div>

            <!-- Change Password -->
            <div class="card overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100 flex items-center gap-3">
                <div class="w-9 h-9 rounded-lg bg-amber-100 flex items-center justify-center">
                  <i class="fas fa-lock text-amber-600"></i>
                </div>
                <h2 class="font-semibold text-slate-900">Change Password</h2>
              </div>
              <div class="p-6">
                <form class="space-y-4" @submit.prevent="changePassword">
                  <div>
                    <label class="label">Current Password</label>
                    <div class="relative">
                      <input
                        v-model="passwordForm.currentPassword"
                        :type="showCurrent ? 'text' : 'password'"
                        class="input pr-11"
                        required
                      />
                      <button
                        type="button"
                        @click="showCurrent = !showCurrent"
                        class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                        tabindex="-1"
                      >
                        <i :class="showCurrent ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                      </button>
                    </div>
                  </div>
                  <div>
                    <label class="label">New Password</label>
                    <div class="relative">
                      <input
                        v-model="passwordForm.newPassword"
                        :type="showNew ? 'text' : 'password'"
                        class="input pr-11"
                        required
                        minlength="6"
                      />
                      <button
                        type="button"
                        @click="showNew = !showNew"
                        class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
                        tabindex="-1"
                      >
                        <i :class="showNew ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                      </button>
                    </div>
                    <!-- Strength -->
                    <div class="mt-2" v-if="passwordForm.newPassword">
                      <div class="flex gap-1 mb-1">
                        <div
                          v-for="i in 4"
                          :key="i"
                          class="h-1.5 flex-1 rounded-full transition-all duration-300"
                          :class="i <= passwordStrength.level ? passwordStrength.color : 'bg-slate-200'"
                        ></div>
                      </div>
                      <p class="text-xs font-medium" :class="passwordStrength.textColor">
                        {{ passwordStrength.label }}
                      </p>
                    </div>
                  </div>
                  <div>
                    <label class="label">Confirm New Password</label>
                    <div class="relative">
                      <input
                        v-model="passwordForm.confirmPassword"
                        :type="showConfirm ? 'text' : 'password'"
                        class="input pr-11"
                        :class="passwordForm.confirmPassword && passwordForm.confirmPassword !== passwordForm.newPassword
                          ? 'border-red-300 focus:ring-red-500 focus:border-red-500'
                          : passwordForm.confirmPassword && passwordForm.confirmPassword === passwordForm.newPassword
                            ? 'border-emerald-300 focus:ring-emerald-500 focus:border-emerald-500'
                            : ''"
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
                      v-if="passwordForm.confirmPassword && passwordForm.confirmPassword !== passwordForm.newPassword"
                      class="text-xs text-red-500 mt-1"
                    >
                      Passwords do not match
                    </p>
                    <p
                      v-else-if="passwordForm.confirmPassword && passwordForm.confirmPassword === passwordForm.newPassword"
                      class="text-xs text-emerald-600 mt-1"
                    >
                      <i class="fas fa-check mr-1"></i> Passwords match
                    </p>
                  </div>
                  <button
                    class="btn-warning w-full"
                    type="submit"
                    :disabled="savingPassword || !isPasswordFormValid"
                  >
                    <template v-if="savingPassword">
                      <i class="fas fa-spinner fa-spin"></i>
                      <span>Updating...</span>
                    </template>
                    <template v-else>
                      <i class="fas fa-key"></i>
                      <span>Change Password</span>
                    </template>
                  </button>
                </form>
              </div>
            </div>
          </div>

          <!-- Account Info -->
          <div class="card overflow-hidden">
            <div class="px-6 py-4 border-b border-slate-100 flex items-center gap-3">
              <div class="w-9 h-9 rounded-lg bg-slate-100 flex items-center justify-center">
                <i class="fas fa-info-circle text-slate-600"></i>
              </div>
              <h2 class="font-semibold text-slate-900">Account Details</h2>
            </div>
            <div class="p-6">
              <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                <div class="p-4 rounded-xl bg-slate-50 border border-slate-200">
                  <p class="text-xs text-slate-500 font-medium mb-1">Role</p>
                  <p class="font-semibold text-slate-900">{{ userRole || '—' }}</p>
                </div>
                <div class="p-4 rounded-xl bg-slate-50 border border-slate-200">
                  <p class="text-xs text-slate-500 font-medium mb-1">Service</p>
                  <p class="font-semibold text-slate-900">{{ userService || '—' }}</p>
                </div>
                <div class="p-4 rounded-xl bg-slate-50 border border-slate-200">
                  <p class="text-xs text-slate-500 font-medium mb-1">Status</p>
                  <span class="badge badge-success text-xs">
                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 mr-1"></span>
                    Active
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { toast } from 'vue3-toastify';
import { usePasswordStrength } from '@/composables/usePasswordStrength';
import { getInitials, getRoleBadgeClass, getRoleIcon, getServiceIcon } from '@/composables/useRoleStyling';
import { authApi } from '@/api';
import { useAuthStore } from '@/stores/authStore';
import BaseLoadingSpinner from '@/components/common/BaseLoadingSpinner.vue';

const authStore = useAuthStore();

const loading = ref(true);
const savingProfile = ref(false);
const savingPassword = ref(false);
const showCurrent = ref(false);
const showNew = ref(false);
const showConfirm = ref(false);

const profileForm = ref({
  username: '',
  email: '',
  firstName: '',
  lastName: '',
});

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const userRole = computed(() => {
  const user = authStore.user;
  if (!user) return '';
  if (user.isAdmin) return 'ADMIN';
  const roles = user.roles || [];
  if (roles.length > 0) {
    const roleName = roles[0]?.role || '';
    if (roleName.includes('MANAGER')) return 'MANAGER';
    if (roleName.includes('OPERATOR') || roleName.includes('BARTENDER')) return 'OPERATOR';
    return roleName;
  }
  return '';
});

const userService = computed(() => {
  const user = authStore.user;
  if (!user) return '';
  if (user.isAdmin) return 'ALL';
  const roles = user.roles || [];
  if (roles.length > 0) {
    const svc = roles[0]?.service || '';
    return svc.replace('_SERVICE', '');
  }
  return '';
});

const _newPassword = computed(() => passwordForm.value.newPassword);
const { strength: passwordStrength } = usePasswordStrength(_newPassword);

const isPasswordFormValid = computed(() => {
  return passwordForm.value.currentPassword
    && passwordForm.value.newPassword.length >= 6
    && passwordForm.value.confirmPassword === passwordForm.value.newPassword;
});

async function loadProfile() {
  loading.value = true;
  try {
    const response = await authApi.getProfile();
    const data = response?.data || {};
    profileForm.value = {
      username: data.username || '',
      email: data.email || '',
      firstName: data.firstName || '',
      lastName: data.lastName || '',
    };
  } catch (e) {
    toast.error(e?.message || 'Failed to load profile');
  } finally {
    loading.value = false;
  }
}

async function saveProfile() {
  try {
    savingProfile.value = true;
    const response = await authApi.updateProfile({
      email: profileForm.value.email,
      firstName: profileForm.value.firstName,
      lastName: profileForm.value.lastName,
    });
    const updated = response?.data || {};
    authStore.user = {
      ...(authStore.user || {}),
      username: updated.username || profileForm.value.username,
      email: updated.email || profileForm.value.email,
      firstName: updated.firstName || profileForm.value.firstName,
      lastName: updated.lastName || profileForm.value.lastName,
    };
    toast.success('Profile updated successfully');
  } catch (e) {
    toast.error(e?.message || 'Failed to update profile');
  } finally {
    savingProfile.value = false;
  }
}

async function changePassword() {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    toast.error('New password and confirmation do not match');
    return;
  }

  try {
    savingPassword.value = true;
    await authApi.changeMyPassword({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword,
      confirmPassword: passwordForm.value.confirmPassword,
    });
    passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' };
    showCurrent.value = false;
    showNew.value = false;
    showConfirm.value = false;
    toast.success('Password changed successfully');
  } catch (e) {
    toast.error(e?.message || 'Failed to change password');
  } finally {
    savingPassword.value = false;
  }
}

onMounted(() => {
  loadProfile();
});
</script>
