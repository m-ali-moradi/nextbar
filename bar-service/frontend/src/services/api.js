import axios from "axios";

// Base URL for API endpoints (backend server)
const API_BASE_URL = "http://localhost:8080/api";

// Axios instance with base URL
const api = axios.create({
  baseURL: API_BASE_URL,
});

// API methods for bar service at coditects
export default {
  // Get all bars
  async getBars() {
    const response = await api.get("/bars");
    console.log("Fetched bars:", response.data);
    return response.data;
  },

  // Add a new bar
  async addBar(name, location) {
    const response = await api.post("/bars", null, {
      params: { name, location },
    });
    return response.data;
  },

  // Get specific bar details by ID
  async getBar(barId) {
    const response = await api.get(`/bars/${barId}`);
    return response.data;
  },

  // Get bar's stock for a specific bar
  async getStock(barId) {
    const response = await api.get(`/bars/${barId}/stock`);
    return response.data;
  },

  // Reduce bar's stock (serving drinks)
  async reduceStock(barId, productId, quantity) {
    await api.post(`/bars/${barId}/stock/reduce`, null, {
      params: { productId, quantity },
    });
  },

  // Log drink usage
  async logDrink(barId, productId, quantity) {
    await api.post(`/bars/${barId}/usage`, null, {
      params: { productId, quantity },
    });
  },

  // Get usage logs for specific bar, by bar ID
  async getUsageLogs(barId) {
    const response = await api.get(`/bars/${barId}/usage`);
    return response.data;
  },

  // Get total drinks served for a specific bar, by bar ID
  async getTotalServed(barId) {
    const response = await fetch(
      `http://localhost:8080/api/bars/${barId}/usage/total-served`,
      {
        cache: "no-store", // Force no caching to avoid 304
      }
    );
    if (!response.ok) {
      throw new Error(`Failed to fetch total served: ${response.statusText}`);
    }
    const data = await response.json();
    console.log("API Response:", data); // Debug log
    return data;
  },

  // Create supply request for a specific bar. items is an array of objects with productId and quantity
  async createSupplyRequest(barId, items) {
    const response = await api.post(`/bars/${barId}/supply`, items);
    return response.data;
  },

  // Get supply requests for a specific bar
  async getSupplyRequests(barId) {
    const response = await api.get(`/bars/${barId}/supply`);
    return response.data;
  },

  // Cancel supply request by bar ID and request ID if request status is still REQUESTED
  async cancelSupplyRequest(barId, requestId) {
    await api.delete(`/bars/${barId}/supply/${requestId}`);
  },
};
