import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import {
  eventApi,
  EventSummary,
  EventDetail,
  EventBar,
  EventDropPoint,
  BarStock,
  CreateEventRequest,
  UpdateEventRequest,
  CreateBarRequest,
  UpdateBarRequest,
  CreateDropPointRequest,
  UpdateDropPointRequest,
  CreateBarStockRequest,
  UserDto,
  WarehouseStockDto,
  EventStatus,
} from '../api/eventApi';

export const useEventStore = defineStore('event', () => {
  // ============ State ============
  const events = ref<EventSummary[]>([]);
  const currentEvent = ref<EventDetail | null>(null);
  const currentBars = ref<EventBar[]>([]);
  const currentDropPoints = ref<EventDropPoint[]>([]);
  const currentBarStocks = ref<Map<number, BarStock[]>>(new Map());
  const availableStaff = ref<UserDto[]>([]);
  const warehouseInventory = ref<WarehouseStockDto[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // ============ Getters ============
  const eventCount = computed(() => events.value.length);

  const scheduledEvents = computed(() =>
    events.value.filter(event => event.status === 'SCHEDULED')
  );

  const runningEvents = computed(() =>
    events.value.filter(event => event.status === 'RUNNING')
  );

  const completedEvents = computed(() =>
    events.value.filter(event => event.status === 'COMPLETED')
  );

  const cancelledEvents = computed(() =>
    events.value.filter(event => event.status === 'CANCELLED')
  );

  const upcomingEvents = computed(() => {
    const today = new Date().toISOString().split('T')[0];
    return events.value.filter(
      event => event.date >= today && (event.status === 'SCHEDULED' || event.status === 'RUNNING')
    );
  });

  const pastEvents = computed(() =>
    events.value.filter(
      event => event.status === 'COMPLETED' || event.status === 'CANCELLED'
    )
  );

  const statusCounts = computed(() => ({
    scheduled: scheduledEvents.value.length,
    running: runningEvents.value.length,
    completed: completedEvents.value.length,
    cancelled: cancelledEvents.value.length,
  }));

  // ============ Event Actions ============

  async function fetchEvents() {
    loading.value = true;
    error.value = null;
    try {
      const response = await eventApi.getEvents();
      events.value = response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch events';
      console.error('Error fetching events:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchEvent(id: number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await eventApi.getEvent(id);
      currentEvent.value = response.data;
      currentBars.value = response.data.bars || [];
      currentDropPoints.value = response.data.dropPoints || [];
      return currentEvent.value;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch event';
      console.error('Error fetching event:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function createEvent(data: CreateEventRequest) {
    loading.value = true;
    error.value = null;
    try {
      const response = await eventApi.createEvent(data);
      events.value.push(response.data);
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to create event';
      console.error('Error creating event:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateEvent(id: number, data: UpdateEventRequest) {
    loading.value = true;
    error.value = null;
    try {
      const response = await eventApi.updateEvent(id, data);
      const index = events.value.findIndex(event => event.id === id);
      if (index !== -1) {
        events.value[index] = { ...events.value[index], ...response.data };
      }
      if (currentEvent.value?.id === id) {
        currentEvent.value = response.data;
      }
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to update event';
      console.error('Error updating event:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function deleteEvent(id: number) {
    loading.value = true;
    error.value = null;
    try {
      await eventApi.deleteEvent(id);
      events.value = events.value.filter(event => event.id !== id);
      if (currentEvent.value?.id === id) {
        currentEvent.value = null;
      }
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to delete event';
      console.error('Error deleting event:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function startEvent(id: number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await eventApi.startEvent(id);
      updateEventInList(id, response.data);
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to start event';
      console.error('Error starting event:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function completeEvent(id: number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await eventApi.completeEvent(id);
      updateEventInList(id, response.data);
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to complete event';
      console.error('Error completing event:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function cancelEvent(id: number) {
    loading.value = true;
    error.value = null;
    try {
      const response = await eventApi.cancelEvent(id);
      updateEventInList(id, response.data);
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to cancel event';
      console.error('Error cancelling event:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  function updateEventInList(id: number, updatedEvent: EventDetail) {
    const index = events.value.findIndex(e => e.id === id);
    if (index !== -1) {
      events.value[index] = {
        ...events.value[index],
        status: updatedEvent.status,
        name: updatedEvent.name,
        date: updatedEvent.date,
        location: updatedEvent.location,
      };
    }
    if (currentEvent.value?.id === id) {
      currentEvent.value = updatedEvent;
      currentBars.value = updatedEvent.bars || [];
      currentDropPoints.value = updatedEvent.dropPoints || [];
    }
  }

  // ============ Bar Actions ============

  async function fetchEventBars(eventId: number) {
    try {
      const response = await eventApi.getEventBars(eventId);
      currentBars.value = response.data;
      return response.data;
    } catch (err) {
      console.error('Error fetching bars:', err);
      throw err;
    }
  }

  async function createBar(data: CreateBarRequest) {
    try {
      const response = await eventApi.createBar(data);
      currentBars.value.push(response.data);
      return response.data;
    } catch (err) {
      console.error('Error creating bar:', err);
      throw err;
    }
  }

  async function updateBar(id: number, data: UpdateBarRequest) {
    try {
      const response = await eventApi.updateBar(id, data);
      const index = currentBars.value.findIndex(bar => bar.id === id);
      if (index !== -1) {
        currentBars.value[index] = response.data;
      }
      return response.data;
    } catch (err) {
      console.error('Error updating bar:', err);
      throw err;
    }
  }

  async function deleteBar(id: number) {
    try {
      await eventApi.deleteBar(id);
      currentBars.value = currentBars.value.filter(bar => bar.id !== id);
    } catch (err) {
      console.error('Error deleting bar:', err);
      throw err;
    }
  }

  async function assignBarStaff(barId: number, staffId: string) {
    try {
      const response = await eventApi.assignBarStaff(barId, staffId);
      const index = currentBars.value.findIndex(bar => bar.id === barId);
      if (index !== -1) {
        currentBars.value[index] = response.data;
      }
      return response.data;
    } catch (err) {
      console.error('Error assigning staff to bar:', err);
      throw err;
    }
  }

  // ============ Drop Point Actions ============

  async function fetchEventDropPoints(eventId: number) {
    try {
      const response = await eventApi.getEventDropPoints(eventId);
      currentDropPoints.value = response.data;
      return response.data;
    } catch (err) {
      console.error('Error fetching drop points:', err);
      throw err;
    }
  }

  async function createDropPoint(data: CreateDropPointRequest) {
    try {
      const response = await eventApi.createDropPoint(data);
      currentDropPoints.value.push(response.data);
      return response.data;
    } catch (err) {
      console.error('Error creating drop point:', err);
      throw err;
    }
  }

  async function updateDropPoint(id: number, data: UpdateDropPointRequest) {
    try {
      const response = await eventApi.updateDropPoint(id, data);
      const index = currentDropPoints.value.findIndex(dp => dp.id === id);
      if (index !== -1) {
        currentDropPoints.value[index] = response.data;
      }
      return response.data;
    } catch (err) {
      console.error('Error updating drop point:', err);
      throw err;
    }
  }

  async function deleteDropPoint(id: number) {
    try {
      await eventApi.deleteDropPoint(id);
      currentDropPoints.value = currentDropPoints.value.filter(dp => dp.id !== id);
    } catch (err) {
      console.error('Error deleting drop point:', err);
      throw err;
    }
  }

  async function assignDropPointStaff(dropPointId: number, staffId: string) {
    try {
      const response = await eventApi.assignDropPointStaff(dropPointId, staffId);
      const index = currentDropPoints.value.findIndex(dp => dp.id === dropPointId);
      if (index !== -1) {
        currentDropPoints.value[index] = response.data;
      }
      return response.data;
    } catch (err) {
      console.error('Error assigning staff to drop point:', err);
      throw err;
    }
  }

  // ============ Bar Stock Actions ============

  async function fetchBarStocks(barId: number) {
    try {
      const response = await eventApi.getBarStocks(barId);
      currentBarStocks.value.set(barId, response.data);
      return response.data;
    } catch (err) {
      console.error('Error fetching bar stocks:', err);
      throw err;
    }
  }

  async function addBarStock(barId: number, data: CreateBarStockRequest) {
    try {
      const response = await eventApi.addBarStock(barId, data);
      const stocks = currentBarStocks.value.get(barId) || [];
      stocks.push(response.data);
      currentBarStocks.value.set(barId, stocks);
      return response.data;
    } catch (err) {
      console.error('Error adding bar stock:', err);
      throw err;
    }
  }

  async function deleteBarStock(barId: number, stockId: number) {
    try {
      await eventApi.deleteBarStock(barId, stockId);
      const stocks = currentBarStocks.value.get(barId) || [];
      currentBarStocks.value.set(
        barId,
        stocks.filter(s => s.id !== stockId)
      );
    } catch (err) {
      console.error('Error deleting bar stock:', err);
      throw err;
    }
  }

  // ============ External Data Actions ============

  async function fetchAvailableStaff() {
    try {
      const response = await eventApi.getAvailableBarStaff();
      availableStaff.value = response.data;
      return response.data;
    } catch (err) {
      console.error('Error fetching available staff:', err);
      return [];
    }
  }

  async function fetchWarehouseInventory() {
    try {
      const response = await eventApi.getWarehouseInventory();
      warehouseInventory.value = response.data;
      return response.data;
    } catch (err) {
      console.error('Error fetching warehouse inventory:', err);
      return [];
    }
  }

  // ============ Utility Functions ============

  function clearError() {
    error.value = null;
  }

  function clearCurrentEvent() {
    currentEvent.value = null;
    currentBars.value = [];
    currentDropPoints.value = [];
    currentBarStocks.value.clear();
  }

  function getBarStocks(barId: number): BarStock[] {
    return currentBarStocks.value.get(barId) || [];
  }

  // ============ Return ============
  return {
    // State
    events,
    currentEvent,
    currentBars,
    currentDropPoints,
    currentBarStocks,
    availableStaff,
    warehouseInventory,
    loading,
    error,
    // Getters
    eventCount,
    scheduledEvents,
    runningEvents,
    completedEvents,
    cancelledEvents,
    upcomingEvents,
    pastEvents,
    statusCounts,
    // Event Actions
    fetchEvents,
    fetchEvent,
    createEvent,
    updateEvent,
    deleteEvent,
    startEvent,
    completeEvent,
    cancelEvent,
    // Bar Actions
    fetchEventBars,
    createBar,
    updateBar,
    deleteBar,
    assignBarStaff,
    // Drop Point Actions
    fetchEventDropPoints,
    createDropPoint,
    updateDropPoint,
    deleteDropPoint,
    assignDropPointStaff,
    // Bar Stock Actions
    fetchBarStocks,
    addBarStock,
    deleteBarStock,
    getBarStocks,
    // External Data
    fetchAvailableStaff,
    fetchWarehouseInventory,
    // Utility
    clearError,
    clearCurrentEvent,
  };
});
