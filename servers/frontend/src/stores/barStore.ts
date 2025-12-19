import { defineStore } from 'pinia';
import { getBarDetailsData, barApi } from '../api';

interface BarState {
  bars: any[];
  currentBar: any | null;
  stock: any[];
  usageLogs: any[];
  totalServed: any[];
  supplyRequests: any[];
  loading: boolean;
  error: string | null;
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
    async fetchBars() {
      this.loading = true;
      try {
        const response = await barApi.getBars();
        this.bars = response.data;
        this.error = null;
      } catch (error) {
        this.error = (error as Error).message;
      } finally {
        this.loading = false;
      }
    },
    async fetchBarDetails(barId: string) {
      this.loading = true;
      try {
    const data = await getBarDetailsData(barId);
    console.log('Bar details data:', data); // Debug log
    this.currentBar = data.bar?.data ?? data.bar;
    this.stock = data.stock?.data ?? data.stock;
    this.usageLogs = data.usageLogs?.data ?? data.usageLogs;
    this.totalServed = data.totalServed?.data ?? data.totalServed;
    this.supplyRequests = data.supplyRequests?.data ?? data.supplyRequests;
    this.error = null;
      } catch (error) {
        this.error = (error as Error).message;
      } finally {
        this.loading = false;
      }
    },
    async addBar(name: string, location: string, maxCapacity: number) {
      try {
        await barApi.addBar(name, location, maxCapacity);
        await this.fetchBars();
      } catch (error) {
        this.error = (error as Error).message;
      }
    },
    async deleteBar(barId: string) {
      try {
        await barApi.deleteBar(barId);
        await this.fetchBars();
      } catch (error) {
        this.error = (error as Error).message;
      }
    },
    async addStock(barId: string, productId: string, quantity: number) {
      try {
        await barApi.addStock(barId, productId, quantity);
        await this.fetchBarDetails(barId);
      } catch (error) {
        this.error = (error as Error).message;
      }
    },
    async reduceStock(barId: string, productId: string, quantity: number) {
      try {
        await barApi.reduceStock(barId, productId, quantity);
        await barApi.logDrink(barId, productId, quantity);
        await this.fetchBarDetails(barId);
      } catch (error) {
        this.error = (error as Error).message;
      }
    },
    async createSupplyRequest(barId: string, items: Array<{ productId: string, quantity: number }>) {
      try {
        await barApi.createSupplyRequest(barId, items);
        await this.fetchBarDetails(barId);
      } catch (error) {
        this.error = (error as Error).message;
      }
    },
    async updateSupplyRequest(barId: string, requestId: string, status: string) {
      try {
        await barApi.updateSupplyRequest(barId, requestId, status);
        await this.fetchBarDetails(barId);
      } catch (error) {
        this.error = (error as Error).message;
      }
    },
    async cancelSupplyRequest(barId: string, requestId: string) {
      try {
        await barApi.cancelSupplyRequest(barId, requestId);
        await this.fetchBarDetails(barId);
      } catch (error) {
        this.error = (error as Error).message;
      }
    },
  },
  persist: true,
});