import { computed, type MaybeRef, toValue } from 'vue';
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query';
import { eventApi } from '@/api/eventApi';
import { useAuthStore } from '@/stores/authStore';
import { queryKeys } from './queryKeys';
import type {
  CreateEventRequest,
  UpdateEventRequest,
} from '@/api/types';

// ═══════════════════════════════════════════════════════════════════
// Queries
// ═══════════════════════════════════════════════════════════════════

/** Fetch the summary list of all events. */
export function useEventsQuery() {
  return useQuery({
    queryKey: queryKeys.events.all,
    queryFn: async () => {
      const { data } = await eventApi.getEvents();
      return data;
    },
  });
}

/** Fetch full event details (includes nested bars & dropPoints). */
export function useEventQuery(id: MaybeRef<number>) {
  return useQuery({
    queryKey: computed(() => queryKeys.events.detail(toValue(id))),
    queryFn: async () => {
      const { data } = await eventApi.getEvent(toValue(id));
      return data;
    },
    enabled: computed(() => !!toValue(id)),
  });
}

/** Fetch available staff for bar assignment. */
export function useAvailableStaffQuery(enabled: MaybeRef<boolean> = true) {
  return useQuery({
    queryKey: ['events', 'staff', 'available'] as const,
    queryFn: async () => {
      const { data } = await eventApi.getAvailableBarStaff();
      return data;
    },
    enabled: computed(() => toValue(enabled)),
    staleTime: 2 * 60 * 1000,
  });
}

/** Fetch warehouse inventory available for event stock allocation. */
export function useWarehouseInventoryForEventsQuery(enabled: MaybeRef<boolean> = true) {
  return useQuery({
    queryKey: ['events', 'warehouse', 'inventory'] as const,
    queryFn: async () => {
      const { data } = await eventApi.getWarehouseInventory();
      return data;
    },
    enabled: computed(() => toValue(enabled)),
    staleTime: 60 * 1000,
  });
}

// ═══════════════════════════════════════════════════════════════════
// Event Lifecycle Mutations
// ═══════════════════════════════════════════════════════════════════

/** Create a new event. */
export function useCreateEvent() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateEventRequest) =>
      eventApi.createEvent(data).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.events.all });
    },
  });
}

/** Update an existing event. */
export function useUpdateEvent() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateEventRequest }) =>
      eventApi.updateEvent(id, data).then((r) => r.data),
    onSuccess: (_data, { id }) => {
      qc.invalidateQueries({ queryKey: queryKeys.events.all });
      qc.invalidateQueries({ queryKey: queryKeys.events.detail(id) });
    },
  });
}

/** Delete an event. */
export function useDeleteEvent() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => eventApi.deleteEvent(id),
    onSuccess: (_data, id) => {
      qc.invalidateQueries({ queryKey: queryKeys.events.all });
      qc.removeQueries({ queryKey: queryKeys.events.detail(id) });
    },
  });
}

/** Start an event (SCHEDULED → RUNNING). */
export function useStartEvent() {
  const qc = useQueryClient();
  const authStore = useAuthStore();
  return useMutation({
    mutationFn: (id: number) => eventApi.startEvent(id).then((r) => r.data),
    onSuccess: async (_data, id) => {
      qc.invalidateQueries({ queryKey: queryKeys.events.all });
      qc.invalidateQueries({ queryKey: queryKeys.events.detail(id) });
      await authStore.refreshSession();
    },
  });
}

/** Complete an event (RUNNING → COMPLETED). */
export function useCompleteEvent() {
  const qc = useQueryClient();
  const authStore = useAuthStore();
  return useMutation({
    mutationFn: (id: number) => eventApi.completeEvent(id).then((r) => r.data),
    onSuccess: async (_data, id) => {
      qc.invalidateQueries({ queryKey: queryKeys.events.all });
      qc.invalidateQueries({ queryKey: queryKeys.events.detail(id) });
      await authStore.refreshSession();
    },
  });
}

/** Cancel an event. */
export function useCancelEvent() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => eventApi.cancelEvent(id).then((r) => r.data),
    onSuccess: (_data, id) => {
      qc.invalidateQueries({ queryKey: queryKeys.events.all });
      qc.invalidateQueries({ queryKey: queryKeys.events.detail(id) });
    },
  });
}

// ═══════════════════════════════════════════════════════════════════
// Event-Bar Mutations (used inside EventDetailsView)
// ═══════════════════════════════════════════════════════════════════

/** Delete a bar from an event. Caller should invalidate the parent event detail. */
export function useDeleteEventBar() {
  return useMutation({
    mutationFn: (barId: number) => eventApi.deleteBar(barId),
  });
}

/** Delete a drop point from an event. Caller should invalidate the parent event detail. */
export function useDeleteEventDropPoint() {
  return useMutation({
    mutationFn: (dpId: number) => eventApi.deleteDropPoint(dpId),
  });
}
