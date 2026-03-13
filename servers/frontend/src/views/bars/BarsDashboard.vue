<template>
  <div>
        <!-- Page Header -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-bold text-slate-900">Bars Dashboard</h1>
            <p class="text-slate-500 mt-1">Manage your bars and inventory</p>
          </div>
          <button v-if="canCreateLocalBar" @click="showAddBarModal = true" class="btn-primary">
            <i class="fas fa-plus"></i>
            <span>Create Local Bar</span>
          </button>
        </div>

        <!-- Stats Overview -->
        <div class="mb-8">
          <!-- If user is not admin/manager and has no assigned resource -> hide message -->
          <div v-if="!isAdminOrManager && !hasAssignedResource" class="hidden">
            <!-- No stats to show, but we won't display an empty state here since the user might have access to bars without being assigned to one -->
          </div>

          <!-- If user has an assigned resource but is not global admin/manager -> show only user's resource capacity -->
          <div v-else-if="hasAssignedResource && !isAdminOrManager" class="grid grid-cols-1 sm:grid-cols-1 lg:grid-cols-1 gap-4">
            <div class="card p-5 card-interactive">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-slate-500 font-medium">Your Resource Capacity</p>
                  <p class="text-2xl font-bold text-slate-900 mt-1">{{ assignedResourceCapacity }}</p>
                </div>
                <div class="w-12 h-12 rounded-xl bg-violet-100 flex items-center justify-center">
                  <i class="fas fa-users text-xl text-violet-600"></i>
                </div>
              </div>
            </div>
          </div>

          <!-- Admins or managers see the full stats -->
          <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <div class="card p-5 card-interactive">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm text-slate-500 font-medium">Total Bars</p>
                  <p class="text-2xl font-bold text-slate-900 mt-1">{{ bars.length }}</p>
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
                  <p class="text-2xl font-bold text-slate-900 mt-1">{{ activeBars.length }}</p>
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
        </div>

        <!-- Loading State -->
        <BaseLoadingSpinner v-if="isLoading" color="bar" message="Loading bars..." />

        <!-- Error State -->
        <div 
          v-else-if="error" 
          class="card p-8 text-center"
        >
          <div class="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4">
            <i class="fas fa-exclamation-triangle text-2xl text-red-600"></i>
          </div>
          <h3 class="text-lg font-semibold text-slate-900 mb-2">Error Loading Bars</h3>
          <p class="text-slate-500 mb-4">{{ error }}</p>
          <button @click="refetch()" class="btn-primary">
            <i class="fas fa-redo"></i>
            <span>Try Again</span>
          </button>
        </div>

        <!-- Bars Grid (active + inactive) -->
        <div v-else-if="activeBars.length || inactiveBars.length">
          <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
            <div
              v-for="bar in activeBars"
              :key="bar.id"
              class="card group card-interactive overflow-hidden"
            >
              <!-- Card Header with Gradient -->
              <div class="h-24 bg-gradient-to-br from-bar-500 to-bar-700 p-5 relative">
                <div class="absolute top-3 right-3 flex items-center space-x-2">
                  <span v-if="bar.eventName" class="inline-flex items-center rounded-lg bg-white/20 px-2 py-1 text-xs font-medium text-white">
                    Event: {{ bar.eventName }}
                  </span>
                  <span class="inline-flex items-center rounded-lg bg-white/20 px-2 py-1 text-xs font-medium text-white">
                    {{ getStatusLabel(bar) }}
                  </span>
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

          <div v-if="inactiveBars.length" class="mt-8">
            <h2 class="text-lg font-semibold text-slate-900 mb-4">Inactive Bars</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
              <div v-for="bar in inactiveBars" :key="`inactive-${bar.id}`" class="card overflow-hidden opacity-75">
                <div class="h-24 bg-gradient-to-br from-slate-500 to-slate-700 p-5 relative">
                  <div class="absolute top-3 right-3 flex items-center space-x-2">
                    <span v-if="bar.eventName" class="inline-flex items-center rounded-lg bg-white/20 px-2 py-1 text-xs font-medium text-white">
                      Event: {{ bar.eventName }}
                    </span>
                    <span class="inline-flex items-center rounded-lg bg-white/20 px-2 py-1 text-xs font-medium text-white">
                      Inactive
                    </span>
                  </div>
                  <div class="w-12 h-12 rounded-xl bg-white/20 backdrop-blur-sm flex items-center justify-center">
                    <i class="fas fa-glass-martini-alt text-xl text-white"></i>
                  </div>
                </div>
                <div class="p-5">
                  <h3 class="text-lg font-semibold text-slate-900 mb-2">{{ bar.name }}</h3>
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
                  <button
                    type="button"
                    disabled
                    class="flex items-center justify-center gap-2 w-full py-2.5 bg-slate-100 text-slate-500 rounded-lg font-medium cursor-not-allowed"
                  >
                    <i class="fas fa-ban"></i>
                    <span>Bar Inactive</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <BaseEmptyState
          v-else
          icon="fas fa-glass-martini-alt"
          color="bar"
          title="No bars yet"
          description="Maybe, you have not been assigned to any bar."
        >
          <button v-if="canCreateLocalBar" @click="showAddBarModal = true" class="btn-primary inline-flex items-center gap-2">
            <i class="fas fa-plus"></i>
            <span>Create Local Bar</span>
          </button>
        </BaseEmptyState>

    <!-- Modals -->
    <AddBarModal
      :is-open="showAddBarModal"
      @close="showAddBarModal = false"
      @bar-added="handleBarAdded"
    />
    <DrinkCatalogModal
      :is-open="showDrinkCatalogModal"
      @close="showDrinkCatalogModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useQueryClient } from '@tanstack/vue-query';
import { useBarsQuery } from '@/composables/queries/useBarQueries';
import { queryKeys } from '@/composables/queries/queryKeys';
import { useAuthStore } from '@/stores/authStore';
import AddBarModal from '@/components/bars/AddBarModal.vue';
import DrinkCatalogModal from '@/components/bars/DrinkCatalogModal.vue';
import BaseLoadingSpinner from '@/components/common/BaseLoadingSpinner.vue';
import BaseEmptyState from '@/components/common/BaseEmptyState.vue';

const queryClient = useQueryClient();
const { data: barsData, isLoading, error, refetch } = useBarsQuery();
const bars = computed(() => barsData.value ?? []);

const showAddBarModal = ref(false);
const showDrinkCatalogModal = ref(false);

const totalCapacity = computed(() => {
  return bars.value.reduce((sum, bar) => sum + (bar.maxCapacity || 0), 0);
});

const activeBars = computed(() =>
  bars.value.filter(bar => (bar.eventStatus || '').toUpperCase() !== 'COMPLETED')
);

const inactiveBars = computed(() =>
  bars.value.filter(bar => (bar.eventStatus || '').toUpperCase() === 'COMPLETED')
);

const getStatusLabel = (bar) => {
  const status = (bar.eventStatus || '').toUpperCase();
  if (status === 'RUNNING') return 'Running Event';
  if (status === 'COMPLETED') return 'NOW CLOSED';
  return 'Local Bar';
};

const authStore = useAuthStore();

const isAdminOrManager = computed(() => {
  const u = authStore.user;
  if (!u) return false;
  if (u.isAdmin) return true;
  return Array.isArray(u.roles) && u.roles.some(r => r?.role === 'MANAGER');
});

const canCreateLocalBar = computed(() => {
  const u = authStore.user;
  if (!u) return false;
  if (u.isAdmin) return true;
  return Array.isArray(u.roles) && u.roles.some(r => r?.service === 'BAR' && r?.role === 'MANAGER');
});

const assignedRoles = computed(() => {
  const u = authStore.user;
  if (!u || !Array.isArray(u.roles)) return [];
  return u.roles.filter(r => r && r.resourceId);
});

const hasAssignedResource = computed(() => assignedRoles.value.length > 0);

const assignedResourceCapacity = computed(() => {
  const assigned = assignedRoles.value.map(r => r.resourceId).filter(Boolean);
  if (!assigned.length) return 0;
  return bars.value
    .filter(b => assigned.includes(b.id))
    .reduce((s, b) => s + (b.maxCapacity || 0), 0);
});

const handleBarAdded = () => {
  showAddBarModal.value = false;
  queryClient.invalidateQueries({ queryKey: queryKeys.bars.all });
};
</script>