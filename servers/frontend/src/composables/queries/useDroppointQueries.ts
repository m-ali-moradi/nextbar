import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query';
import { droppointApi } from '@/api/droppointApi';
import { queryKeys } from './queryKeys';

// ═══════════════════════════════════════════════════════════════════
// Queries
// ═══════════════════════════════════════════════════════════════════

/** Fetch all drop points. */
export function useDroppointsQuery() {
  return useQuery({
    queryKey: queryKeys.droppoints.all,
    queryFn: async () => {
      const { data } = await droppointApi.getDropPoints();
      return data;
    },
  });
}

// ═══════════════════════════════════════════════════════════════════
// Mutations
// ═══════════════════════════════════════════════════════════════════

/** Add empties to a drop point. Invalidates the list. */
export function useAddEmpties() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => droppointApi.addEmpties(id).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.droppoints.all });
    },
  });
}

/** Notify warehouse that a drop point is full. Invalidates droppoints + collections. */
export function useNotifyWarehouse() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => droppointApi.notifyWarehouse(id).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.droppoints.all });
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.collections });
    },
  });
}

/** Verify that empties were transferred. Invalidates the list. */
export function useVerifyTransfer() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => droppointApi.verifyTransfer(id).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.droppoints.all });
    },
  });
}
