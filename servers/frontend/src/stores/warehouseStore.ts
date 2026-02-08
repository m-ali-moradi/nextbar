import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import {
  warehouseApi,
  ApiErrorResponse,
} from '../api';
import type {
  Bar,
  WarehouseStock,
  SupplyRequest,
  ReplenishStockPayload,
  FulfillSupplyRequestPayload,
  RejectSupplyRequestPayload,
  CollectionRecord,
  EmptyBottleInventory,
} from '../api/types';

export const useWarehouseStore = defineStore('warehouse', () => {
  // =====================================================
  // State
  // =====================================================
  const stock = ref<WarehouseStock[]>([]);
  const bars = ref<Bar[]>([]);
  const allSupplyRequests = ref<SupplyRequest[]>([]);
  const supplyRequests = ref<SupplyRequest[]>([]);
  const selectedBar = ref<Bar | null>(null);
  const collections = ref<CollectionRecord[]>([]);
  const emptiesCollected = ref<EmptyBottleInventory[]>([]);
  const loading = ref(false);
  const error = ref<{ message: string; code?: string } | null>(null);

  // =====================================================
  // Getters
  // =====================================================
  const totalSKUs = computed(() => stock.value.length);

  const totalInventoryValue = computed(() =>
    stock.value.reduce((sum, item) => sum + (item.quantity * (item.unitPrice || 0)), 0)
  );

  const lowStockItems = computed(() =>
    stock.value.filter(item => item.lowStock || item.quantity < (item.minStockLevel || 10))
  );

  const pendingRequests = computed(() =>
    allSupplyRequests.value.filter(req => req.status === 'REQUESTED')
  );

  const inProgressRequests = computed(() =>
    allSupplyRequests.value.filter(req => req.status === 'IN_PROGRESS')
  );

  const completedRequests = computed(() =>
    allSupplyRequests.value.filter(req => req.status === 'DELIVERED')
  );

  const rejectedRequests = computed(() =>
    allSupplyRequests.value.filter(req => req.status === 'REJECTED')
  );

  const selectedBarRequests = computed(() => {
    if (!selectedBar.value) return [];
    return supplyRequests.value.filter(req => String(req.barId) === String(selectedBar.value!.id));
  });

  const pendingCollections = computed(() =>
    collections.value.filter(c => c.status === 'PENDING')
  );

  const acceptedCollections = computed(() =>
    collections.value.filter(c => c.status === 'ACCEPTED')
  );

  // =====================================================
  // Error Handling
  // =====================================================
  function handleError(err: unknown): void {
    if (err instanceof ApiErrorResponse) {
      error.value = {
        message: err.message,
        code: err.code,
      };
    } else if (err instanceof Error) {
      error.value = { message: err.message };
    } else {
      error.value = { message: 'An unexpected error occurred' };
    }
  }

  function clearError(): void {
    error.value = null;
  }

  // =====================================================
  // Helper for running actions with loading state
  // =====================================================
  async function runWithLoading<T>(
    action: () => Promise<T>,
    errorMessage: string,
    logLabel: string,
    options: { silent?: boolean } = {}
  ): Promise<T> {
    if (!options.silent) {
      loading.value = true;
    }
    clearError();
    try {
      return await action();
    } catch (err) {
      handleError(err);
      console.error(logLabel, err);
      throw err;
    } finally {
      if (!options.silent) {
        loading.value = false;
      }
    }
  }

  // =====================================================
  // Stock Actions
  // =====================================================
  async function fetchStock(options: { silent?: boolean } = {}) {
    return runWithLoading(async () => {
      const response = await warehouseApi.getStock();
      // Map to ensure productName is available for backward compatibility
      stock.value = response.data.map(item => ({
        ...item,
        productName: item.productName || item.beverageType
      }));
    }, 'Failed to fetch stock', 'Error fetching stock:', options);
  }

  async function fetchLowStock() {
    return runWithLoading(async () => {
      const response = await warehouseApi.getLowStockItems();
      return response.data;
    }, 'Failed to fetch low stock items', 'Error fetching low stock:');
  }

  async function fetchStockItem(productId: number) {
    return runWithLoading(async () => {
      const response = await warehouseApi.getStockItem(productId);
      return response.data;
    }, 'Failed to fetch stock item', 'Error fetching stock item:');
  }

  async function createStock(payload: ReplenishStockPayload) {
    return runWithLoading(async () => {
      const response = await warehouseApi.createStock(payload);
      stock.value.push(response.data);
      return response.data;
    }, 'Failed to create stock', 'Error creating stock:');
  }

  async function updateStockQuantity(beverageType: string, quantity: number) {
    return runWithLoading(async () => {
      const response = await warehouseApi.updateStockQuantity(beverageType, { quantity });
      const index = stock.value.findIndex(item => item.beverageType === beverageType);
      if (index !== -1) {
        stock.value[index] = response.data;
      }
      return response.data;
    }, 'Failed to update stock', 'Error updating stock:');
  }

  async function addToStock(beverageType: string, quantity: number) {
    return runWithLoading(async () => {
      const response = await warehouseApi.addStock(beverageType, quantity);
      const index = stock.value.findIndex(item => item.beverageType === beverageType);
      if (index !== -1) {
        stock.value[index] = response.data;
      }
      return response.data;
    }, 'Failed to add stock', 'Error adding stock:');
  }

  async function deleteStockItem(id: number) {
    return runWithLoading(async () => {
      await warehouseApi.deleteStock(id);
      stock.value = stock.value.filter(item => item.id !== id);
    }, 'Failed to delete stock', 'Error deleting stock:');
  }

  // =====================================================
  // Bar Actions
  // =====================================================
  async function fetchBars(options: { silent?: boolean } = {}) {
    return runWithLoading(async () => {
      const response = await warehouseApi.getBars();
      bars.value = response.data;
    }, 'Failed to fetch bars', 'Error fetching bars:', options);
  }

  // =====================================================
  // Supply Request Actions (REST-aligned naming)
  // =====================================================
  async function fetchBarSupplyRequests(barId: string | number, options: { silent?: boolean } = {}) {
    return runWithLoading(async () => {
      const response = await warehouseApi.getBarSupplyRequests(barId);
      supplyRequests.value = response.data;
    }, 'Failed to fetch supply requests', 'Error fetching supply requests:', options);
  }

  async function fetchAllSupplyRequests(options: { silent?: boolean } = {}) {
    return runWithLoading(async () => {
      const response = await warehouseApi.getAllSupplyRequests();
      allSupplyRequests.value = response.data;
    }, 'Failed to fetch all supply requests', 'Error fetching all supply requests:', options);
  }

  async function fulfillSupplyRequest(
    barId: string | number,
    requestId: string | number,
    payload: FulfillSupplyRequestPayload
  ) {
    return runWithLoading(async () => {
      const response = await warehouseApi.fulfillSupplyRequest(barId, requestId, payload);
      const index = allSupplyRequests.value.findIndex(req => String(req.id) === String(requestId));
      if (index !== -1) {
        allSupplyRequests.value[index] = response.data;
      }
      return response.data;
    }, 'Failed to fulfill supply request', 'Error fulfilling supply request:');
  }

  async function rejectSupplyRequest(
    barId: string | number,
    requestId: string | number,
    reason: string
  ) {
    return runWithLoading(async () => {
      const response = await warehouseApi.rejectSupplyRequest(barId, requestId, { reason });
      const index = allSupplyRequests.value.findIndex(req => String(req.id) === String(requestId));
      if (index !== -1) {
        allSupplyRequests.value[index] = response.data;
      }
      return response.data;
    }, 'Failed to reject supply request', 'Error rejecting supply request:');
  }

  async function updateSupplyRequestStatus(
    barId: string | number,
    requestId: string | number,
    status: string
  ) {
    return runWithLoading(async () => {
      const response = await warehouseApi.updateSupplyRequestStatus(barId, requestId, { status });
      const index = supplyRequests.value.findIndex(req => String(req.id) === String(requestId));
      if (index !== -1) {
        supplyRequests.value[index] = response.data;
      }
      return response.data;
    }, 'Failed to update supply request status', 'Error updating supply request status:');
  }

  // =====================================================
  // Collection Actions
  // =====================================================
  async function syncCollections() {
    return runWithLoading(async () => {
      const response = await warehouseApi.syncCollections();
      collections.value = response.data;
      return response.data;
    }, 'Failed to sync collections', 'Error syncing collections:');
  }

  async function fetchPendingCollections() {
    return runWithLoading(async () => {
      const response = await warehouseApi.getPendingCollections();
      collections.value = response.data;
    }, 'Failed to fetch pending collections', 'Error fetching pending collections:');
  }

  async function acceptCollection(collectionId: number) {
    return runWithLoading(async () => {
      const response = await warehouseApi.acceptCollection(collectionId);
      const index = collections.value.findIndex(c => c.id === collectionId);
      if (index !== -1) {
        collections.value[index] = response.data;
      }
      return response.data;
    }, 'Failed to accept collection', 'Error accepting collection:');
  }

  async function completeCollection(collectionId: number) {
    return runWithLoading(async () => {
      const response = await warehouseApi.completeCollection(collectionId);
      const index = collections.value.findIndex(c => c.id === collectionId);
      if (index !== -1) {
        collections.value[index] = response.data;
      }
      return response.data;
    }, 'Failed to complete collection', 'Error completing collection:');
  }

  async function fetchEmptiesCollected(options: { silent?: boolean } = {}) {
    return runWithLoading(async () => {
      const response = await warehouseApi.getInventorySummary();
      // Map for backward compatibility with frontend
      emptiesCollected.value = response.data.map(item => ({
        ...item,
        location: item.location || item.dropPointLocation,
        emptiesCount: item.emptiesCount || item.totalBottlesCollected,
        collectionDate: item.collectionDate || item.lastCollectionAt
      }));
    }, 'Failed to fetch empties collected', 'Error fetching empties collected:', options);
  }

  // =====================================================
  // Utility Actions
  // =====================================================
  function setSelectedBar(bar: Bar | null) {
    selectedBar.value = bar;
  }

  return {
    // State
    stock,
    bars,
    allSupplyRequests,
    supplyRequests,
    selectedBar,
    collections,
    emptiesCollected,
    loading,
    error,

    // Getters
    totalSKUs,
    totalInventoryValue,
    lowStockItems,
    pendingRequests,
    inProgressRequests,
    completedRequests,
    rejectedRequests,
    selectedBarRequests,
    pendingCollections,
    acceptedCollections,

    // Stock Actions
    fetchStock,
    fetchLowStock,
    fetchStockItem,
    createStock,
    updateStockQuantity,
    addToStock,
    deleteStockItem,

    // Bar Actions
    fetchBars,

    // Supply Request Actions (REST-aligned)
    fetchBarSupplyRequests,
    fetchAllSupplyRequests,
    fulfillSupplyRequest,
    rejectSupplyRequest,
    updateSupplyRequestStatus,

    // Collection Actions
    syncCollections,
    fetchPendingCollections,
    acceptCollection,
    completeCollection,
    fetchEmptiesCollected,

    // Utility
    setSelectedBar,
    clearError,
  };
});
