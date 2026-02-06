<template>
  <div class="flex min-h-screen bg-slate-50">
    <Sidebar />
    
    <div class="flex-1 ml-72">
      <Navbar />
      
      <main class="p-6">
        <!-- Page Header -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-bold text-slate-900">Bars Dashboard</h1>
            <p class="text-slate-500 mt-1">Manage your bars and inventory</p>
          </div>
        </div>

        <!-- Stats Overview -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Total Bars</p>
                <p class="text-2xl font-bold text-slate-900 mt-1">{{ barStore.bars.length }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-bar-100 flex items-center justify-center">
                <i class="fas fa-glass-martini text-xl text-bar-600"></i>
              </div>
            </div>
          </div>
          
          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Active Bars</p>
                <p class="text-2xl font-bold text-slate-900 mt-1">{{ barStore.bars.length }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-emerald-100 flex items-center justify-center">
                <i class="fas fa-check-circle text-xl text-emerald-600"></i>
              </div>
            </div>
          </div>

          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Total Capacity</p>
                <p class="text-2xl font-bold text-slate-900 mt-1">{{ totalCapacity }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-violet-100 flex items-center justify-center">
                <i class="fas fa-users text-xl text-violet-600"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="barStore.loading" class="flex items-center justify-center py-20">
          <div class="text-center">
            <div class="w-12 h-12 border-4 border-bar-200 border-t-bar-600 rounded-full animate-spin mx-auto mb-4"></div>
            <p class="text-slate-500">Loading bars...</p>
          </div>
        </div>

        <!-- Error State -->
        <div 
          v-else-if="barStore.error" 
          class="card p-8 text-center"
        >
          <div class="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4">
            <i class="fas fa-exclamation-triangle text-2xl text-red-600"></i>
          </div>
          <h3 class="text-lg font-semibold text-slate-900 mb-2">Error Loading Bars</h3>
          <p class="text-slate-500 mb-4">{{ barStore.error }}</p>
          <button @click="barStore.fetchBars" class="btn-primary">
            <i class="fas fa-redo"></i>
            <span>Try Again</span>
          </button>
        </div>

        <!-- Bars Grid -->
        <div 
          v-else-if="barStore.bars.length" 
          class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6"
        >
          <div
            v-for="bar in barStore.bars"
            :key="bar.id"
            class="card group card-interactive overflow-hidden"
          >
            <!-- Card Header with Gradient -->
            <div class="h-24 bg-gradient-to-br from-bar-500 to-bar-700 p-5 relative">
              <div class="absolute top-3 right-3">
                <div class="relative">
                  <button 
                    @click.stop="toggleMenu(bar.id)"
                    class="w-8 h-8 rounded-lg bg-white/20 hover:bg-white/30 
                           flex items-center justify-center text-white transition-colors"
                  >
                    <i class="fas fa-ellipsis-v"></i>
                  </button>
                  
                  <!-- Dropdown Menu -->
                  <Transition name="dropdown">
                    <div 
                      v-if="activeMenu === bar.id"
                      class="absolute right-0 mt-2 w-40 bg-white rounded-xl shadow-elevated 
                             border border-slate-100 overflow-hidden z-20"
                    >
                      <button
                        @click="editBar(bar.id)"
                        class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-slate-700 
                               hover:bg-slate-50 transition-colors"
                      >
                        <i class="fas fa-edit w-4 text-slate-400"></i>
                        <span>Edit Bar</span>
                      </button>
                      <button
                        @click="deleteBar(bar.id)"
                        class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-red-600 
                               hover:bg-red-50 transition-colors"
                      >
                        <i class="fas fa-trash-alt w-4"></i>
                        <span>Delete</span>
                      </button>
                    </div>
                  </Transition>
                </div>
              </div>
              
              <div class="w-12 h-12 rounded-xl bg-white/20 backdrop-blur-sm 
                          flex items-center justify-center">
                <i class="fas fa-glass-martini-alt text-xl text-white"></i>
              </div>
            </div>
            
            <!-- Card Body -->
            <div class="p-5">
              <h3 class="text-lg font-semibold text-slate-900 mb-2 group-hover:text-bar-600 transition-colors">
                {{ bar.name }}
              </h3>
              
              <div class="flex items-center gap-4 text-sm text-slate-500 mb-4">
                <span class="flex items-center gap-1.5">
                  <i class="fas fa-map-marker-alt text-slate-400"></i>
                  {{ bar.location || 'No location' }}
                </span>
                <span class="flex items-center gap-1.5">
                  <i class="fas fa-users text-slate-400"></i>
                  {{ bar.maxCapacity || 0 }}
                </span>
              </div>
              
              <router-link
                :to="`/bars/${bar.id}`"
                class="flex items-center justify-center gap-2 w-full py-2.5 
                       bg-bar-50 text-bar-600 rounded-lg font-medium
                       hover:bg-bar-100 transition-colors"
              >
                <i class="fas fa-door-open"></i>
                <span>Open Bar</span>
              </router-link>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="card p-12 text-center">
          <div class="w-20 h-20 rounded-2xl bg-bar-100 flex items-center justify-center mx-auto mb-6">
            <i class="fas fa-glass-martini-alt text-3xl text-bar-400"></i>
          </div>
          <h3 class="text-xl font-semibold text-slate-900 mb-2">No bars yet</h3>
          <p class="text-slate-500 mb-6 max-w-sm mx-auto">
            Get started by creating your first bar to manage inventory and sales.
          </p>
          <button @click="showAddBarModal = true" class="btn-primary">
            <i class="fas fa-plus"></i>
            <span>Create Your First Bar</span>
          </button>
        </div>
      </main>
    </div>

    <!-- Modals -->
    <AddBarModal
      :is-open="showAddBarModal"
      @close="showAddBarModal = false"
      @bar-added="barStore.fetchBars"
    />
    <DrinkCatalogModal
      :is-open="showDrinkCatalogModal"
      @close="showDrinkCatalogModal = false"
    />
  </div>

  <!-- Click outside handler for menus -->
  <div 
    v-if="activeMenu"
    class="fixed inset-0 z-10"
    @click="activeMenu = null"
  ></div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useBarStore } from '../../stores/barStore.ts';
import Sidebar from '../../components/common/Sidebar.vue';
import Navbar from '../../components/common/Navbar.vue';
import AddBarModal from '../../components/bars/AddBarModal.vue';
import DrinkCatalogModal from '../../components/bars/DrinkCatalogModal.vue';

const barStore = useBarStore();
const showAddBarModal = ref(false);
const showDrinkCatalogModal = ref(false);
const activeMenu = ref(null);

const totalCapacity = computed(() => {
  return barStore.bars.reduce((sum, bar) => sum + (bar.maxCapacity || 0), 0);
});

const avgCapacity = computed(() => {
  if (barStore.bars.length === 0) return 0;
  return Math.round(totalCapacity.value / barStore.bars.length);
});

onMounted(() => {
  barStore.fetchBars();
});

const toggleMenu = (barId) => {
  activeMenu.value = activeMenu.value === barId ? null : barId;
};

const editBar = (barId) => {
  activeMenu.value = null;
  console.log('Edit bar:', barId);
};

const deleteBar = async (barId) => {
  activeMenu.value = null;
  if (confirm('Are you sure you want to delete this bar?')) {
    await barStore.deleteBar(barId);
  }
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