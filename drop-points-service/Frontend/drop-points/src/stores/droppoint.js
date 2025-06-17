// stores/droppoint.js
import { defineStore } from 'pinia'
import axios from 'axios'

// Base URL for API endpoints (gateway server)
const API_BASE_URL = "http://localhost:8080/api";

// Axios instance with base URL
const api = axios.create({
  baseURL: API_BASE_URL,
});

export const useDroppointStore = defineStore('droppoint', {
  state: () => ({
    droppoints: [],
    isLoading: false,
    error: null,
  }),
  actions: {
    async fetchDropPoints() {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.get('/droppoints')
        this.droppoints = response.data
      } catch (err) {
        this.error = 'Failed to fetch drop points.'
        console.error(err)
      } finally {
        this.isLoading = false
      }
    },
    async addDropPoint(newInventory) {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.post('/droppoints', newInventory)

        this.droppoints.push(response.data)
      } catch (err) {
        this.error = 'Failed to add drop point.'
        console.error(err)
      } finally {
        this.isLoading = false
      }
    },
    async updateDropPoint(dropPointId, updatedInventory) {
      this.isLoading = true
      this.error = null
      try {
        const response = await api.put(
          `/droppoints/${dropPointId}`,
          updatedInventory,
        )

        const index = this.droppoints.findIndex((i) => i.id === dropPointId)
        if (index !== -1) {
          this.droppoints[index] = response.data
        }
      } catch (err) {
        this.error = 'Failed to update drop point.'
        console.error(err)
      } finally {
        this.isLoading = false
      }
    },
    async deleteDropPoint(dropPointId) {
      this.isLoading = true
      this.error = null
      try {
        await api.delete(`/droppoints/${dropPointId}`)
        // Remove the order from the droppoints array
        this.droppoints = this.droppoints.filter((u) => u.id !== dropPointId)
      } catch (err) {
        this.error = 'Failed to delete drop point.'
        console.error(err)
      } finally {
        this.isLoading = false
      }
    },
  },
  persist: {
    enabled: false,
    strategies: [
      {
        key: 'dropPointStore',
        storage: sessionStorage, // sessionStorage is used to persist data
      },
    ],
  },
})
