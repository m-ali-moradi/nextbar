<template>
    <div>
      <h2>Drop-Point Notifications</h2>
      <button class="button process" @click="refresh">
        Refresh Drop-Points
      </button>
  
      <div class="table-container" v-if="records.length">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Location</th>
              <th>Empties</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in records" :key="r.id">
              <td>{{ r.id }}</td>
              <td>{{ r.location }}</td>
              <td>{{ r.currentEmpties }}</td>
              <td>{{ r.status }}</td>
              <td>
                <button
                  v-if="r.status === 'NOTIFIED'"
                  @click="accept(r.id)"
                  class="button deliver"
                >Accept</button>
                <span v-else>—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
  
      <p v-else>No drop-points awaiting acceptance.</p>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import axios from 'axios'
  
  interface Record {
    id: number
    location: string
    currentEmpties: number
    status: 'NOTIFIED' | 'ACCEPTED'
  }
  
  const records = ref<Record[]>([])
  
  /**
   * 1) Tell your warehouse to pull in any new notifications
   * 2) Read back exactly the NOTIFIED ones
   * 3) Merge into `records` so ACCEPTED rows stick around
   */
  async function refresh() {
    try {
      // 🔵 1) trigger backend to fetch & save all new FULL notifications
      await axios.post('http://localhost:8085/warehouse/droppoints/fetch-notified')
  
      // 🔵 2) read back only the NOTIFIED ones
      const { data: notified } = await axios.get<Record[]>(
        'http://localhost:8085/warehouse/droppoints/notified'
      )
  
      // 🔵 3) merge them into `records`
      notified.forEach((r) => {
        const existing = records.value.find(x => x.id === r.id)
        if (!existing) {
          // brand new notification → add it
          records.value.push(r)
        } else if (existing.status === 'NOTIFIED') {
          // update location & count if it's still NOTIFIED
          existing.location = r.location
          existing.currentEmpties = r.currentEmpties
        }
        // if existing.status==='ACCEPTED' we leave it alone
      })
    }
    catch (e) {
      console.error('Refresh failed', e)
    }
  }
  
  /** Flip one from NOTIFIED → ACCEPTED in both backend + local UI */
  async function accept(id: number) {
    try {
      await axios.put(`http://localhost:8085/warehouse/droppoints/${id}/accept`)
      const rec = records.value.find(r => r.id === id)
      if (rec) {
        rec.status = 'ACCEPTED'
        rec.currentEmpties = 0
      }
    }
    catch (e) {
      console.error(`Accept failed for ${id}`, e)
    }
  }
  
  onMounted(refresh)
  </script>
  
  <style scoped>
  .table-container {
    margin-top: 1rem;
    overflow-x: auto;
  }
  table {
    width: 100%;
    border-collapse: collapse;
  }
  th, td {
    padding: 8px 12px;
    border: 1px solid #ddd;
  }
  .button {
    padding: 4px 8px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.85rem;
  }
  .button.process { background: #3182ce; color: white; }
  .button.deliver { background: #38a169; color: white; }
  </style>
  