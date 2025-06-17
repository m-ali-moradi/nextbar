// stores/droppoint.js
import { defineStore } from 'pinia'
import axios from 'axios'

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
        const response = await axios.get('http://localhost:8083/droppoints')
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
        const response = await axios.post('http://localhost:8083/droppoints', newInventory)

        this.droppoints.push(response.data)
      } catch (err) {
        this.error = 'Failed to add drop point.'
        console.error(err)
      } finally {
        this.isLoading = false
      }
    },
    async updateDropPoint(inventoryId, updatedInventory) {
      this.isLoading = true
      this.error = null
      try {
        const response = await axios.put(
          `http://localhost:8083/droppoints/${inventoryId}`,
          updatedInventory,
        )

        const index = this.droppoints.findIndex((i) => i.id === inventoryId)
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
        await axios.delete(`http://localhost:8083/droppoints/${dropPointId}`)
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
