<template>
  <!-- Stats -->
  <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Pending</p>
          <p class="text-2xl font-bold text-blue-600 mt-1">{{ counts.pending }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-blue-100 flex items-center justify-center">
          <i class="fas fa-inbox text-xl text-blue-600"></i>
        </div>
      </div>
    </div>
    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">In Progress</p>
          <p class="text-2xl font-bold text-amber-600 mt-1">{{ counts.inProgress }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-amber-100 flex items-center justify-center">
          <i class="fas fa-spinner text-xl text-amber-600"></i>
        </div>
      </div>
    </div>
    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Delivered</p>
          <p class="text-2xl font-bold text-emerald-600 mt-1">{{ counts.delivered }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-emerald-100 flex items-center justify-center">
          <i class="fas fa-check-circle text-xl text-emerald-600"></i>
        </div>
      </div>
    </div>
    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Rejected</p>
          <p class="text-2xl font-bold text-rose-600 mt-1">{{ counts.rejected }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-rose-100 flex items-center justify-center">
          <i class="fas fa-times-circle text-xl text-rose-600"></i>
        </div>
      </div>
    </div>
  </div>

  <!-- Loading -->
  <BaseLoadingSpinner v-if="loading" color="warehouse" message="Loading supply requests..." />

  <!-- Table Card -->
  <div v-else class="card overflow-hidden">
    <div class="px-6 py-4 border-b border-slate-100 space-y-3">
      <div class="flex items-center justify-between">
        <h2 class="font-semibold text-slate-900">Supply Requests</h2>
        <button @click="refetchRequests()" class="btn-secondary text-sm py-2" :disabled="loading">
          <i class="fas fa-sync-alt text-xs"></i> Refresh
        </button>
      </div>
      <div class="flex flex-wrap items-center gap-3">
        <div class="relative">
          <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"></i>
          <input v-model="search" type="text" placeholder="Search bar, product, ID..." class="input pl-10 py-2 w-64" />
        </div>
        <select v-model="statusFilter" class="input py-2 w-40">
          <option value="">All Statuses</option>
          <option value="REQUESTED">Requested</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="DELIVERED">Delivered</option>
          <option value="REJECTED">Rejected</option>
        </select>
        <select v-model="barFilter" class="input py-2 w-48">
          <option value="">All Bars</option>
          <option v-for="bar in bars" :key="bar.id" :value="String(bar.id)">
            {{ bar.name }}
          </option>
        </select>
      </div>
    </div>

    <div v-if="filtered.length" class="overflow-x-auto">
      <table class="table-modern">
        <thead>
          <tr>
            <th>ID</th>
            <th>Bar</th>
            <th>Items</th>
            <th>Total Qty</th>
            <th>Status</th>
            <th>Requested</th>
            <th class="text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="req in filtered" :key="req.id">
            <td class="font-mono text-slate-500">#{{ req.id }}</td>
            <td>
              <p class="font-medium text-slate-900">{{ barName(req) }}</p>
              <p class="text-xs text-slate-500">{{ barLocation(req) }}</p>
            </td>
            <td :title="itemsDetail(req)">
              <p class="text-sm text-slate-900">{{ itemsSummary(req) }}</p>
            </td>
            <td class="font-semibold text-slate-900">{{ totalQty(req) }}</td>
            <td>
              <span class="badge" :class="statusBadge(req.status)">{{ formatStatus(req.status) }}</span>
              <p v-if="req.status === 'REJECTED' && req.rejectionReason" class="text-xs text-rose-600 mt-1 max-w-[160px] truncate" :title="req.rejectionReason">
                {{ req.rejectionReason }}
              </p>
            </td>
            <td class="text-sm text-slate-500">{{ fmtDate(req.createdAt || req.requestedAt) }}</td>
            <td>
              <div class="flex items-center justify-end gap-1.5">
                <button v-if="req.status === 'REQUESTED'" @click="startRequest(req)" class="btn-primary text-sm py-1.5 px-3">
                  <i class="fas fa-play text-xs"></i> Start
                </button>
                <button v-if="req.status === 'REQUESTED'" @click="openRejectModal(req)" class="btn-danger text-sm py-1.5 px-3">
                  <i class="fas fa-ban text-xs"></i> Reject
                </button>
                <button v-if="req.status === 'IN_PROGRESS'" @click="deliverRequest(req)"
                  class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
                >
                  <i class="fas fa-truck text-xs"></i> Deliver
                </button>
                <span v-if="req.status === 'DELIVERED'" class="text-emerald-600 text-sm font-medium">
                  <i class="fas fa-check"></i> Done
                </span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="p-12 text-center">
      <div class="w-16 h-16 rounded-2xl bg-slate-100 flex items-center justify-center mx-auto mb-4">
        <i class="fas fa-inbox text-2xl text-slate-400"></i>
      </div>
      <p class="text-slate-500 font-medium">{{ search || statusFilter || barFilter ? 'No requests match your filters' : 'No supply requests' }}</p>
    </div>
  </div>

  <!-- Reject Modal -->
  <Transition name="modal">
    <div v-if="showRejectModal" class="modal-overlay" @click.self="showRejectModal = false">
      <div class="modal-content max-w-md">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <h2 class="text-lg font-semibold text-slate-900">Reject Request</h2>
          <button @click="showRejectModal = false" class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <form @submit.prevent="submitReject" class="p-6 space-y-4">
          <div>
            <label class="label">Rejection Reason</label>
            <textarea v-model="rejectReason" class="input" rows="3" placeholder="e.g., Out of stock, supplier delay" required maxlength="255"></textarea>
            <p class="text-xs text-slate-400 mt-1">This reason will be shared with the bar.</p>
          </div>
          <div class="flex flex-wrap gap-2">
            <button type="button" v-for="r in quickReasons" :key="r" @click="rejectReason = r" class="text-xs px-3 py-1.5 rounded-lg bg-slate-100 text-slate-600 hover:bg-slate-200 transition-colors">
              {{ r }}
            </button>
          </div>
          <div class="flex gap-3 pt-2">
            <button type="button" @click="showRejectModal = false" class="btn-secondary flex-1">Cancel</button>
            <button type="submit" class="btn-danger flex-1" :disabled="!rejectReason.trim() || rejecting">
              <template v-if="rejecting"><i class="fas fa-spinner fa-spin"></i></template>
              <template v-else><i class="fas fa-ban"></i> Reject</template>
            </button>
          </div>
        </form>
      </div>
    </div>
  </Transition>

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

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useQueryClient } from '@tanstack/vue-query';
import { useSupplyRequestsQuery, useWarehouseBarsQuery, useFulfillSupplyRequest, useRejectSupplyRequest } from '@/composables/queries/useWarehouseQueries';
import { queryKeys } from '@/composables/queries/queryKeys';
import { useWebSocketEvents } from '@/composables/useWebSocketEvents';
import ConfirmDialog from '@/components/common/ConfirmDialog.vue';
import { useConfirmDialog } from '@/composables/useConfirmDialog';
import { toast } from 'vue3-toastify';
import { formatDateShort as fmtDate } from '@/composables/useDateFormat';
import BaseLoadingSpinner from '@/components/common/BaseLoadingSpinner.vue';

const emit = defineEmits(['pending-count']);
const queryClient = useQueryClient();
const { eventsByType } = useWebSocketEvents();

const { data: requestsData, isLoading: requestsLoading, refetch: refetchRequests } = useSupplyRequestsQuery();
const { data: barsData, isLoading: barsLoading } = useWarehouseBarsQuery();
const requests = computed(() => requestsData.value ?? []);
const bars = computed(() => barsData.value ?? []);
const loading = computed(() => requestsLoading.value || barsLoading.value);

const search = ref('');
const statusFilter = ref('');
const barFilter = ref('');

const showRejectModal = ref(false);
const rejectReason = ref('');
const rejectTarget = ref<any>(null);
const rejecting = ref(false);
const quickReasons = ['Out of stock', 'Supplier delay', 'Quality issue', 'Discontinued'];

const { state: confirmDialog, open: openConfirm, close: closeConfirm } = useConfirmDialog();
const fulfillMutation = useFulfillSupplyRequest();
const rejectMutation = useRejectSupplyRequest();

const counts = computed(() => ({
  pending: requests.value.filter(r => r.status === 'REQUESTED').length,
  inProgress: requests.value.filter(r => r.status === 'IN_PROGRESS').length,
  delivered: requests.value.filter(r => r.status === 'DELIVERED').length,
  rejected: requests.value.filter(r => r.status === 'REJECTED').length,
}));

// Emit pending count to layout for badge
watch(() => counts.value.pending, c => emit('pending-count', c), { immediate: true });

const filtered = computed(() => {
  const q = search.value.toLowerCase().trim();
  return requests.value.filter(r => {
    if (statusFilter.value && r.status !== statusFilter.value) return false;
    if (barFilter.value && String(r.barId) !== barFilter.value) return false;
    if (!q) return true;
    const haystack = [barName(r), barLocation(r), ...r.items.map(i => i.productName || i.productId), String(r.id)].join(' ').toLowerCase();
    return haystack.includes(q);
  });
});

function barName(r) {
  if (r.barName) return r.barName;
  const b = bars.value.find(b => String(b.id) === String(r.barId));
  return b ? b.name : `Bar #${r.barId}`;
}

function barLocation(r) {
  const b = bars.value.find(b => String(b.id) === String(r.barId));
  return b ? b.location : '';
}

function itemsSummary(r) {
  if (!r.items?.length) return 'No items';
  const first = r.items.slice(0, 2).map(i => `${i.productName || i.productId} ×${i.quantity}`);
  const rest = r.items.length - first.length;
  return rest > 0 ? `${first.join(', ')} +${rest}` : first.join(', ');
}

function itemsDetail(r) {
  return (r.items || []).map(i => `${i.productName || i.productId} ×${i.quantity}`).join(', ');
}

function totalQty(r) {
  return (r.items || []).reduce((s, i) => s + (i.quantity || 0), 0);
}

function statusBadge(s) {
  return { REQUESTED: 'badge-info', IN_PROGRESS: 'badge-warning', DELIVERED: 'badge-success', REJECTED: 'badge-danger' }[s] || 'badge-neutral';
}

function formatStatus(s) {
  return { REQUESTED: 'Requested', IN_PROGRESS: 'In Progress', DELIVERED: 'Delivered', REJECTED: 'Rejected' }[s] || s;
}

function startRequest(req: any) {
  const bev = req.items[0]?.productName || req.items[0]?.productId;
  if (!bev) { toast.error('Missing product info'); return; }
  openConfirm({
    title: 'Start Processing',
    message: `Move request #${req.id} to In Progress?`,
    confirmText: 'Start',
    variant: 'warning',
    onConfirm: async () => {
      await fulfillMutation.mutateAsync({ barId: req.barId, requestId: req.id, payload: { beverageType: bev, quantity: totalQty(req), currentStatus: req.status } });
      toast.success('Request started');
    },
  });
}

function deliverRequest(req: any) {
  const bev = req.items[0]?.productName || req.items[0]?.productId;
  if (!bev) { toast.error('Missing product info'); return; }
  openConfirm({
    title: 'Mark as Delivered',
    message: `Confirm delivery for request #${req.id}?`,
    confirmText: 'Deliver',
    variant: 'info',
    onConfirm: async () => {
      await fulfillMutation.mutateAsync({ barId: req.barId, requestId: req.id, payload: { beverageType: bev, quantity: totalQty(req), currentStatus: req.status } });
      toast.success('Request delivered');
    },
  });
}

function openRejectModal(req) {
  rejectTarget.value = req;
  rejectReason.value = '';
  showRejectModal.value = true;
}

async function submitReject() {
  if (!rejectTarget.value) return;
  rejecting.value = true;
  try {
    await rejectMutation.mutateAsync({ barId: rejectTarget.value.barId, requestId: rejectTarget.value.id, reason: rejectReason.value.trim() });
    toast.warning('Request rejected');
    showRejectModal.value = false;
  } catch (e: any) { toast.error(e?.message ?? 'Failed to reject'); }
  finally { rejecting.value = false; }
}

// WebSocket auto-refresh
watch(() => eventsByType['SUPPLY_REQUEST_CREATED'], (e) => {
  if (e) {
    toast.info('New request — refreshing...');
    queryClient.invalidateQueries({ queryKey: queryKeys.warehouse.supply });
  }
});
watch(() => eventsByType['SUPPLY_REQUEST_UPDATED'], (e) => {
  if (e) {
    toast.info('Request updated — refreshing...');
    queryClient.invalidateQueries({ queryKey: queryKeys.warehouse.supply });
  }
});
</script>

<style scoped>
.modal-enter-active, .modal-leave-active { transition: all 0.3s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-from .modal-content, .modal-leave-to .modal-content { transform: scale(0.95); }
</style>
