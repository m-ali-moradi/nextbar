import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query';
import { warehouseApi } from '@/api/warehouseApi';
import { barApi } from '@/api/barApi';
import { queryKeys } from './queryKeys';
import type {
  ReplenishStockPayload,
  FulfillSupplyRequestPayload,
} from '@/api/types';

// ═══════════════════════════════════════════════════════════════════
// Queries
// ═══════════════════════════════════════════════════════════════════

/** Fetch full warehouse stock list. */
export function useWarehouseStockQuery() {
  return useQuery({
    queryKey: queryKeys.warehouse.stock,
    queryFn: async () => {
      const { data } = await warehouseApi.getStock();
      return data.map((item) => ({
        ...item,
        productName: item.productName || item.beverageType,
      }));
    },
  });
}

/** Fetch all supply requests across all bars. */
export function useSupplyRequestsQuery() {
  return useQuery({
    queryKey: queryKeys.warehouse.supply,
    queryFn: async () => {
      const { data } = await warehouseApi.getAllSupplyRequests();
      return data;
    },
  });
}

/** Fetch bars list (from warehouse perspective, for supply-request filtering). */
export function useWarehouseBarsQuery() {
  return useQuery({
    queryKey: queryKeys.warehouse.bars,
    queryFn: async () => {
      const { data } = await barApi.getBars();
      return data;
    },
    staleTime: 2 * 60 * 1000,
  });
}

/** Fetch all collections. */
export function useCollectionsQuery() {
  return useQuery({
    queryKey: queryKeys.warehouse.collections,
    queryFn: async () => {
      const { data } = await warehouseApi.getAllCollections();
      return data;
    },
  });
}

/** Fetch empty-bottle inventory summary. */
export function useCollectionInventoryQuery() {
  return useQuery({
    queryKey: queryKeys.warehouse.collectionInventory,
    queryFn: async () => {
      const { data } = await warehouseApi.getInventorySummary();
      return data.map((item) => ({
        ...item,
        location: item.location || item.dropPointLocation,
        emptiesCount: item.emptiesCount || item.totalBottlesCollected,
        collectionDate: item.collectionDate || item.lastCollectionAt,
      }));
    },
  });
}

// ═══════════════════════════════════════════════════════════════════
// Stock Mutations
// ═══════════════════════════════════════════════════════════════════

/** Create a new stock entry. */
export function useCreateStock() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: ReplenishStockPayload) =>
      warehouseApi.createStock(payload).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.stock });
    },
  });
}

/** Update a stock item's quantity. */
export function useUpdateStockQuantity() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ beverageType, quantity }: { beverageType: string; quantity: number }) =>
      warehouseApi.updateStockQuantity(beverageType, { quantity }).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.stock });
    },
  });
}

/** Delete a stock item. */
export function useDeleteStockItem() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => warehouseApi.deleteStock(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.stock });
    },
  });
}

// ═══════════════════════════════════════════════════════════════════
// Supply-Request Mutations
// ═══════════════════════════════════════════════════════════════════

/** Fulfill (start / deliver) a supply request. */
export function useFulfillSupplyRequest() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      barId,
      requestId,
      payload,
    }: {
      barId: string | number;
      requestId: string | number;
      payload: FulfillSupplyRequestPayload;
    }) => warehouseApi.fulfillSupplyRequest(barId, requestId, payload).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.supply });
    },
  });
}

/** Reject a supply request. */
export function useRejectSupplyRequest() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      barId,
      requestId,
      reason,
    }: {
      barId: string | number;
      requestId: string | number;
      reason: string;
    }) => warehouseApi.rejectSupplyRequest(barId, requestId, { reason }).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.supply });
    },
  });
}

// ═══════════════════════════════════════════════════════════════════
// Collection Mutations
// ═══════════════════════════════════════════════════════════════════

/** Sync drop-point notifications into collection records. */
export function useSyncCollections() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => warehouseApi.syncCollections().then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.collections });
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.collectionInventory });
    },
  });
}

/** Accept a pending collection. */
export function useAcceptCollection() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => warehouseApi.acceptCollection(id).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.collections });
    },
  });
}

/** Complete an accepted collection. */
export function useCompleteCollection() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => warehouseApi.completeCollection(id).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.collections });
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.collectionInventory });
    },
  });
}
