<template>
    <div class="summary">
      <div class="card">
        <h3>Total Items</h3>
        <div class="value">{{ totalSKUs }}</div>
      </div>
      <div class="card">
        <h3>Pending Requests</h3>
        <div class="value">{{ pendingRequestsLabel }}</div>
      </div>
      <div class="card">
        <h3>Completed Requests</h3>
        <div class="value">{{ completedRequestsLabel }}</div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted, watch, computed } from 'vue'
  import axios from 'axios'
  
  /** 
   * If you pass barId in, summary will fetch supply counts for that bar.
   * Otherwise it will leave them blank.
   */
  const props = defineProps<{
    barId?: string
  }>()
  
  const totalSKUs         = ref(0)
  const pendingRequests   = ref(0)
  const completedRequests = ref(0)
  
  // Labels: show “–” when no barId yet
  const pendingRequestsLabel   = computed(() => props.barId ? pendingRequests.value : '–')
  const completedRequestsLabel = computed(() => props.barId ? completedRequests.value : '–')
  
  onMounted(async () => {
    try {
      const { data: stock } = await axios.get<any[]>('http://localhost:8085/warehouse/stock')
      totalSKUs.value = stock.length
    } catch (e) {
      console.error('Failed to load items:', e)
      totalSKUs.value = 0
    }
  })
  
  // When barId changes (and is non-empty), fetch its supply
  watch(
    () => props.barId,
    async (barId) => {
      if (!barId) {
        pendingRequests.value   = 0
        completedRequests.value = 0
        return
      }
      try {
        const { data: reqs } = await axios.get<any[]>(
          `http://localhost:8085/warehouse/bars/${barId}/supply`
        )
        pendingRequests.value   = reqs.filter(r => r.status === 'REQUESTED').length
        completedRequests.value = reqs.filter(r => r.status === 'DELIVERED').length
      } catch (e) {
        // If the bar has no supply endpoint yet, just set zeros
        pendingRequests.value   = 0
        completedRequests.value = 0
      }
    },
    { immediate: true }
  )
  </script>
  
  <style scoped>
  .summary {
    display: flex;
    gap: 16px;
    margin-bottom: 24px;
  }
  .card {
    flex: 1;
    background: #fff;
    border: 1px solid #ddd;
    border-radius: 6px;
    padding: 16px;
  }
  .card h3 {
    font-size: 0.9rem;
    color: #666;
    margin-bottom: 8px;
  }
  .card .value {
    font-size: 1.8rem;
    font-weight: bold;
  }
  </style>  