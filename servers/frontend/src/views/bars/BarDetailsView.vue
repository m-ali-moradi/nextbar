<template>
  <div>
        <!-- Page Header -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div class="flex items-center gap-4">
            <router-link
              to="/bars"
              class="p-2.5 rounded-xl text-slate-500 hover:text-slate-700 
                     hover:bg-slate-100 transition-colors"
              title="Back to Bars"
            >
              <i class="fas fa-arrow-left"></i>
            </router-link>
            <div>
              <h1 class="text-2xl font-bold text-slate-900">
                {{ bar?.name || 'Bar Details' }}
              </h1>
              <p class="text-slate-500 mt-1">
                {{ bar?.location || 'Manage stock and sales' }}
              </p>
            </div>
          </div>
          <button @click="showAddStockModal = true" class="btn-primary">
            <i class="fas fa-plus"></i>
            <span>Add Drink</span>
          </button>
        </div>

        <!-- Loading State -->
        <BaseLoadingSpinner v-if="isLoading" color="bar" message="Loading bar details..." />

        <!-- Error State -->
        <div v-else-if="error" class="card p-8 text-center">
          <div class="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4">
            <i class="fas fa-exclamation-triangle text-2xl text-red-600"></i>
          </div>
          <h3 class="text-lg font-semibold text-slate-900 mb-2">Error Loading Bar</h3>
          <p class="text-slate-500 mb-4">{{ error?.message ?? error }}</p>
          <button @click="refetch()" class="btn-primary">
            <i class="fas fa-redo"></i>
            <span>Try Again</span>
          </button>
        </div>

        <!-- Content -->
        <div v-else class="space-y-6">
          <!-- Stock Section -->
          <div class="card overflow-hidden">
            <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-bar-100 flex items-center justify-center">
                  <i class="fas fa-boxes text-bar-600"></i>
                </div>
                <div>
                  <h2 class="font-semibold text-slate-900">Stock Inventory</h2>
                  <p class="text-xs text-slate-500">{{ stock.length }} items</p>
                </div>
              </div>
            </div>

            <div v-if="stock.length" class="overflow-x-auto">
              <table class="table-modern">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Quantity</th>
                    <th>Last Update</th>
                    <th class="text-right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="item in sortedStock" :key="item.id">
                    <td>
                      <div class="flex items-center gap-3">
                        <div 
                          class="w-10 h-10 rounded-xl flex items-center justify-center"
                          :class="getProductBgClass(item.productName)"
                        >
                          <i class="fas fa-wine-bottle text-white text-sm"></i>
                        </div>
                        <span class="font-medium text-slate-900">
                          {{ item.productName ?? 'Unknown' }}
                        </span>
                      </div>
                    </td>
                    <td>
                      <span 
                        class="badge"
                        :class="getQuantityBadgeClass(item.quantity)"
                      >
                        {{ item.quantity === 0 ? 'Empty' : item.quantity }}
                      </span>
                    </td>
                    <td class="text-slate-500">{{ formatDate(getItemUpdatedAt(item)) }}</td>
                    <td>
                      <div class="flex items-center justify-end gap-2">
                        <button
                          v-if="item.quantity > 0"
                          @click="openSellModal(item)"
                          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-emerald-600 text-white 
                                 rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
                        >
                          <i class="fas fa-shopping-cart"></i>
                          Sell
                        </button>
                        <button
                          v-if="item.quantity === 0 && !hasPendingRequest(item.productName)"
                          @click="openRequestModal(item)"
                          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-amber-500 text-white 
                                 rounded-lg text-sm font-medium hover:bg-amber-600 transition-colors"
                        >
                          <i class="fas fa-truck"></i>
                          Request
                        </button>
                        <span
                          v-if="hasPendingRequest(item.productName)"
                          class="badge badge-warning"
                        >
                          <i class="fas fa-clock mr-1.5"></i>
                          Pending
                        </span>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-else class="p-8 text-center">
              <i class="fas fa-box-open text-4xl text-slate-300 mb-4"></i>
              <p class="text-slate-500">No stock available</p>
            </div>
          </div>

          <!-- Stats Cards -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <!-- Usage Logs -->
            <div class="card p-6">
              <div class="flex items-center gap-3 mb-4">
                <div class="w-10 h-10 rounded-xl bg-blue-100 flex items-center justify-center">
                  <i class="fas fa-history text-blue-600"></i>
                </div>
                <h2 class="font-semibold text-slate-900">Usage Logs</h2>
              </div>
              <div v-if="usageLogs.length" class="space-y-3 max-h-64 overflow-y-auto scrollbar-thin">
                <div
                  v-for="log in usageLogs.slice(0, 10)"
                  :key="log.id"
                  class="flex items-center justify-between py-2 border-b border-slate-100 last:border-0"
                >
                  <div>
                    <p class="font-medium text-slate-900 text-sm">{{ log.productName }}</p>
                    <p class="text-xs text-slate-500">{{ formatDate(log.timestamp) }}</p>
                  </div>
                  <span class="text-sm font-semibold text-slate-700">-{{ log.quantity }}</span>
                </div>
              </div>
              <p v-else class="text-slate-500 text-sm">No usage logs available</p>
            </div>

            <!-- Total Served -->
            <div class="card p-6">
              <div class="flex items-center gap-3 mb-4">
                <div class="w-10 h-10 rounded-xl bg-emerald-100 flex items-center justify-center">
                  <i class="fas fa-chart-line text-emerald-600"></i>
                </div>
                <h2 class="font-semibold text-slate-900">Total Served</h2>
              </div>
              <div v-if="totalServed.length" class="space-y-3 max-h-64 overflow-y-auto scrollbar-thin">
                <div
                  v-for="served in totalServed"
                  :key="served.name"
                  class="flex items-center justify-between py-2 border-b border-slate-100 last:border-0"
                >
                  <span class="font-medium text-slate-900 text-sm">{{ served.name }}</span>
                  <span class="text-lg font-bold text-emerald-600">{{ served.total }}</span>
                </div>
              </div>
              <p v-else class="text-slate-500 text-sm">No data available</p>
            </div>

            <!-- Supply Requests -->
            <div class="card p-6">
              <div class="flex items-center gap-3 mb-4">
                <div class="w-10 h-10 rounded-xl bg-amber-100 flex items-center justify-center">
                  <i class="fas fa-truck text-amber-600"></i>
                </div>
                <h2 class="font-semibold text-slate-900">Supply Requests</h2>
              </div>
              <div v-if="supplyRequests.length" class="space-y-3 max-h-64 overflow-y-auto scrollbar-thin">
                <div
                  v-for="request in supplyRequests"
                  :key="request.id"
                  class="p-3 bg-slate-50 rounded-lg"
                >
                  <div class="flex items-center justify-between mb-2">
                    <span class="badge" :class="getRequestStatusBadgeClass(request.status)">
                      {{ request.status }}
                    </span>
                    <span class="text-xs text-slate-500">{{ formatDate(request.createdAt) }}</span>
                  </div>
                  <div class="mb-2 space-y-1">
                    <p
                      v-for="(item, index) in getRequestItemsSummary(request)"
                      :key="`${request.id}-item-${index}`"
                      class="text-sm text-slate-700"
                    >
                      {{ item }}
                    </p>
                    <p
                      v-if="getRequestRejectionReason(request)"
                      class="text-xs text-red-600"
                    >
                      <span class="font-semibold">Reason:</span>
                      {{ getRequestRejectionReason(request) }}
                    </p>
                  </div>
                  <div class="flex gap-2">
                    <button
                      v-if="request.status === 'REQUESTED'"
                      @click="cancelRequest(request.id)"
                      class="flex-1 py-1.5 bg-red-100 text-red-700 rounded text-xs font-medium
                             hover:bg-red-200 transition-colors"
                    >
                      Cancel
                    </button>
                    <button
                      v-if="request.status === 'DELIVERED'"
                      @click="addToStock(request)"
                      class="flex-1 py-1.5 bg-emerald-100 text-emerald-700 rounded text-xs font-medium
                             hover:bg-emerald-200 transition-colors"
                    >
                      Add to Stock
                    </button>
                  </div>
                </div>
              </div>
              <p v-else class="text-slate-500 text-sm">No supply requests</p>
            </div>
          </div>
        </div>

        <!-- Modals -->
        <SellStockModal
          :isOpen="showSellModal"
          :item="selectedItem"
          :barId="String(route.params.barId)"
          @close="showSellModal = false"
          @sale-confirmed="handleSell"
        />
        <SupplyRequestModal
          :isOpen="showRequestModal"
          :item="selectedItem"
          :barId="String(route.params.barId)"
          @close="showRequestModal = false"
          @supply-requested="handleSupplyRequest"
        />
        <AddStockModal
          :is-open="showAddStockModal"
          @close="showAddStockModal = false"
          @stock-added="handleAddStock"
        />
        <AutoSupplyRequestModal
          v-if="showAutoSupplyModal && autoSupplyItem"
          :item="autoSupplyItem"
          @confirm="handleAutoSupply"
          @cancel="showAutoSupplyModal = false"
        />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useQueryClient } from '@tanstack/vue-query';
import {
  useBarDetailsQuery,
  useReduceStock,
  useAddStock,
  useCreateSupplyRequest,
  useUpdateSupplyRequest,
  useCancelSupplyRequest,
} from '@/composables/queries/useBarQueries';
import { queryKeys } from '@/composables/queries/queryKeys';
import { useWebSocketEvents } from '@/composables/useWebSocketEvents';
import SellStockModal from '@/components/bars/SellStockModal.vue';
import SupplyRequestModal from '@/components/bars/SupplyRequestModal.vue';
import AddStockModal from '@/components/bars/AddStockModal.vue';
import AutoSupplyRequestModal from '@/components/bars/AutoSupplyRequestModal.vue';
import { formatRelativeTime as formatDate } from '@/composables/useDateFormat';
import BaseLoadingSpinner from '@/components/common/BaseLoadingSpinner.vue';

const route = useRoute();
const queryClient = useQueryClient();
const { eventsByType } = useWebSocketEvents();

const barId = computed(() => String(route.params.barId ?? ''));
const { data: barDetails, isLoading, error, refetch } = useBarDetailsQuery(barId);

// Convenient aliases for template
const bar = computed(() => barDetails.value?.bar ?? null);
const stock = computed(() => barDetails.value?.stock ?? []);
const usageLogs = computed(() => barDetails.value?.usageLogs ?? []);
const totalServed = computed(() => barDetails.value?.totalServed ?? []);
const supplyRequests = computed(() => barDetails.value?.supplyRequests ?? []);

// Mutations
const reduceStockMutation = useReduceStock();
const addStockMutation = useAddStock();
const createSupplyRequestMutation = useCreateSupplyRequest();
const updateSupplyRequestMutation = useUpdateSupplyRequest();
const cancelSupplyRequestMutation = useCancelSupplyRequest();

const showSellModal = ref(false);
const showRequestModal = ref(false);
const showAddStockModal = ref(false);
const showAutoSupplyModal = ref(false);
const selectedItem = ref(null as any);
const autoSupplyItem = ref(null as any);

// Refresh bar data when warehouse updates supply status for this bar
watch(
  () => eventsByType['SUPPLY_REQUEST_UPDATED'],
  (newEvent) => {
    if (newEvent && newEvent.resourceId === barId.value) {
      queryClient.invalidateQueries({ queryKey: queryKeys.bars.details(barId.value) });
    }
  }
);

// Refresh bar data when stock is updated for this bar
const sortedStock = computed(() => {
  const items = stock.value.filter(Boolean);
  const toTime = (value: string | undefined) => {
    const t = new Date(value ?? 0).getTime();
    return Number.isFinite(t) ? t : 0;
  };
  return [...items].sort((a, b) => toTime(b?.updatedAt) - toTime(a?.updatedAt));
});

// Helper to format the items in a supply request for display in the UI
const getRequestItemsSummary = (request: any) => {
  const items = Array.isArray(request?.items) ? request.items : [];
  if (items.length === 0) return ['No items'];
  return items.map((item: any) => {
    const name = String(item?.productName ?? 'Unknown');
    const quantity = Number(item?.quantity ?? 0);
    return `${name} × ${quantity}`;
  });
};

// If a request was rejected, we can show the reason in the UI to provide more context to the user about why their supply request was rejected
const getRequestRejectionReason = (request: any) => {
  if (request?.status !== 'REJECTED') return null;
  const reason = String(request?.rejectionReason ?? '').trim();
  return reason || null;
};

// When selling an item, we can directly reduce the stock without needing to wait for a separate confirmation step since the user has already confirmed the sale in the SellStockModal
const getProductBgClass = (name: string) => {
  const lowerName = String(name ?? '').toLowerCase();
  if (!lowerName) return 'bg-slate-500';
  if (lowerName === 'fanta') return 'bg-amber-500';
  if (lowerName === 'coca zero') return 'bg-slate-900';
  if (lowerName === 'red bull') return 'bg-red-500';
  if (lowerName === 'beer') return 'bg-yellow-500';
  if (lowerName === 'wine') return 'bg-purple-500';
  return 'bg-bar-500';
};

const getQuantityBadgeClass = (quantity: number) => {
  if (quantity === 0) return 'badge-danger';
  if (quantity < 10) return 'badge-warning';
  return 'badge-success';
};

/**
 * Determine a sensible 'updatedAt' for a stock item.
 * Prefer the stock item's explicit timestamp, otherwise use the most
 * recent usage log for that product, then fall back to createdAt or null.
 */
const getItemUpdatedAt = (item: any) => {
  if (!item) return null;
  if (item.updatedAt) return item.updatedAt;
  if (item.createdAt) return item.createdAt;

  // Try to infer from the latest usage log for this product
  const logs = usageLogs.value.filter((l: any) => l.productName === item.productName);
  if (logs.length) {
    const sorted = [...logs].sort((a: any, b: any) => {
      const ta = new Date(a.timestamp ?? 0).getTime();
      const tb = new Date(b.timestamp ?? 0).getTime();
      return tb - ta;
    });
    return sorted[0]?.timestamp ?? null;
  }

  // No timestamp available
  return null;
};

// Map supply request status to badge classes
const getRequestStatusBadgeClass = (status: string) => {
  const classes: Record<string, string> = {
    'REQUESTED': 'badge-info',
    'IN_PROGRESS': 'badge-warning',
    'DELIVERED': 'badge-success',
    'REJECTED': 'badge-danger',
    'CANCELLED': 'badge-danger',
    'COMPLETED': 'badge-neutral',
  };
  return classes[status] || 'badge-neutral';
};

// Helper to check if there's a pending supply request for a given product
const hasPendingRequest = (productName: string) => {
  return supplyRequests.value.some((req) => {
    const items = Array.isArray(req?.items) ? req.items : [];
    return items.some(
      (item) =>
        item?.productName === productName &&
        (req?.status === 'REQUESTED' || req?.status === 'IN_PROGRESS' || req?.status === 'DELIVERED')
    );
  });
};

const openSellModal = (item: any) => {
  selectedItem.value = item;
  showSellModal.value = true;
};

const openRequestModal = (item: any) => {
  selectedItem.value = item;
  showRequestModal.value = true;
};

// When selling an item, we can directly reduce the stock without needing to wait for a separate confirmation step since the user has already confirmed the sale in the SellStockModal
const handleSell = async ({ quantity }: { quantity: number }) => {
  const id = barId.value;
  const productName = selectedItem.value?.productName;
  try {
    if (!productName) throw new Error('Missing product name');
    const updatedItem = await reduceStockMutation.mutateAsync({ barId: id, payload: { productName, quantity } });
    // After sell completes, check if stock is low and trigger auto supply
    if (updatedItem && updatedItem.quantity <= 5 && !hasPendingRequest(productName)) {
      autoSupplyItem.value = updatedItem;
      showAutoSupplyModal.value = true;
    }
  } catch {
    // Error handled by Vue Query
  } finally {
    showSellModal.value = false;
  }
};

// When a supply request is made, we can directly create the request without needing to update the stock first since the backend will handle the necessary validations and trigger events to update the UI accordingly
const handleSupplyRequest = async ({ quantity }: { quantity: number }) => {
  const id = barId.value;
  const productName = selectedItem.value?.productName;
  try {
    if (!productName) throw new Error('Missing product name');
    await createSupplyRequestMutation.mutateAsync({ barId: id, payload: { items: [{ productName, quantity }] } });
  } catch {
    // Error handled by Vue Query
  } finally {
    showRequestModal.value = false;
  }
};

// When cancelling a request, we can directly call the cancel endpoint without needing to update the request status manually since the backend will handle that and trigger the necessary events to update the UI
const cancelRequest = async (requestId: string) => {
  if (confirm('Are you sure you want to cancel this supply request?')) {
    await cancelSupplyRequestMutation.mutateAsync({ barId: barId.value, requestId });
  }
};

// When marking a request as delivered, we can directly add to stock from the request details without waiting for a separate delivery confirmation step
const addToStock = async (request: any) => {
  const id = barId.value;
  const firstItem = Array.isArray(request?.items) ? request.items[0] : null;
  const productName = firstItem?.productName || firstItem?.productId;
  const quantity = firstItem?.quantity;
  if (!productName || !quantity) return;
  await addStockMutation.mutateAsync({ barId: id, payload: { productName, quantity } });
  await updateSupplyRequestMutation.mutateAsync({ barId: id, requestId: request.id, payload: { status: 'COMPLETED' } });
};
// After adding stock, we can also trigger the auto supply flow in case the user wants to automatically request more stock for this item
const handleAddStock = async ({ productName, quantity }: { productName: string; quantity: number }) => {
  await addStockMutation.mutateAsync({ barId: barId.value, payload: { productName, quantity } });
  showAddStockModal.value = false;
};

// When auto supply is confirmed, we create a supply request for the item with the specified quantity
const handleAutoSupply = async ({ productName, quantity }: { productName: string; quantity: number }) => {
  try {
    await createSupplyRequestMutation.mutateAsync({ barId: barId.value, payload: { items: [{ productName, quantity }] } });
  } catch {
    // Error handled by Vue Query
  } finally {
    showAutoSupplyModal.value = false;
    autoSupplyItem.value = null;
  }
};
</script>

<style scoped>
/* Minimal scoped styles - using Tailwind utilities */
</style>