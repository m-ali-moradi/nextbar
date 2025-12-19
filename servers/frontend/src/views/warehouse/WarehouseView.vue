<template>
  <div class="warehouse-container">
    <header class="page-header">
      <h1 class="text-3xl font-bold text-gray-800">Warehouse Management</h1>
    </header>

    <!-- Dashboard Summary -->
    <div class="summary-cards">
      <div class="card">
        <h3>Total Items</h3>
        <div class="value">{{ totalSKUs }}</div>
      </div>
      <div class="card">
        <h3>Pending Requests</h3>
        <div class="value">{{ pendingRequests.length }}</div>
      </div>
      <div class="card">
        <h3>Completed Requests</h3>
        <div class="value">{{ completedRequests.length }}</div>
      </div>
      <div class="card warning" v-if="lowStockItems.length > 0">
        <h3>Low Stock Items</h3>
        <div class="value">{{ lowStockItems.length }}</div>
      </div>
    </div>

    <!-- Tab Navigation -->
    <div class="tabs">
      <button
        :class="['tab', { active: activeTab === 'stock' }]"
        @click="activeTab = 'stock'"
      >
        Stock Inventory
      </button>
      <button
        :class="['tab', { active: activeTab === 'supply' }]"
        @click="activeTab = 'supply'"
      >
        Supply Requests
      </button>
      <button
        :class="['tab', { active: activeTab === 'empties' }]"
        @click="activeTab = 'empties'"
      >
        Empties Collected
      </button>
    </div>

    <!-- Stock Inventory Tab -->
    <div v-if="activeTab === 'stock'" class="tab-content">
      <div class="section-header">
        <h2>Stock Inventory</h2>
        <button @click="openReplenishModal" class="btn-primary">
          + Replenish Stock
        </button>
      </div>

      <div v-if="loading" class="loading-state">Loading stock...</div>

      <div v-else class="table-container">
        <table class="data-table">
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
              <td>{{ item.id }}</td>
              <td>{{ item.productName }}</td>
              <td>{{ item.quantity }}</td>
              <td>€{{ item.unitPrice?.toFixed(2) || '0.00' }}</td>
              <td>€{{ ((item.quantity * (item.unitPrice || 0))).toFixed(2) }}</td>
              <td>
                <span
                  class="status-badge"
                  :class="item.quantity < 10 ? 'status-low' : 'status-ok'"
                >
                  {{ item.quantity < 10 ? 'Low Stock' : 'In Stock' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="stock.length === 0" class="empty-state">
          No stock items found.
        </div>
      </div>
    </div>

    <!-- Supply Requests Tab -->
    <div v-if="activeTab === 'supply'" class="tab-content">
      <div class="section-header">
        <h2>Supply Requests</h2>
        <div class="mode-toggle">
          <button
            :class="{ active: viewMode === 'all' }"
            @click="switchViewMode('all')"
          >
            All Bars
          </button>
          <button
            :class="{ active: viewMode === 'bar' }"
            @click="switchViewMode('bar')"
          >
            By Bar
          </button>
        </div>
      </div>

      <!-- All Bars View -->
      <div v-if="viewMode === 'all'" class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th>Bar ID</th>
              <th>Bar Name</th>
              <th>Location</th>
              <th>Capacity</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="bar in bars" :key="bar.id">
              <td>{{ bar.id }}</td>
              <td>{{ bar.name }}</td>
              <td>{{ bar.location }}</td>
              <td>{{ bar.maxCapacity }}</td>
              <td>
                <button @click="viewBarRequests(bar)" class="btn-view">
                  View Requests
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- By Bar View -->
      <div v-else>
        <div class="controls">
          <label for="bar-select">Select Bar:</label>
          <select id="bar-select" v-model="selectedBarId" @change="onBarSelected">
            <option value="">-- Choose a bar --</option>
            <option v-for="bar in bars" :key="bar.id" :value="bar.id">
              {{ bar.name }} ({{ bar.location }})
            </option>
          </select>
        </div>

        <div v-if="selectedBarRequests.length > 0" class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>Request ID</th>
                <th>Product</th>
                <th>Quantity</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="request in selectedBarRequests" :key="request.id">
                <td>{{ request.id }}</td>
                <td>{{ request.items[0]?.productName }}</td>
                <td>{{ request.items[0]?.quantity }}</td>
                <td>
                  <span
                    class="status-badge"
                    :class="getRequestStatusClass(request.status)"
                  >
                    {{ request.status }}
                  </span>
                </td>
                <td class="actions-cell">
                  <button
                    v-if="request.status === 'REQUESTED'"
                    @click="processRequest(request, 'REQUESTED')"
                    class="btn-process"
                  >
                    Process
                  </button>
                  <button
                    v-if="request.status === 'REQUESTED'"
                    @click="rejectRequest(request)"
                    class="btn-reject"
                  >
                    Reject
                  </button>
                  <button
                    v-if="request.status === 'IN_PROGRESS'"
                    @click="deliverRequest(request)"
                    class="btn-deliver"
                  >
                    Deliver
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-else-if="selectedBarId" class="empty-state">
          No supply requests for this bar.
        </div>
      </div>
    </div>

    <!-- Empties Collected Tab -->
    <div v-if="activeTab === 'empties'" class="tab-content">
      <div class="section-header">
        <h2>Empties Collected from Drop Points</h2>
        <button @click="fetchEmptiesData" class="btn-refresh">
          Refresh
        </button>
      </div>

      <div v-if="loading" class="loading-state">Loading empties data...</div>

      <div v-else-if="emptiesCollected.length > 0" class="table-container">
        <table class="data-table">
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
              <td>{{ item.dropPointId }}</td>
              <td>{{ item.location }}</td>
              <td>{{ item.emptiesCount }}</td>
              <td>{{ formatDate(item.collectionDate) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="empty-state">
        No empties collected yet.
      </div>
    </div>

    <!-- Replenish Stock Modal -->
    <div v-if="showReplenishModal" class="modal-overlay" @click.self="closeReplenishModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>Replenish Stock</h2>
          <button @click="closeReplenishModal" class="close-btn">&times;</button>
        </div>

        <form @submit.prevent="handleReplenish" class="modal-form">
          <div class="form-group">
            <label for="productName">Product Name</label>
            <input
              id="productName"
              v-model="replenishForm.productName"
              type="text"
              required
              placeholder="e.g., Beer, Cola"
            />
          </div>

          <div class="form-group">
            <label for="quantity">Quantity</label>
            <input
              id="quantity"
              v-model.number="replenishForm.quantity"
              type="number"
              required
              min="1"
              placeholder="Number of units"
            />
          </div>

          <div class="modal-actions">
            <button type="submit" class="btn-primary">Replenish</button>
            <button type="button" @click="closeReplenishModal" class="btn-secondary">
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useWarehouseStore } from '@/stores/warehouseStore';
import { Bar, SupplyRequest } from '@/api/warehouseApi';
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';

const warehouseStore = useWarehouseStore();

// State
const activeTab = ref<'stock' | 'supply' | 'empties'>('stock');
const viewMode = ref<'all' | 'bar'>('all');
const selectedBarId = ref<string>('');
const showReplenishModal = ref(false);
const replenishForm = ref({
  productName: '',
  quantity: 0,
});

// Computed
const stock = computed(() => warehouseStore.stock);
const bars = computed(() => warehouseStore.bars);
const supplyRequests = computed(() => warehouseStore.supplyRequests);
const selectedBarRequests = computed(() => warehouseStore.selectedBarRequests);
const emptiesCollected = computed(() => warehouseStore.emptiesCollected);
const loading = computed(() => warehouseStore.loading);
const totalSKUs = computed(() => warehouseStore.totalSKUs);
const lowStockItems = computed(() => warehouseStore.lowStockItems);
const pendingRequests = computed(() => warehouseStore.pendingRequests);
const completedRequests = computed(() => warehouseStore.completedRequests);

// Methods
const switchViewMode = async (mode: 'all' | 'bar') => {
  viewMode.value = mode;
  if (mode === 'all') {
    selectedBarId.value = '';
    warehouseStore.setSelectedBar(null);
  }
};

const viewBarRequests = async (bar: Bar) => {
  viewMode.value = 'bar';
  selectedBarId.value = bar.id.toString();
  warehouseStore.setSelectedBar(bar);
  await warehouseStore.fetchBarSupplyRequests(bar.id);
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

const processRequest = async (request: SupplyRequest, currentStatus: string) => {
  if (!confirm('Process this request?')) return;

  try {
    await warehouseStore.updateSupplyRequest(request.barId, request.id, {
      status: 'IN_PROGRESS',
      quantity: request.items[0]?.quantity || 0,
    });
    toast.success('Request processed successfully');
    await warehouseStore.fetchBarSupplyRequests(request.barId);
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to process request';
    toast.error(errorMessage);
  }
};

const rejectRequest = async (request: SupplyRequest) => {
  if (!confirm('Reject this request?')) return;

  try {
    await warehouseStore.updateSupplyRequest(request.barId, request.id, {
      status: 'REJECTED',
      quantity: 0,
    });
    toast.warning('Request rejected');
    await warehouseStore.fetchBarSupplyRequests(request.barId);
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to reject request';
    toast.error(errorMessage);
  }
};

const deliverRequest = async (request: SupplyRequest) => {
  if (!confirm('Mark as delivered?')) return;

  try {
    await warehouseStore.updateSupplyRequest(request.barId, request.id, {
      status: 'DELIVERED',
      quantity: request.items[0]?.quantity || 0,
    });
    toast.success('Request delivered successfully');
    await warehouseStore.fetchBarSupplyRequests(request.barId);
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to deliver request';
    toast.error(errorMessage);
  }
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
    await warehouseStore.replenishStock(replenishForm.value);
    toast.success(`Stock replenished: ${replenishForm.value.productName}`);
    closeReplenishModal();
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to replenish stock';
    toast.error(errorMessage);
  }
};

const fetchEmptiesData = async () => {
  try {
    await warehouseStore.fetchEmptiesCollected();
    toast.success('Empties data refreshed');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to fetch empties';
    toast.error(errorMessage);
  }
};

const getRequestStatusClass = (status: string) => {
  switch (status) {
    case 'REQUESTED':
      return 'status-requested';
    case 'IN_PROGRESS':
      return 'status-in-progress';
    case 'DELIVERED':
      return 'status-delivered';
    case 'REJECTED':
      return 'status-rejected';
    default:
      return '';
  }
};

const formatDate = (date: string) => {
  if (!date) return 'N/A';
  return new Date(date).toLocaleDateString();
};

// Lifecycle
onMounted(async () => {
  await warehouseStore.fetchStock();
  await warehouseStore.fetchBars();
});
</script>

<style scoped>
.warehouse-container {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 2rem;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.card {
  background: white;
  padding: 1.5rem;
  border-radius: 0.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.card.warning {
  background: #fef3c7;
}

.card h3 {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 0.5rem;
}

.card .value {
  font-size: 2rem;
  font-weight: bold;
  color: #111827;
}

.tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2rem;
  border-bottom: 2px solid #e5e7eb;
}

.tab {
  padding: 0.75rem 1.5rem;
  background: none;
  border: none;
  cursor: pointer;
  font-weight: 500;
  color: #6b7280;
  border-bottom: 3px solid transparent;
  transition: all 0.2s;
}

.tab.active {
  color: #3b82f6;
  border-bottom-color: #3b82f6;
}

.tab-content {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
}

.btn-primary,
.btn-refresh,
.btn-view,
.btn-process,
.btn-reject,
.btn-deliver,
.btn-secondary {
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  border: none;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary {
  background-color: #3b82f6;
  color: white;
}

.btn-primary:hover {
  background-color: #2563eb;
}

.btn-refresh {
  background-color: #10b981;
  color: white;
}

.btn-refresh:hover {
  background-color: #059669;
}

.btn-view {
  background-color: #8b5cf6;
  color: white;
  font-size: 0.875rem;
}

.btn-view:hover {
  background-color: #7c3aed;
}

.btn-process {
  background-color: #3b82f6;
  color: white;
  font-size: 0.875rem;
}

.btn-process:hover {
  background-color: #2563eb;
}

.btn-reject {
  background-color: #ef4444;
  color: white;
  font-size: 0.875rem;
}

.btn-reject:hover {
  background-color: #dc2626;
}

.btn-deliver {
  background-color: #10b981;
  color: white;
  font-size: 0.875rem;
}

.btn-deliver:hover {
  background-color: #059669;
}

.btn-secondary {
  background-color: #e5e7eb;
  color: #374151;
}

.btn-secondary:hover {
  background-color: #d1d5db;
}

.mode-toggle {
  display: flex;
  gap: 0.5rem;
}

.mode-toggle button {
  padding: 0.5rem 1rem;
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  cursor: pointer;
  font-weight: 500;
}

.mode-toggle button.active {
  background: #3b82f6;
  color: white;
  border-color: #3b82f6;
}

.controls {
  margin-bottom: 1.5rem;
}

.controls label {
  display: inline-block;
  margin-right: 1rem;
  font-weight: 500;
}

.controls select {
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  font-size: 1rem;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead {
  background-color: #f9fafb;
  border-bottom: 2px solid #e5e7eb;
}

.data-table th {
  padding: 0.75rem;
  text-align: left;
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
  text-transform: uppercase;
}

.data-table td {
  padding: 0.75rem;
  border-bottom: 1px solid #e5e7eb;
}

.data-table tbody tr:hover {
  background-color: #f9fafb;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-ok {
  background-color: #d1fae5;
  color: #065f46;
}

.status-low {
  background-color: #fef3c7;
  color: #92400e;
}

.status-requested {
  background-color: #dbeafe;
  color: #1e40af;
}

.status-in-progress {
  background-color: #fef3c7;
  color: #92400e;
}

.status-delivered {
  background-color: #d1fae5;
  color: #065f46;
}

.status-rejected {
  background-color: #fee2e2;
  color: #991b1b;
}

.actions-cell {
  display: flex;
  gap: 0.5rem;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 0.75rem;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: #6b7280;
  cursor: pointer;
  padding: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.375rem;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #f3f4f6;
}

.modal-form {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 1rem;
}

.form-group input:focus {
  outline: none;
  border-color: #3b82f6;
}

.modal-actions {
  display: flex;
  gap: 1rem;
}

.modal-actions button {
  flex: 1;
  padding: 0.75rem;
  border-radius: 0.5rem;
  font-weight: 600;
}
</style>
