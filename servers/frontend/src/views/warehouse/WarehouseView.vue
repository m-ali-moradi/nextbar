<template>
  <div class="flex min-h-screen bg-slate-50">
    <Sidebar />
    
    <div class="flex-1 ml-72">
      <Navbar />

      <main class="p-6">
        <!-- Page Header -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-bold text-slate-900">Warehouse Management</h1>
            <p class="text-slate-500 mt-1">Manage inventory, supply requests, and collections</p>
          </div>
          <!-- WebSocket Connection Status -->
          <div class="flex items-center gap-2 px-3 py-1.5 rounded-lg" 
               :class="connected ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'">
            <span class="w-2 h-2 rounded-full animate-pulse" 
                  :class="connected ? 'bg-emerald-500' : 'bg-rose-500'"></span>
            <span class="text-sm font-medium">
              {{ connected ? '🔌 Live Updates' : '⚠️ Disconnected' }}
            </span>
          </div>
        </div>

        <!-- Stats Overview -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Total Items</p>
                <p class="text-2xl font-bold text-slate-900 mt-1">{{ totalSKUs }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-warehouse-100 flex items-center justify-center">
                <i class="fas fa-boxes text-xl text-warehouse-600"></i>
              </div>
            </div>
          </div>
          
          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Pending Requests</p>
                <p class="text-2xl font-bold text-amber-600 mt-1">{{ pendingRequests.length }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-amber-100 flex items-center justify-center">
                <i class="fas fa-clock text-xl text-amber-600"></i>
              </div>
            </div>
          </div>

          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Completed</p>
                <p class="text-2xl font-bold text-emerald-600 mt-1">{{ completedRequests.length }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-emerald-100 flex items-center justify-center">
                <i class="fas fa-check-circle text-xl text-emerald-600"></i>
              </div>
            </div>
          </div>

          <div 
            v-if="lowStockItems.length > 0" 
            class="card p-5 card-interactive bg-amber-50 border-amber-200"
          >
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-amber-700 font-medium">Low Stock</p>
                <p class="text-2xl font-bold text-amber-700 mt-1">{{ lowStockItems.length }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-amber-100 flex items-center justify-center">
                <i class="fas fa-exclamation-triangle text-xl text-amber-600"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- Tab Navigation -->
        <div class="flex gap-1 p-1.5 bg-white rounded-xl border border-slate-200 shadow-sm mb-6 inline-flex">
          <button
            @click="activeTab = 'stock'"
            class="px-5 py-2.5 rounded-lg text-sm font-medium transition-all duration-200"
            :class="activeTab === 'stock' 
              ? 'bg-warehouse-600 text-white shadow-sm' 
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
          >
            <i class="fas fa-boxes mr-2"></i>
            Stock Inventory
          </button>
          <button
            @click="activeTab = 'supply'"
            class="px-5 py-2.5 rounded-lg text-sm font-medium transition-all duration-200"
            :class="activeTab === 'supply' 
              ? 'bg-warehouse-600 text-white shadow-sm' 
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
          >
            <i class="fas fa-truck mr-2"></i>
            Supply Requests
          </button>
          <button
            @click="activeTab = 'empties'"
            class="px-5 py-2.5 rounded-lg text-sm font-medium transition-all duration-200"
            :class="activeTab === 'empties' 
              ? 'bg-warehouse-600 text-white shadow-sm' 
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
          >
            <i class="fas fa-recycle mr-2"></i>
            Empties Collected
          </button>
        </div>

        <!-- Stock Inventory Tab -->
        <Transition name="fade" mode="out-in">
          <div v-if="activeTab === 'stock'" key="stock">
            <div class="card overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                <h2 class="font-semibold text-slate-900">Stock Inventory</h2>
                <button @click="openReplenishModal" class="btn-primary">
                  <i class="fas fa-plus"></i>
                  <span>Replenish Stock</span>
                </button>
              </div>

              <div v-if="loading" class="p-12 text-center">
                <div class="w-10 h-10 border-4 border-warehouse-200 border-t-warehouse-600 rounded-full animate-spin mx-auto mb-3"></div>
                <p class="text-slate-500">Loading stock...</p>
              </div>

              <div v-else-if="stock.length" class="overflow-x-auto">
                <table class="table-modern">
                  <thead>
                    <tr>
                      <th>Product ID</th>
                      <th>Product Name</th>
                      <th>Quantity</th>
                      <th>Unit Price</th>
                      <th>Total Value</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in stock" :key="item.id">
                      <td class="font-mono text-slate-500">#{{ item.id }}</td>
                      <td class="font-medium text-slate-900">{{ item.productName }}</td>
                      <td>{{ item.quantity }}</td>
                      <td>€{{ item.unitPrice?.toFixed(2) || '0.00' }}</td>
                      <td class="font-medium">€{{ ((item.quantity * (item.unitPrice || 0))).toFixed(2) }}</td>
                      <td>
                        <span 
                          class="badge"
                          :class="item.quantity < 10 ? 'badge-warning' : 'badge-success'"
                        >
                          {{ item.quantity < 10 ? 'Low Stock' : 'In Stock' }}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div v-else class="p-12 text-center">
                <div class="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mx-auto mb-4">
                  <i class="fas fa-box-open text-2xl text-slate-400"></i>
                </div>
                <p class="text-slate-500">No stock items found</p>
              </div>
            </div>
          </div>
        </Transition>

        <!-- Supply Requests Tab -->
        <Transition name="fade" mode="out-in">
          <div v-if="activeTab === 'supply'" key="supply">
            <div class="card overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                <div>
                  <h2 class="font-semibold text-slate-900">Supply Requests</h2>
                  <p class="text-sm text-slate-500">Track requests across bars and update statuses</p>
                </div>
                <div class="flex items-center gap-3">
                  <div class="flex gap-1 p-1 bg-slate-100 rounded-lg">
                    <button
                      @click="switchViewMode('all')"
                      class="px-4 py-1.5 rounded-md text-sm font-medium transition-all"
                      :class="viewMode === 'all' ? 'bg-white shadow-sm text-slate-900' : 'text-slate-600'"
                    >
                      All Requests
                    </button>
                    <button
                      @click="switchViewMode('bar')"
                      class="px-4 py-1.5 rounded-md text-sm font-medium transition-all"
                      :class="viewMode === 'bar' ? 'bg-white shadow-sm text-slate-900' : 'text-slate-600'"
                    >
                      By Bar
                    </button>
                  </div>
                  <button @click="refreshAllRequests" class="btn-secondary text-sm py-1.5">
                    <i class="fas fa-sync-alt"></i>
                    Refresh
                  </button>
                </div>
              </div>

              <!-- All Bars View -->
              <div v-if="viewMode === 'all'" class="p-6">
                <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-6">
                  <div class="flex flex-wrap gap-2">
                    <span class="badge badge-info">Requested: {{ pendingRequests.length }}</span>
                    <span class="badge badge-warning">In Progress: {{ inProgressRequests.length }}</span>
                    <span class="badge badge-success">Delivered: {{ completedRequests.length }}</span>
                    <span class="badge badge-danger">Rejected: {{ rejectedRequests.length }}</span>
                  </div>
                  <div class="flex flex-col sm:flex-row gap-3">
                    <select v-model="statusFilter" class="input w-full sm:w-44">
                      <option value="ALL">All Statuses</option>
                      <option value="REQUESTED">Requested</option>
                      <option value="IN_PROGRESS">In Progress</option>
                      <option value="DELIVERED">Delivered</option>
                      <option value="REJECTED">Rejected</option>
                    </select>
                    <div class="relative">
                      <i class="fas fa-search absolute left-3 top-3 text-slate-400 text-sm"></i>
                      <input
                        v-model="supplySearch"
                        type="text"
                        placeholder="Search bar, item, request id"
                        class="input pl-9 w-full sm:w-64"
                      />
                    </div>
                  </div>
                </div>

                <div v-if="loading" class="p-12 text-center">
                  <div class="w-10 h-10 border-4 border-warehouse-200 border-t-warehouse-600 rounded-full animate-spin mx-auto mb-3"></div>
                  <p class="text-slate-500">Loading supply requests...</p>
                </div>

                <div v-else-if="filteredAllRequests.length" class="overflow-x-auto -mx-6">
                  <table class="table-modern">
                    <thead>
                      <tr>
                        <th>Request ID</th>
                        <th>Bar</th>
                        <th>Items</th>
                        <th>Total Qty</th>
                        <th>Status</th>
                        <th>Requested</th>
                        <th class="text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="request in filteredAllRequests" :key="request.id">
                        <td class="font-mono text-slate-500">#{{ request.id }}</td>
                        <td>
                          <p class="font-medium text-slate-900">{{ getBarLabel(request) }}</p>
                          <p class="text-xs text-slate-500">{{ getBarLocation(request) }}</p>
                        </td>
                        <td :title="getItemsDetail(request)">
                          <p class="text-sm text-slate-900">{{ getItemsSummary(request) }}</p>
                        </td>
                        <td class="font-medium text-slate-900">{{ getTotalQuantity(request) }}</td>
                        <td>
                          <span class="badge" :class="getStatusBadgeClass(request.status)">
                            {{ request.status }}
                          </span>
                          <p v-if="request.status === 'REJECTED' && request.rejectionReason" class="text-xs text-rose-600 mt-1">
                            {{ request.rejectionReason }}
                          </p>
                        </td>
                        <td>{{ formatDate(request.createdAt || request.requestedAt || '') }}</td>
                        <td class="text-right">
                          <div class="flex items-center justify-end gap-2">
                            <button
                              v-if="request.status === 'REQUESTED'"
                              @click="processRequest(request)"
                              class="btn-primary text-sm py-1.5"
                            >
                              Start
                            </button>
                            <button
                              v-if="request.status === 'REQUESTED'"
                              @click="openRejectModal(request)"
                              class="btn-danger text-sm py-1.5"
                            >
                              Reject
                            </button>
                            <button
                              v-if="request.status === 'IN_PROGRESS'"
                              @click="deliverRequest(request)"
                              class="inline-flex items-center gap-2 px-3 py-1.5 bg-emerald-600 text-white 
                                     rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
                            >
                              Deliver
                            </button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div v-else class="text-center py-12">
                  <div class="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mx-auto mb-4">
                    <i class="fas fa-inbox text-2xl text-slate-400"></i>
                  </div>
                  <p class="text-slate-500">No supply requests found</p>
                </div>
              </div>

              <!-- By Bar View -->
              <div v-else class="p-6">
                <div class="mb-6">
                  <label for="bar-select" class="label">Select Bar</label>
                  <select 
                    id="bar-select" 
                    v-model="selectedBarId" 
                    @change="onBarSelected"
                    class="input max-w-sm"
                  >
                    <option value="">-- Choose a bar --</option>
                    <option v-for="bar in bars" :key="bar.id" :value="bar.id">
                      {{ bar.name }} ({{ bar.location }})
                    </option>
                  </select>
                </div>

                <div v-if="selectedBarRequests.length > 0" class="overflow-x-auto -mx-6">
                  <table class="table-modern">
                    <thead>
                      <tr>
                        <th>Request ID</th>
                        <th>Items</th>
                        <th>Total Qty</th>
                        <th>Status</th>
                        <th>Requested</th>
                        <th class="text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="request in selectedBarRequests" :key="request.id">
                        <td class="font-mono text-slate-500">#{{ request.id }}</td>
                        <td class="font-medium text-slate-900" :title="getItemsDetail(request)">
                          {{ getItemsSummary(request) }}
                        </td>
                        <td>{{ getTotalQuantity(request) }}</td>
                        <td>
                          <span class="badge" :class="getStatusBadgeClass(request.status)">
                            {{ request.status }}
                          </span>
                          <p v-if="request.status === 'REJECTED' && request.rejectionReason" class="text-xs text-rose-600 mt-1">
                            {{ request.rejectionReason }}
                          </p>
                        </td>
                        <td>{{ formatDate(request.createdAt || request.requestedAt || '') }}</td>
                        <td class="text-right">
                          <div class="flex items-center justify-end gap-2">
                            <button
                              v-if="request.status === 'REQUESTED'"
                              @click="processRequest(request)"
                              class="btn-primary text-sm py-1.5"
                            >
                              Start
                            </button>
                            <button
                              v-if="request.status === 'REQUESTED'"
                              @click="openRejectModal(request)"
                              class="btn-danger text-sm py-1.5"
                            >
                              Reject
                            </button>
                            <button
                              v-if="request.status === 'IN_PROGRESS'"
                              @click="deliverRequest(request)"
                              class="inline-flex items-center gap-2 px-3 py-1.5 bg-emerald-600 text-white 
                                     rounded-lg text-sm font-medium hover:bg-emerald-700 transition-colors"
                            >
                              Deliver
                            </button>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <div v-else-if="selectedBarId" class="text-center py-12">
                  <div class="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mx-auto mb-4">
                    <i class="fas fa-inbox text-2xl text-slate-400"></i>
                  </div>
                  <p class="text-slate-500">No supply requests for this bar</p>
                </div>
              </div>
            </div>
          </div>
        </Transition>

        <!-- Empties Collected Tab -->
        <Transition name="fade" mode="out-in">
          <div v-if="activeTab === 'empties'" key="empties">
            <div class="card overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                <h2 class="font-semibold text-slate-900">Empties Collected from Drop Points</h2>
                <button @click="fetchEmptiesData" class="btn-secondary">
                  <i class="fas fa-sync-alt"></i>
                  <span>Refresh</span>
                </button>
              </div>

              <div v-if="loading" class="p-12 text-center">
                <div class="w-10 h-10 border-4 border-droppoint-200 border-t-droppoint-600 rounded-full animate-spin mx-auto mb-3"></div>
                <p class="text-slate-500">Loading empties data...</p>
              </div>

              <div v-else-if="emptiesCollected.length > 0" class="overflow-x-auto">
                <table class="table-modern">
                  <thead>
                    <tr>
                      <th>Drop Point ID</th>
                      <th>Location</th>
                      <th>Empties Collected</th>
                      <th>Collection Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in emptiesCollected" :key="item.id">
                      <td class="font-mono text-slate-500">#{{ item.dropPointId }}</td>
                      <td>{{ item.location }}</td>
                      <td class="font-medium">{{ item.emptiesCount }}</td>
                      <td>{{ formatDate(item.collectionDate) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div v-else class="p-12 text-center">
                <div class="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mx-auto mb-4">
                  <i class="fas fa-recycle text-2xl text-slate-400"></i>
                </div>
                <p class="text-slate-500">No empties collected yet</p>
              </div>
            </div>
          </div>
        </Transition>

        <!-- Replenish Stock Modal -->
        <Transition name="modal">
          <div 
            v-if="showReplenishModal" 
            class="modal-overlay" 
            @click.self="closeReplenishModal"
          >
            <div class="modal-content">
              <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                <h2 class="text-lg font-semibold text-slate-900">Replenish Stock</h2>
                <button 
                  @click="closeReplenishModal" 
                  class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
                >
                  <i class="fas fa-times"></i>
                </button>
              </div>

              <form @submit.prevent="handleReplenish" class="p-6 space-y-5">
                <div>
                  <label for="productName" class="label">Product Name</label>
                  <input
                    id="productName"
                    v-model="replenishForm.productName"
                    type="text"
                    required
                    placeholder="e.g., Beer, Cola"
                    class="input"
                  />
                </div>

                <div>
                  <label for="quantity" class="label">Quantity</label>
                  <input
                    id="quantity"
                    v-model.number="replenishForm.quantity"
                    type="number"
                    required
                    min="1"
                    placeholder="Number of units"
                    class="input"
                  />
                </div>

                <div class="flex gap-3 pt-2">
                  <button type="submit" class="btn-primary flex-1">
                    <i class="fas fa-plus"></i>
                    Replenish
                  </button>
                  <button type="button" @click="closeReplenishModal" class="btn-secondary flex-1">
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        </Transition>

        <!-- Reject Supply Request Modal -->
        <Transition name="modal">
          <div
            v-if="showRejectModal"
            class="modal-overlay"
            @click.self="closeRejectModal"
          >
            <div class="modal-content">
              <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                <h2 class="text-lg font-semibold text-slate-900">Reject Supply Request</h2>
                <button
                  @click="closeRejectModal"
                  class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
                >
                  <i class="fas fa-times"></i>
                </button>
              </div>

              <form @submit.prevent="confirmReject" class="p-6 space-y-5">
                <div>
                  <label for="reject-reason" class="label">Rejection reason</label>
                  <textarea
                    id="reject-reason"
                    v-model="rejectForm.reason"
                    rows="4"
                    required
                    maxlength="255"
                    placeholder="e.g., Out of stock, supplier delay"
                    class="input"
                  ></textarea>
                  <p class="text-xs text-slate-500 mt-2">
                    This reason will be shared with the bar for transparency.
                  </p>
                </div>

                <div class="flex flex-wrap gap-2">
                  <button type="button" class="btn-secondary text-sm" @click="setQuickReason('Out of stock')">
                    Out of stock
                  </button>
                  <button type="button" class="btn-secondary text-sm" @click="setQuickReason('Supplier delay')">
                    Supplier delay
                  </button>
                  <button type="button" class="btn-secondary text-sm" @click="setQuickReason('Quality issue')">
                    Quality issue
                  </button>
                </div>

                <div class="flex gap-3 pt-2">
                  <button type="submit" class="btn-danger flex-1" :disabled="!rejectForm.reason.trim()">
                    <i class="fas fa-ban"></i>
                    Reject request
                  </button>
                  <button type="button" @click="closeRejectModal" class="btn-secondary flex-1">
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        </Transition>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useWarehouseStore } from '@/stores/warehouseStore';
import { useWebSocketEvents } from '@/composables/useWebSocketEvents';
import { SupplyRequest } from '@/api/warehouseApi';
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';
import Sidebar from '@/components/common/Sidebar.vue';
import Navbar from '@/components/common/Navbar.vue';

const warehouseStore = useWarehouseStore();

// ============================================================
// WebSocket Real-time Updates
// ============================================================
const { connected, eventsByType, onEvent } = useWebSocketEvents();

const refreshOnEvent = async (eventType, newEvent) => {
  console.log(`[WarehouseView] 📡 ${eventType} event detected!`, newEvent);
  toast.info('Supply request changed - refreshing...');
  await refreshAllRequests({ silent: true });
  toast.success('Data refreshed!');
};

// Watch for SUPPLY_REQUEST_CREATED events and auto-refresh
watch(
  () => eventsByType['SUPPLY_REQUEST_CREATED'],
  async (newEvent) => {
    if (newEvent) {
      await refreshOnEvent('SUPPLY_REQUEST_CREATED', newEvent);
    }
  }
);

// Watch for SUPPLY_REQUEST_UPDATED events and auto-refresh
watch(
  () => eventsByType['SUPPLY_REQUEST_UPDATED'],
  async (newEvent) => {
    if (newEvent) {
      await refreshOnEvent('SUPPLY_REQUEST_UPDATED', newEvent);
    }
  }
);

// State
const activeTab = ref<'stock' | 'supply' | 'empties'>('stock');
const viewMode = ref<'all' | 'bar'>('all');
const selectedBarId = ref<string>('');
const statusFilter = ref<'ALL' | 'REQUESTED' | 'IN_PROGRESS' | 'DELIVERED' | 'REJECTED'>('ALL');
const supplySearch = ref('');
const showReplenishModal = ref(false);
const showRejectModal = ref(false);
const replenishForm = ref({
  productName: '',
  quantity: 0,
});
const rejectForm = ref<{ request: SupplyRequest | null; reason: string }>({
  request: null,
  reason: '',
});

// Computed
const stock = computed(() => warehouseStore.stock);
const bars = computed(() => warehouseStore.bars);
const allSupplyRequests = computed(() => warehouseStore.allSupplyRequests);
const selectedBarRequests = computed(() => warehouseStore.selectedBarRequests);
const emptiesCollected = computed(() => warehouseStore.emptiesCollected);
const loading = computed(() => warehouseStore.loading);
const totalSKUs = computed(() => warehouseStore.totalSKUs);
const lowStockItems = computed(() => warehouseStore.lowStockItems);
const pendingRequests = computed(() => warehouseStore.pendingRequests);
const inProgressRequests = computed(() => warehouseStore.inProgressRequests);
const completedRequests = computed(() => warehouseStore.completedRequests);
const rejectedRequests = computed(() => warehouseStore.rejectedRequests);

const filteredAllRequests = computed(() => {
  const status = statusFilter.value;
  const query = supplySearch.value.trim().toLowerCase();

  return allSupplyRequests.value.filter(request => {
    if (status !== 'ALL' && request.status !== status) return false;

    if (!query) return true;

    const barLabel = getBarLabel(request).toLowerCase();
    const barLocation = getBarLocation(request).toLowerCase();
    const itemsText = request.items
      .map(item => `${item.productName} ${item.quantity}`)
      .join(' ')
      .toLowerCase();
    const idText = String(request.id).toLowerCase();

    return (
      barLabel.includes(query)
      || barLocation.includes(query)
      || itemsText.includes(query)
      || idText.includes(query)
    );
  });
});

// Methods
const getStatusBadgeClass = (status: string) => {
  const classes: Record<string, string> = {
    'REQUESTED': 'badge-info',
    'IN_PROGRESS': 'badge-warning',
    'DELIVERED': 'badge-success',
    'REJECTED': 'badge-danger',
  };
  return classes[status] || 'badge-neutral';
};

const switchViewMode = async (mode: 'all' | 'bar') => {
  viewMode.value = mode;
  if (mode === 'all') {
    selectedBarId.value = '';
    warehouseStore.setSelectedBar(null);
    await refreshAllRequests();
  }
};

const onBarSelected = async () => {
  if (!selectedBarId.value) {
    warehouseStore.setSelectedBar(null);
    return;
  }

  const bar = bars.value.find(b => b.id === Number(selectedBarId.value));
  if (bar) {
    warehouseStore.setSelectedBar(bar);
    await warehouseStore.fetchBarSupplyRequests(bar.id);
  }
};

const processRequest = async (request: SupplyRequest) => {
  if (!confirm('Move this request to In Progress?')) return;

  const beverageType = request.items[0]?.productName || request.items[0]?.productId;
  const quantity = getTotalQuantity(request);

  if (!beverageType) {
    toast.error('Missing beverage information for this request');
    return;
  }

  try {
    await warehouseStore.fulfillSupplyRequest(request.barId, request.id, {
      beverageType,
      quantity,
      currentStatus: request.status,
    });
    toast.success('Request moved to In Progress');
    await refreshAllRequests();
    if (selectedBarId.value) {
      await warehouseStore.fetchBarSupplyRequests(request.barId);
    }
  } catch (err) {
    toast.error(toErrorMessage(err, 'Failed to process request'));
  }
};

const openRejectModal = (request: SupplyRequest) => {
  rejectForm.value = { request, reason: '' };
  showRejectModal.value = true;
};

const closeRejectModal = () => {
  showRejectModal.value = false;
  rejectForm.value = { request: null, reason: '' };
};

const setQuickReason = (reason: string) => {
  rejectForm.value.reason = reason;
};

const confirmReject = async () => {
  if (!rejectForm.value.request) return;

  try {
    await warehouseStore.rejectSupplyRequest(
      rejectForm.value.request.barId,
      rejectForm.value.request.id,
      rejectForm.value.reason.trim()
    );
    toast.warning('Request rejected');
    closeRejectModal();
    await refreshAllRequests();
    if (selectedBarId.value) {
      await warehouseStore.fetchBarSupplyRequests(rejectForm.value.request.barId);
    }
  } catch (err) {
    toast.error(toErrorMessage(err, 'Failed to reject request'));
  }
};

const deliverRequest = async (request: SupplyRequest) => {
  if (!confirm('Mark as delivered?')) return;

  const beverageType = request.items[0]?.productName || request.items[0]?.productId;
  const quantity = getTotalQuantity(request);

  if (!beverageType) {
    toast.error('Missing beverage information for this request');
    return;
  }

  try {
    await warehouseStore.fulfillSupplyRequest(request.barId, request.id, {
      beverageType,
      quantity,
      currentStatus: request.status,
    });
    toast.success('Request delivered successfully');
    await refreshAllRequests();
    if (selectedBarId.value) {
      await warehouseStore.fetchBarSupplyRequests(request.barId);
    }
  } catch (err) {
    toast.error(toErrorMessage(err, 'Failed to deliver request'));
  }
};

const toErrorMessage = (err: unknown, fallback: string) =>
  err instanceof Error ? err.message : fallback;

const refreshAllRequests = async (options: { silent?: boolean } = {}) => {
  try {
    await warehouseStore.fetchAllSupplyRequests(options);
  } catch (err) {
    toast.error(toErrorMessage(err, 'Failed to fetch supply requests'));
  }
};

const getTotalQuantity = (request: SupplyRequest) =>
  request.items.reduce((sum, item) => sum + (item.quantity || 0), 0);

const getItemsSummary = (request: SupplyRequest) => {
  if (!request.items.length) return 'No items';
  const firstTwo = request.items
    .slice(0, 2)
    .map(item => `${item.productName || item.productId} x${item.quantity}`);
  const remaining = request.items.length - firstTwo.length;
  return remaining > 0 ? `${firstTwo.join(', ')} +${remaining} more` : firstTwo.join(', ');
};

const getItemsDetail = (request: SupplyRequest) => {
  if (!request.items.length) return 'No items';
  return request.items
    .map(item => `${item.productName || item.productId} x${item.quantity}`)
    .join(', ');
};

const getBarLabel = (request: SupplyRequest) => {
  if (request.barName) return request.barName;
  const bar = bars.value.find(b => String(b.id) === String(request.barId));
  return bar ? bar.name : `Bar #${request.barId}`;
};

const getBarLocation = (request: SupplyRequest) => {
  const bar = bars.value.find(b => String(b.id) === String(request.barId));
  return bar ? bar.location : 'Unknown location';
};

const openReplenishModal = () => {
  showReplenishModal.value = true;
  replenishForm.value = { productName: '', quantity: 0 };
};

const closeReplenishModal = () => {
  showReplenishModal.value = false;
  replenishForm.value = { productName: '', quantity: 0 };
};

const handleReplenish = async () => {
  try {
    await warehouseStore.createStock({
      beverageType: replenishForm.value.productName,
      quantity: replenishForm.value.quantity,
    });
    toast.success(`Stock replenished: ${replenishForm.value.productName}`);
    closeReplenishModal();
  } catch (err) {
    toast.error(toErrorMessage(err, 'Failed to replenish stock'));
  }
};

const fetchEmptiesData = async () => {
  try {
    await warehouseStore.fetchEmptiesCollected();
    toast.success('Empties data refreshed');
  } catch (err) {
    toast.error(toErrorMessage(err, 'Failed to fetch empties'));
  }
};

const formatDate = (date: string) => {
  if (!date) return 'N/A';
  return new Date(date).toLocaleDateString();
};

// Lifecycle
onMounted(async () => {
  console.log('[WarehouseView] Mounted, WebSocket connected:', connected.value);
  await warehouseStore.fetchStock();
  await warehouseStore.fetchBars();
  await refreshAllRequests();
});

watch(activeTab, async (tab) => {
  if (tab === 'supply') {
    await refreshAllRequests();
  }
});
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: all 0.2s ease-out;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.95);
}
</style>
