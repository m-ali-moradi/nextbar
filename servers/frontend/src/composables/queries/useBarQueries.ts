import { computed, type MaybeRef, toValue } from 'vue';
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query';
import { barApi } from '@/api/barApi';
import { queryKeys } from './queryKeys';
import type {
  Bar,
  StockItem,
  BarUsageLog,
  TotalServed,
  SupplyRequest,
  CreateBarPayload,
  StockOperationPayload,
  CreateSupplyRequestPayload,
  UpdateSupplyRequestPayload,
} from '@/api/types';

// ─── Result type for the composite bar-details fetch ─────────────
export interface BarDetails {
  bar: Bar;
  stock: StockItem[];
  usageLogs: BarUsageLog[];
  totalServed: TotalServed[];
  supplyRequests: SupplyRequest[];
}

const getBarDetailsData = async (barId: string): Promise<BarDetails> => {
  const [bar, stock, usageLogs, totalServed, supplyRequests] = await Promise.all([
    barApi.getBar(barId),
    barApi.getStock(barId),
    barApi.getUsageLogs(barId),
    barApi.getTotalServed(barId),
    barApi.getSupplyRequests(barId),
  ]);

  return {
    bar: bar.data,
    stock: stock.data,
    usageLogs: usageLogs.data,
    totalServed: totalServed.data,
    supplyRequests: supplyRequests.data,
  };
};

// ═══════════════════════════════════════════════════════════════════
// Queries
// ═══════════════════════════════════════════════════════════════════

/** Fetch the list of all bars. */
export function useBarsQuery() {
  return useQuery({
    queryKey: queryKeys.bars.all,
    queryFn: async () => {
      const { data } = await barApi.getBars();
      return data;
    },
  });
}

/** Fetch the composite bar-details (bar + stock + logs + totalServed + supply). */
export function useBarDetailsQuery(barId: MaybeRef<string>) {
  return useQuery({
    queryKey: computed(() => queryKeys.bars.details(toValue(barId))),
    queryFn: async (): Promise<BarDetails> => getBarDetailsData(toValue(barId)),
    enabled: computed(() => !!toValue(barId)),
  });
}

/** Fetch the global product catalog. */
export function useProductsQuery() {
  return useQuery({
    queryKey: queryKeys.bars.products,
    queryFn: async () => {
      const { data } = await barApi.getProducts();
      return data;
    },
    staleTime: 5 * 60 * 1000, // products rarely change
  });
}

// ═══════════════════════════════════════════════════════════════════
// Mutations
// ═══════════════════════════════════════════════════════════════════

/** Create a new local bar. Invalidates the bars list. */
export function useCreateBar() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: CreateBarPayload) =>
      barApi.addBar(payload).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: queryKeys.bars.all });
    },
  });
}

/** Add stock to a bar. Invalidates bar details. */
export function useAddStock() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ barId, payload }: { barId: string; payload: StockOperationPayload }) =>
      barApi.addStock(barId, payload).then((r) => r.data),
    onSuccess: (_data, { barId }) => {
      qc.invalidateQueries({ queryKey: queryKeys.bars.details(barId) });
    },
  });
}

/**
 * Reduce stock **and** log the drink sale.
 * Invalidates bar details + warehouse supply (the bar created a need).
 */
export function useReduceStock() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ barId, payload }: { barId: string; payload: StockOperationPayload }) => {
      const { data } = await barApi.reduceStock(barId, payload);
      await barApi.logDrink(barId, payload);
      return data;
    },
    onSuccess: (_data, { barId }) => {
      qc.invalidateQueries({ queryKey: queryKeys.bars.details(barId) });
    },
  });
}

/** Create a supply request. Invalidates bar details + warehouse supply list. */
export function useCreateSupplyRequest() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ barId, payload }: { barId: string; payload: CreateSupplyRequestPayload }) =>
      barApi.createSupplyRequest(barId, payload).then((r) => r.data),
    onSuccess: (_data, { barId }) => {
      qc.invalidateQueries({ queryKey: queryKeys.bars.details(barId) });
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.supply });
    },
  });
}

/** Update a supply request status. Invalidates bar details. */
export function useUpdateSupplyRequest() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({
      barId,
      requestId,
      payload,
    }: {
      barId: string;
      requestId: string;
      payload: UpdateSupplyRequestPayload;
    }) => barApi.updateSupplyRequest(barId, requestId, payload).then((r) => r.data),
    onSuccess: (_data, { barId }) => {
      qc.invalidateQueries({ queryKey: queryKeys.bars.details(barId) });
    },
  });
}

/** Cancel a supply request. Invalidates bar details + warehouse supply list. */
export function useCancelSupplyRequest() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ barId, requestId }: { barId: string; requestId: string }) =>
      barApi.cancelSupplyRequest(barId, requestId),
    onSuccess: (_data, { barId }) => {
      qc.invalidateQueries({ queryKey: queryKeys.bars.details(barId) });
      qc.invalidateQueries({ queryKey: queryKeys.warehouse.supply });
    },
  });
}
