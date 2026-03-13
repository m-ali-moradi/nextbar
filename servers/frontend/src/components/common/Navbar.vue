<template>
  <header 
    class="sticky top-0 z-40 h-20 px-8 border-b border-slate-200/80 
           bg-white/80 backdrop-blur-xl flex items-center justify-between gap-4"
  >
    <!-- Left: Page Title & Breadcrumb -->
    <div class="flex items-center gap-4">
      <div>
        <h1 class="text-xl font-bold text-slate-900">{{ pageTitle }}</h1>
        <p v-if="pageSubtitle" class="text-sm text-slate-500">{{ pageSubtitle }}</p>
      </div>
    </div>

    <!-- Right: Actions -->
    <div class="flex items-center gap-4">
      <!-- Search Button -->
      <button 
        class="p-3 rounded-xl text-slate-500 hover:text-slate-700 hover:bg-slate-100 
               transition-colors duration-200"
        title="Search"
      >
        <i class="fas fa-search text-lg"></i>
      </button>

      <!-- Notifications -->
      <div class="relative">
        <button 
          @click="toggleNotifications"
          class="p-3 rounded-xl text-slate-500 hover:text-slate-700 hover:bg-slate-100 
                 transition-colors duration-200 relative"
          title="Notifications"
        >
          <i class="fas fa-bell text-lg"></i>
          <span 
            v-if="notificationCount > 0"
            class="absolute top-1 right-1 w-5 h-5 rounded-full bg-red-500 
                   text-white text-[11px] font-bold flex items-center justify-center"
          >
            {{ notificationCount > 9 ? '9+' : notificationCount }}
          </span>
        </button>

        <!-- Notifications Dropdown -->
        <Transition name="dropdown">
          <div 
            v-if="showNotifications"
            class="absolute right-0 mt-2 w-96 bg-white rounded-xl shadow-elevated 
                   border border-slate-100 overflow-hidden"
          >
            <div class="px-5 py-4 border-b border-slate-100">
              <h3 class="font-bold text-lg text-slate-900">Notifications</h3>
            </div>
            <div class="max-h-80 overflow-y-auto">
              <div class="px-5 py-12 text-center text-slate-500">
                <i class="fas fa-bell-slash text-4xl mb-3 opacity-50"></i>
                <p class="text-base">No new notifications</p>
              </div>
            </div>
          </div>
        </Transition>
      </div>

      <!-- Divider -->
      <div class="w-px h-10 bg-slate-200"></div>

      <!-- User Menu -->
      <div class="relative">
        <button 
          @click="toggleUserMenu"
          class="flex items-center gap-4 py-2 px-4 rounded-xl hover:bg-slate-100 
                 transition-colors duration-200"
        >
          <div 
            class="w-11 h-11 rounded-full bg-gradient-to-br from-primary-500 to-primary-600 
                   flex items-center justify-center text-white text-lg font-bold shadow-md"
          >
            {{ userInitials }}
          </div>
          <div class="hidden sm:block text-left">
            <p class="text-base font-semibold text-slate-900">{{ userName }}</p>
            <p class="text-sm text-slate-500">{{ userRole }}</p>
          </div>
          <i class="fas fa-chevron-down text-sm text-slate-400 ml-1"></i>
        </button>

        <!-- User Dropdown -->
        <Transition name="dropdown">
          <div 
            v-if="showUserMenu"
            class="absolute right-0 mt-2 w-64 bg-white rounded-xl shadow-elevated 
                   border border-slate-100 overflow-hidden"
          >
            <div class="px-5 py-4 border-b border-slate-100 bg-slate-50">
              <p class="text-base font-semibold text-slate-900">{{ userName }}</p>
              <p class="text-sm text-slate-500">{{ authStore.user?.email || 'No email' }}</p>
            </div>
            <div class="py-2">
              <button 
                @click="goToProfile"
                class="w-full flex items-center gap-4 px-5 py-3 text-base text-slate-700 
                       hover:bg-slate-50 transition-colors"
              >
                <i class="fas fa-user text-lg w-5 text-slate-400"></i>
                <span>Profile Settings</span>
              </button>
              <button 
                class="w-full flex items-center gap-4 px-5 py-3 text-base text-slate-700 
                       hover:bg-slate-50 transition-colors"
              >
                <i class="fas fa-cog text-lg w-5 text-slate-400"></i>
                <span>Preferences</span>
              </button>
            </div>
            <div class="border-t border-slate-100 py-2">
              <button 
                @click="handleLogout"
                class="w-full flex items-center gap-4 px-5 py-3 text-base text-red-600 
                       hover:bg-red-50 transition-colors"
              >
                <i class="fas fa-sign-out-alt text-lg w-5"></i>
                <span>Logout</span>
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </header>

  <!-- Click outside handler -->
  <div 
    v-if="showNotifications || showUserMenu"
    class="fixed inset-0 z-30"
    @click="closeDropdowns"
  ></div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();

const showNotifications = ref(false);
const showUserMenu = ref(false);
const notificationCount = ref(0);

// Page title based on route
const pageTitle = computed(() => {
  const routeTitles = {
    '/bars': 'Bars Dashboard',
    '/droppoints': 'Drop Points',
    '/events': 'Events',
    '/warehouse': 'Warehouse',
    '/admin': 'Admin Panel',
    '/profile': 'Profile',
  };
  
  // Check for specific routes first
  for (const [path, title] of Object.entries(routeTitles)) {
    if (route.path === path || route.path.startsWith(path + '/')) {
      return title;
    }
  }
  
  return 'Dashboard';
});

const pageSubtitle = computed(() => {
  if (route.params.barId) return 'Bar Details';
  if (route.params.id && route.path.includes('events')) return 'Event Details';
  return null;
});

const userName = computed(() => authStore.user?.username || 'Guest');

const userInitials = computed(() => {
  const name = userName.value;
  return name.charAt(0).toUpperCase();
});

const userRole = computed(() => {
  if (authStore.user?.isAdmin) return 'Administrator';
  const roles = authStore.user?.roles;
  if (Array.isArray(roles) && roles.length > 0) {
    return roles[0]?.role || 'User';
  }
  return 'User';
});

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value;
  showUserMenu.value = false;
};

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value;
  showNotifications.value = false;
};

const closeDropdowns = () => {
  showNotifications.value = false;
  showUserMenu.value = false;
};

const goToProfile = () => {
  closeDropdowns();
  router.push('/profile');
};

const handleLogout = () => {
  closeDropdowns();
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease-out;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>