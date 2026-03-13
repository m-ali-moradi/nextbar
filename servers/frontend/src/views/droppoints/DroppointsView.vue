<template>
  <div>
        <!-- Page Header -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-bold text-slate-900">Drop Points</h1>
            <p class="text-slate-500 mt-1">Manage bottle collection points</p>
          </div>
        </div>

        <!-- Stats Overview -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Total Points</p>
                <p class="text-2xl font-bold text-slate-900 mt-1">{{ droppoints.length }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-droppoint-100 flex items-center justify-center">
                <i class="fas fa-map-marker-alt text-xl text-droppoint-600"></i>
              </div>
            </div>
          </div>
          
          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Empty</p>
                <p class="text-2xl font-bold text-blue-600 mt-1">{{ emptyCount }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-blue-100 flex items-center justify-center">
                <i class="fas fa-check text-xl text-blue-600"></i>
              </div>
            </div>
          </div>

          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Full</p>
                <p class="text-2xl font-bold text-amber-600 mt-1">{{ fullCount }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-amber-100 flex items-center justify-center">
                <i class="fas fa-exclamation-circle text-xl text-amber-600"></i>
              </div>
            </div>
          </div>

          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Notified</p>
                <p class="text-2xl font-bold text-violet-600 mt-1">{{ notifiedCount }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-violet-100 flex items-center justify-center">
                <i class="fas fa-bell text-xl text-violet-600"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- Loading State -->
        <BaseLoadingSpinner v-if="loading" color="droppoint" message="Loading drop points..." />

        <!-- Error State -->
        <div v-else-if="error" class="card p-8 text-center">
          <div class="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4">
            <i class="fas fa-exclamation-triangle text-2xl text-red-600"></i>
          </div>
          <h3 class="text-lg font-semibold text-slate-900 mb-2">Error Loading Data</h3>
          <p class="text-slate-500 mb-4">{{ error }}</p>
          <button @click="refetch()" class="btn-primary">
            <i class="fas fa-redo"></i>
            <span>Try Again</span>
          </button>
        </div>

        <!-- Drop Points Table -->
        <div v-else-if="droppoints.length > 0" class="card overflow-hidden">
          <div class="px-6 py-4 border-b border-slate-100">
            <h2 class="font-semibold text-slate-900">All Drop Points</h2>
          </div>
          
          <div class="overflow-x-auto">
            <table class="table-modern">
              <thead>
                <tr>
                  <th>Location</th>
                  <th>Capacity</th>
                  <th>Current Fill</th>
                  <th>Status</th>
                  <th class="text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="droppoint in droppoints" :key="droppoint.id">
                  <td>
                    <div class="flex items-center gap-3">
                      <div class="w-10 h-10 rounded-xl bg-droppoint-100 flex items-center justify-center">
                        <i class="fas fa-map-marker-alt text-droppoint-600"></i>
                      </div>
                      <div>
                        <p class="font-medium text-slate-900">{{ droppoint.location }}</p>
                        <p class="text-xs text-slate-500">#{{ droppoint.id }}</p>
                      </div>
                    </div>
                  </td>
                  <td>{{ droppoint.capacity }} bottles</td>
                  <td>
                    <div class="flex items-center gap-3">
                      <div class="flex-1 h-2 bg-slate-100 rounded-full overflow-hidden max-w-[120px]">
                        <div 
                          class="h-full rounded-full transition-all duration-300"
                          :class="getFillBarClass(droppoint)"
                          :style="{ width: getFillPercentage(droppoint) + '%' }"
                        ></div>
                      </div>
                      <span class="text-sm font-medium text-slate-600">
                        {{ droppoint.current_empties }}/{{ droppoint.capacity }}
                      </span>
                    </div>
                  </td>
                  <td>
                    <span class="badge" :class="getStatusBadgeClass(droppoint.status)">
                      {{ formatStatus(droppoint.status) }}
                    </span>
                  </td>
                  <td>
                    <div class="flex items-center justify-end gap-2">
                      <button
                        v-if="isInactiveDropPoint(droppoint)"
                        type="button"
                        disabled
                        class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 text-slate-500 rounded-lg text-sm font-medium cursor-not-allowed"
                      >
                        <i class="fas fa-ban"></i>
                        Inactive
                      </button>

                      <!-- Action Buttons based on status -->
                      <button 
                        v-else-if="droppoint.status === 'EMPTY'" 
                        @click="handleAddEmpty(droppoint.id!)" 
                        class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-droppoint-600 text-white 
                               rounded-lg text-sm font-medium hover:bg-droppoint-700 transition-colors"
                      >
                        <i class="fas fa-plus"></i>
                        Add
                      </button>
                      <button 
                        v-else-if="droppoint.status === 'FULL'" 
                        @click="handleNotifyWarehouse(droppoint.id!)" 
                        class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-amber-500 text-white 
                               rounded-lg text-sm font-medium hover:bg-amber-600 transition-colors"
                      >
                        <i class="fas fa-bell"></i>
                        Notify
                      </button>
                      <button 
                        v-else-if="droppoint.status === 'FULL_AND_NOTIFIED_TO_WAREHOUSE'" 
                        @click="handleVerifyTransfer(droppoint.id!)" 
                        class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-violet-600 text-white 
                               rounded-lg text-sm font-medium hover:bg-violet-700 transition-colors"
                      >
                        <i class="fas fa-check"></i>
                        Verify
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Empty State -->
        <BaseEmptyState
          v-else
          icon="fas fa-map-marker-alt"
          color="droppoint"
          title="No drop point yet"
          description="Drop point configuration is managed in Event Planner."
        >
          <router-link v-if="canSeeEvents" to="/events" class="btn-primary inline-flex items-center gap-2">
            <i class="fas fa-plus"></i>
            <span>Go to Event Planner</span>
          </router-link>
        </BaseEmptyState>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useDroppointsQuery, useAddEmpties, useNotifyWarehouse, useVerifyTransfer } from '@/composables/queries/useDroppointQueries';
import { DropPoint } from '@/api/droppointApi';
import { useAuthStore } from '@/stores/authStore';
import { hasManagerRole } from '@/composables/useAccessControl';
import { toast } from 'vue3-toastify';
import BaseLoadingSpinner from '@/components/common/BaseLoadingSpinner.vue';
import BaseEmptyState from '@/components/common/BaseEmptyState.vue';

// Query (auto-fetches on mount)
const { data: droppointsData, isLoading: loading, error, refetch } = useDroppointsQuery();
const droppoints = computed(() => droppointsData.value ?? []);

// Mutations
const addEmptiesMutation = useAddEmpties();
const notifyWarehouseMutation = useNotifyWarehouse();
const verifyTransferMutation = useVerifyTransfer();
const authStore = useAuthStore();
const canSeeEvents = computed(() => hasManagerRole(authStore.user, 'EVENT'));

// Computed
const emptyCount = computed(() => droppoints.value.filter(d => d.status === 'EMPTY').length);
const fullCount = computed(() => droppoints.value.filter(d => d.status === 'FULL').length);
const notifiedCount = computed(() => droppoints.value.filter(d => d.status === 'FULL_AND_NOTIFIED_TO_WAREHOUSE').length);

// Methods
const getFillPercentage = (droppoint: DropPoint) => {
  if (!droppoint.capacity) return 0;
  return Math.min(100, (droppoint.current_empties / droppoint.capacity) * 100);
};

const getFillBarClass = (droppoint: DropPoint) => {
  const percentage = getFillPercentage(droppoint);
  if (percentage >= 90) return 'bg-red-500';
  if (percentage >= 70) return 'bg-amber-500';
  return 'bg-droppoint-500';
};

const getStatusBadgeClass = (status: string) => {
  switch (status) {
    case 'EMPTY': return 'badge-info';
    case 'FULL': return 'badge-warning';
    case 'FULL_AND_NOTIFIED_TO_WAREHOUSE': return 'bg-violet-100 text-violet-700';
    default: return 'badge-neutral';
  }
};

const formatStatus = (status: string) => {
  if (status === 'FULL_AND_NOTIFIED_TO_WAREHOUSE') return 'Notified';
  return status.replace(/_/g, ' ').toLowerCase()
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

const isInactiveDropPoint = (droppoint: DropPoint) => {
  return (droppoint.eventStatus || '').toUpperCase() === 'COMPLETED';
};

const handleAddEmpty = async (id: number) => {
  try {
    await addEmptiesMutation.mutateAsync(id);
    toast.success('Empty bottle added to drop point');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to add empty';
    toast.error(errorMessage);
  }
};

const handleNotifyWarehouse = async (id: number) => {
  if (!confirm('Notify warehouse to collect empties?')) return;

  try {
    await notifyWarehouseMutation.mutateAsync(id);
    toast.info('Warehouse notified successfully');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to notify warehouse';
    toast.error(errorMessage);
  }
};

const handleVerifyTransfer = async (id: number) => {
  if (!confirm('Verify that empties have been transferred to warehouse?')) return;

  try {
    await verifyTransferMutation.mutateAsync(id);
    toast.success('Transfer verified successfully');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to verify transfer';
    toast.error(errorMessage);
  }
};
</script>
