/**
 * Centralized query key factory for Vue Query.
 * Structured as a tree so invalidation can target broad or specific scopes.
 *
 * Usage:
 *   queryKeys.bars.all          → ['bars']
 *   queryKeys.bars.details('1') → ['bar-details', '1']
 */
export const queryKeys = {
  bars: {
    all: ['bars'] as const,
    details: (barId: string) => ['bar-details', barId] as const,
    products: ['bars', 'products'] as const,
  },

  droppoints: {
    all: ['droppoints'] as const,
    detail: (id: number) => ['droppoints', id] as const,
  },

  events: {
    all: ['events'] as const,
    detail: (id: number) => ['events', id] as const,
  },

  warehouse: {
    stock: ['warehouse', 'stock'] as const,
    supply: ['warehouse', 'supply'] as const,
    bars: ['warehouse', 'bars'] as const,
    collections: ['warehouse', 'collections'] as const,
    collectionInventory: ['warehouse', 'collections', 'inventory'] as const,
  },
} as const;
