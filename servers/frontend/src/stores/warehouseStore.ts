import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import {
  warehouseApi,
  WarehouseStock,
  SupplyRequest,
  Bar,
  UpdateSupplyRequestDto,
  ReplenishStockDto,
} from '../api/warehouseApi';

export const useWarehouseStore = defineStore('warehouse', () => {
  // State
  const stock = ref<WarehouseStock[]>([]);
  const bars = ref<Bar[]>([]);
  const supplyRequests = ref<SupplyRequest[]>([]);
  const selectedBar = ref<Bar | null>(null);
  const emptiesCollected = ref<any[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Getters
  const totalSKUs = computed(() => stock.value.length);

  const totalInventoryValue = computed(() =>
    stock.value.reduce((sum, item) => sum + (item.quantity * (item.unitPrice || 0)), 0)
  );

  const lowStockItems = computed(() =>
    stock.value.filter(item => item.quantity < 10)
  );

  const pendingRequests = computed(() =>
    supplyRequests.value.filter(req => req.status === 'REQUESTED')
  );

  const inProgressRequests = computed(() =>
    supplyRequests.value.filter(req => req.status === 'IN_PROGRESS')
  );

  const completedRequests = computed(() =>
    supplyRequests.value.filter(req => req.status === 'DELIVERED')
  );

  const selectedBarRequests = computed(() => {
    if (!selectedBar.value) return [];
    return supplyRequests.value.filter(req => req.barId === selectedBar.value!.id);
  });

  // Actions
  async function fetchStock() {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.getStock();
      stock.value = response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch stock';
      console.error('Error fetching stock:', err);
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
        item => item.productName === response.data.productName
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

  async function fetchBarSupplyRequests(barId: number) {
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
      supplyRequests.value = response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch all supply requests';
      console.error('Error fetching all supply requests:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateSupplyRequest(
    barId: number,
    requestId: number,
    data: UpdateSupplyRequestDto
  ) {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.updateSupplyRequest(barId, requestId, data);
      const index = supplyRequests.value.findIndex(req => req.id === requestId);
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

  async function fetchEmptiesCollected() {
    loading.value = true;
    error.value = null;
    try {
      const response = await warehouseApi.getEmptiesCollected();
      emptiesCollected.value = response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch empties collected';
      console.error('Error fetching empties collected:', err);
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
    supplyRequests,
    selectedBar,
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
    selectedBarRequests,
    // Actions
    fetchStock,
    fetchStockItem,
    replenishStock,
    fetchBars,
    fetchBarSupplyRequests,
    fetchAllSupplyRequests,
    updateSupplyRequest,
    fetchDropPointDashboard,
    fetchEmptiesCollected,
    setSelectedBar,
    clearError,
  };
});
