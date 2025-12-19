import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { droppointApi, DropPoint, CreateDropPointDto, UpdateDropPointDto } from '../api/droppointApi';

export const useDroppointStore = defineStore('droppoint', () => {
  // State
  const droppoints = ref<DropPoint[]>([]);
  const currentDroppoint = ref<DropPoint | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Getters
  const emptyDroppoints = computed(() => 
    droppoints.value.filter(dp => dp.status === 'EMPTY')
  );

  const fullDroppoints = computed(() => 
    droppoints.value.filter(dp => dp.status === 'FULL')
  );

  const notifiedDroppoints = computed(() => 
    droppoints.value.filter(dp => dp.status === 'FULL_AND_NOTIFIED_TO_WAREHOUSE')
  );

  const droppointCount = computed(() => droppoints.value.length);

  // Actions
  async function fetchDropPoints() {
    loading.value = true;
    error.value = null;
    try {
      const response = await droppointApi.getDropPoints();
      droppoints.value = response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch drop points';
      console.error('Error fetching drop points:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchDropPoint(id: number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await droppointApi.getDropPoint(id);
      currentDroppoint.value = response.data;
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch drop point';
      console.error('Error fetching drop point:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function createDropPoint(data: CreateDropPointDto) {
    loading.value = true;
    error.value = null;
    try {
      const response = await droppointApi.createDropPoint(data);
      droppoints.value.push(response.data);
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to create drop point';
      console.error('Error creating drop point:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateDropPoint(id: number, data: UpdateDropPointDto) {
    loading.value = true;
    error.value = null;
    try {
      const response = await droppointApi.updateDropPoint(id, data);
      const index = droppoints.value.findIndex(dp => dp.id === id);
      if (index !== -1) {
        droppoints.value[index] = response.data;
      }
      if (currentDroppoint.value?.id === id) {
        currentDroppoint.value = response.data;
      }
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to update drop point';
      console.error('Error updating drop point:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function deleteDropPoint(id: number) {
    loading.value = true;
    error.value = null;
    try {
      await droppointApi.deleteDropPoint(id);
      droppoints.value = droppoints.value.filter(dp => dp.id !== id);
      if (currentDroppoint.value?.id === id) {
        currentDroppoint.value = null;
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to delete drop point';
      console.error('Error deleting drop point:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function addEmpties(id: number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await droppointApi.addEmpties(id);
      const index = droppoints.value.findIndex(dp => dp.id === id);
      if (index !== -1) {
        droppoints.value[index] = response.data;
      }
      if (currentDroppoint.value?.id === id) {
        currentDroppoint.value = response.data;
      }
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to add empties';
      console.error('Error adding empties:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function notifyWarehouse(id: number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await droppointApi.notifyWarehouse(id);
      const index = droppoints.value.findIndex(dp => dp.id === id);
      if (index !== -1) {
        droppoints.value[index] = response.data;
      }
      if (currentDroppoint.value?.id === id) {
        currentDroppoint.value = response.data;
      }
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to notify warehouse';
      console.error('Error notifying warehouse:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function verifyTransfer(id: number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await droppointApi.verifyTransfer(id);
      const index = droppoints.value.findIndex(dp => dp.id === id);
      if (index !== -1) {
        droppoints.value[index] = response.data;
      }
      if (currentDroppoint.value?.id === id) {
        currentDroppoint.value = response.data;
      }
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to verify transfer';
      console.error('Error verifying transfer:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  function clearError() {
    error.value = null;
  }

  return {
    // State
    droppoints,
    currentDroppoint,
    loading,
    error,
    // Getters
    emptyDroppoints,
    fullDroppoints,
    notifiedDroppoints,
    droppointCount,
    // Actions
    fetchDropPoints,
    fetchDropPoint,
    createDropPoint,
    updateDropPoint,
    deleteDropPoint,
    addEmpties,
    notifyWarehouse,
    verifyTransfer,
    clearError,
  };
});
