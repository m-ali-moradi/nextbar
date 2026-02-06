<template>
  <div class="flex min-h-screen bg-slate-50">
    <Sidebar />
    
    <div class="flex-1 ml-72">
      <Navbar />
      
      <main class="p-6">
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
                {{ barStore.currentBar?.name || 'Bar Details' }}
              </h1>
              <p class="text-slate-500 mt-1">
                {{ barStore.currentBar?.location || 'Manage stock and sales' }}
              </p>
            </div>
          </div>
          <button @click="showAddStockModal = true" class="btn-primary">
            <i class="fas fa-plus"></i>
            <span>Add Drink</span>
          </button>
        </div>

        <!-- Loading State -->
        <div v-if="barStore.loading" class="flex items-center justify-center py-20">
          <div class="text-center">
            <div class="w-12 h-12 border-4 border-bar-200 border-t-bar-600 rounded-full animate-spin mx-auto mb-4"></div>
            <p class="text-slate-500">Loading bar details...</p>
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="barStore.error" class="card p-8 text-center">
          <div class="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4">
            <i class="fas fa-exclamation-triangle text-2xl text-red-600"></i>
          </div>
          <h3 class="text-lg font-semibold text-slate-900 mb-2">Error Loading Bar</h3>
          <p class="text-slate-500 mb-4">{{ barStore.error }}</p>
          <button @click="barStore.fetchBarDetails(route.params.barId)" class="btn-primary">
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
                  <p class="text-xs text-slate-500">{{ barStore.stock.length }} items</p>
                </div>
              </div>
            </div>

            <div v-if="barStore.stock.length" class="overflow-x-auto">
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
                          :class="getProductBgClass(item.name ?? item.productName)"
                        >
                          <i class="fas fa-wine-bottle text-white text-sm"></i>
                        </div>
                        <span class="font-medium text-slate-900">
                          {{ item.name ?? item.productName ?? 'Unknown' }}
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
                    <td class="text-slate-500">{{ formatDate(item.updatedAt) }}</td>
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
                          v-if="item.quantity === 0 && !hasPendingRequest(item.productId)"
                          @click="openRequestModal(item)"
                          class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-amber-500 text-white 
                                 rounded-lg text-sm font-medium hover:bg-amber-600 transition-colors"
                        >
                          <i class="fas fa-truck"></i>
                          Request
                        </button>
                        <span
                          v-if="hasPendingRequest(item.productId)"
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
              <div v-if="barStore.usageLogs.length" class="space-y-3 max-h-64 overflow-y-auto scrollbar-thin">
                <div
                  v-for="log in barStore.usageLogs.slice(0, 10)"
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
              <div v-if="barStore.totalServed.length" class="space-y-3 max-h-64 overflow-y-auto scrollbar-thin">
                <div
                  v-for="served in barStore.totalServed"
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
              <div v-if="barStore.supplyRequests.length" class="space-y-3 max-h-64 overflow-y-auto scrollbar-thin">
                <div
                  v-for="request in barStore.supplyRequests"
                  :key="request.id"
                  class="p-3 bg-slate-50 rounded-lg"
                >
                  <div class="flex items-center justify-between mb-2">
                    <span class="badge" :class="getRequestStatusBadgeClass(request.status)">
                      {{ request.status }}
                    </span>
                    <span class="text-xs text-slate-500">{{ formatDate(request.createdAt) }}</span>
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
          :barId="route.params.barId"
          @close="showSellModal = false"
          @sale-confirmed="handleSell"
        />
        <SupplyRequestModal
          :isOpen="showRequestModal"
          :item="selectedItem"
          :barId="route.params.barId"
          @close="showRequestModal = false"
          @supply-requested="handleSupplyRequest"
        />
        <AddStockModal
          :is-open="showAddStockModal"
          @close="showAddStockModal = false"
          @stock-added="handleAddStock"
        />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useBarStore } from '../../stores/barStore.ts';
import Sidebar from '../../components/common/Sidebar.vue';
import Navbar from '../../components/common/Navbar.vue';
import SellStockModal from '../../components/bars/SellStockModal.vue';
import SupplyRequestModal from '../../components/bars/SupplyRequestModal.vue'; 
import AddStockModal from '../../components/bars/AddStockModal.vue'; 

const barStore = useBarStore();
const route = useRoute();
const showSellModal = ref(false);
const showRequestModal = ref(false);
const showAddStockModal = ref(false);
const selectedItem = ref(null);

onMounted(() => {
  const barId = route.params.barId;
  barStore.fetchBarDetails(barId);
});

const sortedStock = computed(() => {
  const stock = Array.isArray(barStore.stock) ? barStore.stock.filter(Boolean) : [];
  const toTime = (value) => {
    const t = new Date(value ?? 0).getTime();
    return Number.isFinite(t) ? t : 0;
  };
  return [...stock].sort((a, b) => toTime(b?.updatedAt) - toTime(a?.updatedAt));
});

const getProductBgClass = (name) => {
  const lowerName = String(name ?? '').toLowerCase();
  if (!lowerName) return 'bg-slate-500';
  if (lowerName === 'fanta') return 'bg-amber-500';
  if (lowerName === 'coca zero') return 'bg-slate-900';
  if (lowerName === 'red bull') return 'bg-red-500';
  if (lowerName === 'beer') return 'bg-yellow-500';
  if (lowerName === 'wine') return 'bg-purple-500';
  return 'bg-bar-500';
};

const getQuantityBadgeClass = (quantity) => {
  if (quantity === 0) return 'badge-danger';
  if (quantity < 10) return 'badge-warning';
  return 'badge-success';
};

const getRequestStatusBadgeClass = (status) => {
  const classes = {
    'REQUESTED': 'badge-info',
    'IN_PROGRESS': 'badge-warning',
    'DELIVERED': 'badge-success',
    'CANCELLED': 'badge-danger',
    'COMPLETED': 'badge-neutral',
  };
  return classes[status] || 'badge-neutral';
};

const formatDate = (date) => {
  if (!date) return '—';
  const updated = new Date(date);
  const updatedTime = updated.getTime();
  if (!Number.isFinite(updatedTime)) return '—';

  const nowTime = Date.now();
  const diffMs = Math.max(0, nowTime - updatedTime);
  const diffSeconds = Math.floor(diffMs / 1000);
  if (diffSeconds < 60) return 'Just now';

  const diffMinutes = Math.floor(diffSeconds / 60);
  if (diffMinutes < 60) return `${diffMinutes} min ago`;

  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours}h ago`;

  const diffDays = Math.floor(diffHours / 24);
  return `${diffDays}d ago`;
};

const hasPendingRequest = (productId) => {
  const requests = Array.isArray(barStore.supplyRequests) ? barStore.supplyRequests : [];
  return requests.some((req) => {
    const items = Array.isArray(req?.items) ? req.items : [];
    return items.some(
      (item) =>
        item?.productId === productId &&
        (req?.status === 'REQUESTED' || req?.status === 'IN_PROGRESS' || req?.status === 'DELIVERED')
    );
  });
};

const openSellModal = (item) => {
  selectedItem.value = item;
  showSellModal.value = true;
};

const openRequestModal = (item) => {
  selectedItem.value = item;
  showRequestModal.value = true;
};

const handleSell = async ({ quantity }) => {
  const barId = route.params.barId;
  const productId = selectedItem.value?.productId;
  try {
    if (!productId) throw new Error('Missing product id');
    await barStore.reduceStock(barId, productId, quantity);
  } catch (error) {
    barStore.error = (error instanceof Error ? error.message : String(error));
  } finally {
    showSellModal.value = false;
  }
};

const handleSupplyRequest = async ({ quantity }) => {
  const barId = route.params.barId;
  const productId = selectedItem.value?.productId;
  try {
    if (!productId) throw new Error('Missing product id');
    await barStore.createSupplyRequest(barId, [{ productId, quantity }]);
  } catch (error) {
    barStore.error = (error instanceof Error ? error.message : String(error));
  } finally {
    showRequestModal.value = false;
  }
};

const cancelRequest = async (requestId) => {
  if (confirm('Are you sure you want to cancel this supply request?')) {
    const barId = route.params.barId;
    await barStore.cancelSupplyRequest(barId, requestId);
  }
};

const addToStock = async (request) => {
  const barId = route.params.barId;
  const firstItem = Array.isArray(request?.items) ? request.items[0] : null;
  const productId = firstItem?.productId;
  const quantity = firstItem?.quantity;
  if (!productId || !quantity) {
    barStore.error = 'Supply request has no items to add to stock.';
    return;
  }
  await barStore.addStock(barId, productId, quantity);
  await barStore.updateSupplyRequest(barId, request.id, 'COMPLETED');
};

const handleAddStock = async ({ productId, quantity }) => {
  const barId = route.params.barId;
  await barStore.addStock(barId, productId, quantity);
  showAddStockModal.value = false;
};
</script>

<style scoped>
/* Minimal scoped styles - using Tailwind utilities */
</style>