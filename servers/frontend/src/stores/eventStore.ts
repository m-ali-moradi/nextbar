import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import {
  eventApi,
  Event,
  Beverage,
  CreateEventDto,
  UpdateEventDto,
} from '../api/eventApi';

export const useEventStore = defineStore('event', () => {
  // State
  const events = ref<Event[]>([]);
  const currentEvent = ref<Event | null>(null);
  const beverages = ref<Beverage[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Getters
  const eventCount = computed(() => events.value.length);

  const plannedEvents = computed(() =>
    events.value.filter(event => event.status === 'PLANNED')
  );

  const ongoingEvents = computed(() =>
    events.value.filter(event => event.status === 'ONGOING')
  );

  const completedEvents = computed(() =>
    events.value.filter(event => event.status === 'COMPLETED')
  );

  const upcomingEvents = computed(() => {
    const today = new Date().toISOString().split('T')[0];
    return events.value.filter(event => event.date >= today && event.status !== 'COMPLETED');
  });

  // Actions
  async function fetchEvents() {
    loading.value = true;
    error.value = null;
    try {
      const response = await eventApi.getEvents();
      events.value = response.data.map(ev => ({
        ...ev,
        beverages: ev.beverages || [],
        bars: ev.bars || [],
        dropPoints: ev.dropPoints || [],
      }));
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
      currentEvent.value = {
        ...response.data,
        beverages: response.data.beverages || [],
        bars: response.data.bars || [],
        dropPoints: response.data.dropPoints || [],
      };
      return currentEvent.value;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch event';
      console.error('Error fetching event:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function fetchBeverages() {
    loading.value = true;
    error.value = null;
    try {
      const response = await eventApi.getBeverages();
      beverages.value = response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to fetch beverages';
      console.error('Error fetching beverages:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function createEvent(data: CreateEventDto) {
    loading.value = true;
    error.value = null;
    try {
      const response = await eventApi.createEvent(data);
      events.value.push({
        ...response.data,
        beverages: response.data.beverages || [],
        bars: response.data.bars || [],
        dropPoints: response.data.dropPoints || [],
      });
      return response.data;
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Failed to create event';
      console.error('Error creating event:', err);
      throw err;
    } finally {
      loading.value = false;
    }
  }

  async function updateEvent(id: number, data: UpdateEventDto) {
    loading.value = true;
    error.value = null;
    try {
      const response = await eventApi.updateEvent(id, data);
      const index = events.value.findIndex(event => event.eventId === id);
      if (index !== -1) {
        events.value[index] = {
          ...response.data,
          beverages: response.data.beverages || [],
          bars: response.data.bars || [],
          dropPoints: response.data.dropPoints || [],
        };
      }
      if (currentEvent.value?.eventId === id) {
        currentEvent.value = {
          ...response.data,
          beverages: response.data.beverages || [],
          bars: response.data.bars || [],
          dropPoints: response.data.dropPoints || [],
        };
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
      events.value = events.value.filter(event => event.eventId !== id);
      if (currentEvent.value?.eventId === id) {
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

  function clearError() {
    error.value = null;
  }

  function clearCurrentEvent() {
    currentEvent.value = null;
  }

  return {
    // State
    events,
    currentEvent,
    beverages,
    loading,
    error,
    // Getters
    eventCount,
    plannedEvents,
    ongoingEvents,
    completedEvents,
    upcomingEvents,
    // Actions
    fetchEvents,
    fetchEvent,
    fetchBeverages,
    createEvent,
    updateEvent,
    deleteEvent,
    clearError,
    clearCurrentEvent,
  };
});
