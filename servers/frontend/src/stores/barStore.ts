import { defineStore } from 'pinia';
import { getBarDetailsData, barApi, ApiErrorResponse } from '../api';
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
} from '../api/types';

interface BarState {
  bars: Bar[];
  currentBar: Bar | null;
  stock: StockItem[];
  usageLogs: BarUsageLog[];
  totalServed: TotalServed[];
  supplyRequests: SupplyRequest[];
  loading: boolean;
  error: { message: string; code?: string } | null;
}

export const useBarStore = defineStore('bar', {
  state: (): BarState => ({
    bars: [],
    currentBar: null,
    stock: [],
    usageLogs: [],
    totalServed: [],
    supplyRequests: [],
    loading: false,
    error: null,
  }),

  actions: {
    /**
     * Handle API errors in a consistent way
     */
    handleError(error: unknown) {
      if (error instanceof ApiErrorResponse) {
        this.error = {
          message: error.message,
          code: error.code,
        };
      } else if (error instanceof Error) {
        this.error = { message: error.message };
      } else {
        this.error = { message: 'An unexpected error occurred' };
      }
    },

    /**
     * Clear current error state
     */
    clearError() {
      this.error = null;
    },

    /**
     * Fetch all bars
     */
    async fetchBars() {
      this.loading = true;
      this.clearError();
      try {
        const response = await barApi.getBars();
        this.bars = response.data;
      } catch (error) {
        this.handleError(error);
      } finally {
        this.loading = false;
      }
    },

    /**
     * Fetch detailed data for a specific bar
     */
    async fetchBarDetails(barId: string, options: { silent?: boolean } = {}) {
      if (!options.silent) {
        this.loading = true;
      }
      this.clearError();
      try {
        const data = await getBarDetailsData(barId);
        this.currentBar = data.bar;
        this.stock = data.stock;
        this.usageLogs = data.usageLogs;
        this.totalServed = data.totalServed;
        this.supplyRequests = data.supplyRequests;
      } catch (error) {
        this.handleError(error);
      } finally {
        if (!options.silent) {
          this.loading = false;
        }
      }
    },

    /**
     * Create a new bar
     * @param payload - Bar creation data
     */
    async addBar(payload: CreateBarPayload) {
      this.clearError();
      try {
        await barApi.addBar(payload);
        await this.fetchBars();
      } catch (error) {
        this.handleError(error);
        throw error;
      }
    },

    /**
     * Delete a bar
     */
    async deleteBar(barId: string) {
      this.clearError();
      try {
        await barApi.deleteBar(barId);
        await this.fetchBars();
      } catch (error) {
        this.handleError(error);
        throw error;
      }
    },

    /**
     * Add stock to a bar
     * @param barId - Bar ID
     * @param payload - Stock operation data
     */
    async addStock(barId: string, payload: StockOperationPayload) {
      this.clearError();
      try {
        await barApi.addStock(barId, payload);
        await this.fetchBarDetails(barId, { silent: true });
      } catch (error) {
        this.handleError(error);
        throw error;
      }
    },

    /**
     * Reduce stock (sell) from a bar and log the usage
     * @param barId - Bar ID
     * @param payload - Stock operation data
     */
    async reduceStock(barId: string, payload: StockOperationPayload) {
      this.clearError();
      try {
        await barApi.reduceStock(barId, payload);
        await barApi.logDrink(barId, payload);
        await this.fetchBarDetails(barId, { silent: true });
      } catch (error) {
        this.handleError(error);
        throw error;
      }
    },

    /**
     * Create a supply request for a bar
     * @param barId - Bar ID
     * @param payload - Supply request items
     */
    async createSupplyRequest(barId: string, payload: CreateSupplyRequestPayload) {
      this.clearError();
      try {
        await barApi.createSupplyRequest(barId, payload);
        await this.fetchBarDetails(barId, { silent: true });
      } catch (error) {
        this.handleError(error);
        throw error;
      }
    },

    /**
     * Update supply request status
     * @param barId - Bar ID
     * @param requestId - Supply request ID
     * @param payload - Status update
     */
    async updateSupplyRequest(
      barId: string,
      requestId: string,
      payload: UpdateSupplyRequestPayload
    ) {
      this.clearError();
      try {
        await barApi.updateSupplyRequest(barId, requestId, payload);
        await this.fetchBarDetails(barId, { silent: true });
      } catch (error) {
        this.handleError(error);
        throw error;
      }
    },

    /**
     * Cancel a pending supply request
     */
    async cancelSupplyRequest(barId: string, requestId: string) {
      this.clearError();
      try {
        await barApi.cancelSupplyRequest(barId, requestId);
        await this.fetchBarDetails(barId, { silent: true });
      } catch (error) {
        this.handleError(error);
        throw error;
      }
    },
  },

  persist: true,
});