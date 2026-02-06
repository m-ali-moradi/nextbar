import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import {
  warehouseApi,
  WarehouseStock,
  SupplyRequest,
  Bar,
  ReplenishStockDto,
  CollectionRecord,
  EmptyBottleInventory,
} from '../api/warehouseApi';

export const useWarehouseStore = defineStore('warehouse', () => {
  // State
  const stock = ref<WarehouseStock[]>([]);
  const bars = ref<Bar[]>([]);
  const allSupplyRequests = ref<SupplyRequest[]>([]);
  const supplyRequests = ref<SupplyRequest[]>([]);
  const selectedBar = ref<Bar | null>(null);
  const collections = ref<CollectionRecord[]>([]);
  const emptiesCollected = ref<EmptyBottleInventory[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Note: WebSocket event subscriptions are now handled directly in components
  // using the useWebSocketEvents composable for proper Vue reactivity.
  // See WarehouseView.vue for the implementation.

  // Getters
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

  // Actions
  async function fetchStock() {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.getStock();
      // Map to ensure productName is available for backward compatibility
      stock.value = response.data.map(item => ({
        ...item,
        productName: item.productName || item.beverageType
      }));
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch stock';
      console.error('Error fetching stock:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchLowStock() {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.getLowStockItems();
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch low stock items';
      console.error('Error fetching low stock:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchStockItem(productId: number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.getStockItem(productId);
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch stock item';
      console.error('Error fetching stock item:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function replenishStock(data: ReplenishStockDto) {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.replenishStock(data);
      // Update or add to stock
      const index = stock.value.findIndex(
        item => item.beverageType === response.data.beverageType ||
          item.productName === data.beverageType
      );
      if (index !== -1) {
        stock.value[index] = response.data;
      } else {
        stock.value.push(response.data);
      }
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to replenish stock';
      console.error('Error replenishing stock:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function addToStock(beverageType: string, quantity: number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.addStock(beverageType, quantity);
      // Update in stock list
      const index = stock.value.findIndex(item => item.beverageType === beverageType);
      if (index !== -1) {
        stock.value[index] = response.data;
      }
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to add stock';
      console.error('Error adding stock:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function deleteStockItem(id: number) {
    loading.value = true;
    error.value = null;
    try {
      await warehouseApi.deleteStock(id);
      stock.value = stock.value.filter(item => item.id !== id);
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to delete stock';
      console.error('Error deleting stock:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchBars() {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.getBars();
      bars.value = response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch bars';
      console.error('Error fetching bars:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchBarSupplyRequests(barId: string | number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.getBarSupplyRequests(barId);
      supplyRequests.value = response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch supply requests';
      console.error('Error fetching supply requests:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchAllSupplyRequests() {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.getAllSupplyRequests();
      allSupplyRequests.value = response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch all supply requests';
      console.error('Error fetching all supply requests:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function rejectSupplyRequest(
    barId: string | number,
    requestId: string | number,
    reason: string
  ) {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.rejectSupplyRequest(barId, requestId, reason);
      const index = allSupplyRequests.value.findIndex(req => String(req.id) === String(requestId));
      if (index !== -1) {
        allSupplyRequests.value[index] = response.data;
      }
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to reject supply request';
      console.error('Error rejecting supply request:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateSupplyRequest(
    barId: string | number,
    requestId: string | number,
    data: { status: string; quantity: number }
  ) {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.updateSupplyRequest(barId, requestId, data);
      const index = supplyRequests.value.findIndex(req => String(req.id) === String(requestId));
      if (index !== -1) {
        supplyRequests.value[index] = response.data;
      }
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to update supply request';
      console.error('Error updating supply request:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function processSupplyRequest(
    barId: string | number,
    requestId: string | number,
    beverageType: string,
    quantity: number,
    currentStatus: string
  ) {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.processSupplyRequest(barId, requestId, {
        beverageType,
        quantity,
        currentStatus
      });
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to process supply request';
      console.error('Error processing supply request:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  // Collections
  async function syncCollections() {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.syncCollections();
      collections.value = response.data;
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to sync collections';
      console.error('Error syncing collections:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchPendingCollections() {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.getPendingCollections();
      collections.value = response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch pending collections';
      console.error('Error fetching pending collections:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function acceptCollection(collectionId: number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.acceptCollection(collectionId);
      const index = collections.value.findIndex(c => c.id === collectionId);
      if (index !== -1) {
        collections.value[index] = response.data;
      }
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to accept collection';
      console.error('Error accepting collection:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function completeCollection(collectionId: number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.completeCollection(collectionId);
      const index = collections.value.findIndex(c => c.id === collectionId);
      if (index !== -1) {
        collections.value[index] = response.data;
      }
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to complete collection';
      console.error('Error completing collection:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchEmptiesCollected() {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.getEmptiesCollected();
      // Map for backward compatibility with frontend
      emptiesCollected.value = response.data.map(item => ({
        ...item,
        location: item.location || item.dropPointLocation,
        emptiesCount: item.emptiesCount || item.totalBottlesCollected,
        collectionDate: item.collectionDate || item.lastCollectionAt
      }));
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch empties collected';
      console.error('Error fetching empties collected:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchDropPointDashboard() {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.getDropPointDashboard();
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch drop point dashboard';
      console.error('Error fetching drop point dashboard:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  function setSelectedBar(bar: Bar | null) {
    selectedBar.value = bar;
  }

  function clearError() {
    error.value = null;
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
    // Actions
    fetchStock,
    fetchLowStock,
    fetchStockItem,
    replenishStock,
    addToStock,
    deleteStockItem,
    fetchBars,
    fetchBarSupplyRequests,
    fetchAllSupplyRequests,
    rejectSupplyRequest,
    updateSupplyRequest,
    processSupplyRequest,
    syncCollections,
    fetchPendingCollections,
    acceptCollection,
    completeCollection,
    fetchEmptiesCollected,
    fetchDropPointDashboard,
    setSelectedBar,
    clearError,
  };
});
