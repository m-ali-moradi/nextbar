<template>
  <!-- Stats -->
  <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Pending Pickups</p>
          <p class="text-2xl font-bold text-amber-600 mt-1">{{ pendingCount }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-amber-100 flex items-center justify-center">
          <i class="fas fa-clock text-xl text-amber-600"></i>
        </div>
      </div>
    </div>
    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Accepted</p>
          <p class="text-2xl font-bold text-blue-600 mt-1">{{ acceptedCount }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-blue-100 flex items-center justify-center">
          <i class="fas fa-hand-paper text-xl text-blue-600"></i>
        </div>
      </div>
    </div>
    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Total Collected</p>
          <p class="text-2xl font-bold text-emerald-600 mt-1">{{ totalCollected }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-emerald-100 flex items-center justify-center">
          <i class="fas fa-recycle text-xl text-emerald-600"></i>
        </div>
      </div>
    </div>
  </div>

  <!-- Loading -->
  <BaseLoadingSpinner v-if="loading" color="droppoint" message="Loading collections..." />

  <template v-else>
    <!-- Collections Lifecycle -->
    <div class="card overflow-hidden mb-6">
      <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
        <div>
          <h2 class="font-semibold text-slate-900">Collection Requests</h2>
          <p class="text-sm text-slate-500">Manage drop-point pickup notifications</p>
        </div>
        <div class="flex gap-2">
          <!-- Sync removed: use Refresh to pull latest data -->
          <button @click="fetchData" :disabled="loading" class="btn-secondary text-sm py-2">
            <i class="fas fa-sync-alt text-xs"></i> Refresh
          </button>
        </div>
      </div>

      <div class="px-6 py-3 border-b border-slate-50 flex flex-wrap gap-2 bg-slate-50/50">
        <button
          v-for="tab in statusTabs" :key="tab.value"
          @click="collectionFilter = tab.value"
          class="px-3 py-1.5 rounded-lg text-xs font-semibold transition-all"
          :class="collectionFilter === tab.value
            ? 'bg-white text-slate-900 shadow-sm'
            : 'text-slate-500 hover:text-slate-700'"
        >
          {{ tab.label }}
          <span v-if="tab.count > 0" class="ml-1 px-1.5 py-0.5 rounded-full text-[10px] bg-slate-200">{{ tab.count }}</span>
        </button>
      </div>

      <div v-if="filteredCollections.length" class="overflow-x-auto">
        <table class="table-modern">
          <thead>
            <tr>
              <th>ID</th>
              <th>Drop Point</th>
              <th>Bottles</th>
              <th>Status</th>
              <th>Notified</th>
              <th class="text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in filteredCollections" :key="c.id">
              <td class="font-mono text-slate-500">#{{ c.id }}</td>
              <td>
                <p class="font-medium text-slate-900">DP #{{ c.dropPointId }}</p>
                <p class="text-xs text-slate-500">{{ c.location || '—' }}</p>
              </td>
              <td class="font-semibold text-slate-900">{{ c.bottleCount }}</td>
              <td>
                <span class="badge" :class="collectionStatusBadge(c.status)">
                  {{ formatCollectionStatus(c.status) }}
                </span>
              </td>
              <td class="text-sm text-slate-500">{{ fmtDate(c.notifiedAt) }}</td>
              <td>
                <div class="flex items-center justify-end gap-1.5">
                  <button
                    v-if="c.status === 'PENDING'"
                    @click="acceptCollectionAction(c)"
                    class="btn-primary text-sm py-1.5 px-3"
                    :disabled="c._loading"
                  >
                    <i class="fas fa-hand-paper text-xs"></i> Accept
                  </button>
                  <button
                    v-if="c.status === 'ACCEPTED'"
                    @click="completeCollectionAction(c)"
                    class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
                    :disabled="c._loading"
                  >
                    <i class="fas fa-check text-xs"></i> Complete
                  </button>
                  <span v-if="c.status === 'COLLECTED'" class="text-emerald-600 text-sm font-medium"><i class="fas fa-check-double"></i></span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <BaseEmptyState
        v-else
        icon="fas fa-recycle"
        :color="collectionFilter ? 'slate' : 'warehouse'"
        :title="collectionFilter ? 'No collections with this status' : 'No collection records'"
        description="Use &quot;Sync&quot; to pull new notifications from drop points"
      />
    </div>

    <!-- Inventory Summary -->
    <div class="card overflow-hidden">
      <div class="px-6 py-4 border-b border-slate-100">
        <h2 class="font-semibold text-slate-900">Empties Inventory Summary</h2>
        <p class="text-sm text-slate-500">Aggregate bottles by drop point location</p>
      </div>
      <div v-if="inventory.length" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 p-6">
        <div v-for="inv in inventory" :key="inv.id || inv.dropPointId" class="rounded-xl border border-slate-200 p-4 hover:shadow-sm transition-shadow">
          <div class="flex items-center gap-3 mb-3">
            <div class="w-10 h-10 rounded-lg bg-droppoint-100 flex items-center justify-center">
              <i class="fas fa-map-marker-alt text-droppoint-600"></i>
            </div>
            <div>
              <p class="font-semibold text-slate-900">DP #{{ inv.dropPointId }}</p>
              <p class="text-xs text-slate-500">{{ inv.location || inv.dropPointLocation || '—' }}</p>
            </div>
          </div>
          <div class="flex items-center justify-between">
            <div>
              <p class="text-2xl font-bold text-slate-900">{{ inv.emptiesCount || inv.totalBottlesCollected }}</p>
              <p class="text-xs text-slate-500">bottles collected</p>
            </div>
            <p class="text-xs text-slate-400">{{ fmtDate(inv.collectionDate || inv.lastCollectionAt) }}</p>
          </div>
        </div>
      </div>
      <div v-else class="p-12 text-center">
        <p class="text-slate-500">No inventory data yet</p>
      </div>
    </div>
  </template>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { toast } from 'vue3-toastify';
import { formatDateShort as fmtDate } from '@/composables/useDateFormat';
import {
  useCollectionsQuery,
  useCollectionInventoryQuery,
  useAcceptCollection,
  useCompleteCollection,
} from '@/composables/queries/useWarehouseQueries';
import BaseLoadingSpinner from '@/components/common/BaseLoadingSpinner.vue';
import BaseEmptyState from '@/components/common/BaseEmptyState.vue';

// Queries
const { data: collectionsData, isLoading: collectionsLoading, refetch: refetchCollections } = useCollectionsQuery();
const { data: inventoryData, isLoading: inventoryLoading, refetch: refetchInventory } = useCollectionInventoryQuery();

const collections = computed(() => collectionsData.value ?? []);
const inventory = computed(() => inventoryData.value ?? []);
const loading = computed(() => collectionsLoading.value || inventoryLoading.value);

// Mutations
const acceptMutation = useAcceptCollection();
const completeMutation = useCompleteCollection();
const collectionFilter = ref('');

const pendingCount = computed(() => collections.value.filter(c => c.status === 'PENDING').length);
const acceptedCount = computed(() => collections.value.filter(c => c.status === 'ACCEPTED').length);
const totalCollected = computed(() => {
  const fromInv = inventory.value.reduce((s, i) => s + (i.emptiesCount || i.totalBottlesCollected || 0), 0);
  const fromColl = collections.value.filter(c => c.status === 'COLLECTED').reduce((s, c) => s + (c.bottleCount || 0), 0);
  return fromInv || fromColl;
});

const statusTabs = computed(() => [
  { value: '', label: 'All', count: collections.value.length },
  { value: 'PENDING', label: 'Pending', count: pendingCount.value },
  { value: 'ACCEPTED', label: 'Accepted', count: acceptedCount.value },
  { value: 'COLLECTED', label: 'Collected', count: collections.value.filter(c => c.status === 'COLLECTED').length },
]);

const filteredCollections = computed(() => {
  if (!collectionFilter.value) return collections.value;
  return collections.value.filter(c => c.status === collectionFilter.value);
});

function collectionStatusBadge(s: string) {
  return ({ PENDING: 'badge-warning', ACCEPTED: 'badge-info', COLLECTED: 'badge-success', RESET: 'badge-neutral' } as Record<string, string>)[s] || 'badge-neutral';
}

function formatCollectionStatus(s: string) {
  return ({ PENDING: 'Pending', ACCEPTED: 'Accepted', COLLECTED: 'Collected', RESET: 'Reset' } as Record<string, string>)[s] || s;
}

async function fetchData() {
  await Promise.all([refetchCollections(), refetchInventory()]);
}

async function acceptCollectionAction(c: any) {
  c._loading = true;
  try {
    await acceptMutation.mutateAsync(c.id);
    toast.success(`Collection #${c.id} accepted`);
  } catch (e: any) {
    toast.error(e?.message ?? 'Failed');
  } finally {
    c._loading = false;
  }
}

async function completeCollectionAction(c: any) {
  c._loading = true;
  try {
    await completeMutation.mutateAsync(c.id);
    toast.success(`Collection #${c.id} completed`);
  } catch (e: any) {
    toast.error(e?.message ?? 'Failed');
  } finally {
    c._loading = false;
  }
}
</script>
