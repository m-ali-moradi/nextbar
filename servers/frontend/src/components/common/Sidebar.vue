<template>
  <aside 
    class="fixed left-0 top-0 bottom-0 w-72 bg-gradient-to-b from-slate-900 via-slate-900 to-slate-800 
           text-white flex flex-col z-50 shadow-xl"
  >
    <!-- Brand Header -->
    <div class="px-6 py-6 border-b border-slate-700/50">
      <div class="flex items-center gap-4">
        <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-primary-500 to-primary-600 
                    flex items-center justify-center shadow-lg shadow-primary-500/30">
          <i class="fas fa-glass-martini-alt text-xl"></i>
        </div>
        <div>
          <h1 class="text-2xl font-bold tracking-tight">NextBar</h1>
          <p class="text-sm text-slate-400 font-medium">Management System</p>
        </div>
      </div>
    </div>

    <!-- Navigation Menu -->
    <nav class="flex-1 px-4 py-5 space-y-2 overflow-y-auto scrollbar-thin">
      <!-- Main Section -->
      <p class="px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-4">
        Main Menu
      </p>

      <router-link
        v-if="canSeeBars"
        to="/bars"
        class="nav-item group"
        :class="{ 'nav-item-active': isActive('/bars') }"
      >
        <div class="nav-icon bg-bar-500/20 text-bar-400 group-hover:bg-bar-500 group-hover:text-white">
          <i class="fas fa-glass-martini text-lg"></i>
        </div>
        <span class="nav-text">Bars</span>
        <div v-if="isActive('/bars')" class="nav-indicator bg-bar-500"></div>
      </router-link>

      <router-link
        v-if="canSeeDroppoints"
        to="/droppoints"
        class="nav-item group"
        :class="{ 'nav-item-active': isActive('/droppoints') }"
      >
        <div class="nav-icon bg-droppoint-500/20 text-droppoint-400 group-hover:bg-droppoint-500 group-hover:text-white">
          <i class="fas fa-map-marker-alt text-lg"></i>
        </div>
        <span class="nav-text">Drop Points</span>
        <div v-if="isActive('/droppoints')" class="nav-indicator bg-droppoint-500"></div>
      </router-link>

      <router-link
        v-if="canSeeEvents"
        to="/events"
        class="nav-item group"
        :class="{ 'nav-item-active': isActive('/events') }"
      >
        <div class="nav-icon bg-event-500/20 text-event-400 group-hover:bg-event-500 group-hover:text-white">
          <i class="fas fa-calendar-alt text-lg"></i>
        </div>
        <span class="nav-text">Events</span>
        <div v-if="isActive('/events')" class="nav-indicator bg-event-500"></div>
      </router-link>

      <router-link
        v-if="canSeeWarehouse"
        to="/warehouse"
        class="nav-item group"
        :class="{ 'nav-item-active': isActive('/warehouse') }"
      >
        <div class="nav-icon bg-warehouse-500/20 text-warehouse-400 group-hover:bg-warehouse-500 group-hover:text-white">
          <i class="fas fa-warehouse text-lg"></i>
        </div>
        <span class="nav-text">Warehouse</span>
        <div v-if="isActive('/warehouse')" class="nav-indicator bg-warehouse-500"></div>
      </router-link>

      <!-- Admin Section -->
      <template v-if="isAdmin">
        <div class="my-5 border-t border-slate-700/50"></div>
        <p class="px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider mb-4">
          Administration
        </p>

        <router-link
          to="/admin"
          class="nav-item group"
          :class="{ 'nav-item-active': isActive('/admin') }"
        >
          <div class="nav-icon bg-admin-500/20 text-admin-400 group-hover:bg-admin-500 group-hover:text-white">
            <i class="fas fa-user-shield text-lg"></i>
          </div>
          <span class="nav-text">Manage Accounts</span>
          <div v-if="isActive('/admin')" class="nav-indicator bg-admin-500"></div>
        </router-link>
      </template>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import {
  isAdmin as checkAdmin,
  hasService as checkService,
  hasManagerRole,
  canAccessDroppoints,
} from '@/composables/useAccessControl';

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

const user = computed(() => authStore.user);
const isAdmin = computed(() => checkAdmin(user.value));

const canSeeBars = computed(() => checkService(user.value, 'BAR'));
const canSeeDroppoints = computed(() => canAccessDroppoints(user.value));
const canSeeWarehouse = computed(() => checkService(user.value, 'WAREHOUSE'));
const canSeeEvents = computed(() => hasManagerRole(user.value, 'EVENT'));

const isActive = (path: string) => route.path.startsWith(path);

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.nav-item {
  @apply relative flex items-center gap-4 px-4 py-3 rounded-xl
         text-slate-300 font-semibold text-base
         transition-all duration-200 ease-out
         hover:bg-slate-800 hover:text-white;
}

.nav-item-active {
  @apply bg-slate-800/80 text-white;
}

.nav-icon {
  @apply w-11 h-11 rounded-xl flex items-center justify-center
         transition-all duration-200 ease-out;
}

.nav-text {
  @apply flex-1;
}

.nav-indicator {
  @apply absolute right-0 top-1/2 -translate-y-1/2 w-1.5 h-8 rounded-l-full;
}
</style>