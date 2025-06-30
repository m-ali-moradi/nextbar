<template>
    <div>
      <h2>Drop-Point Notifications</h2>
      <button class="button process" @click="refresh()">
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
  
  async function refresh() {
  // 1) fetch from drop-point microservice
  await axios.post(
    'http://localhost:8085/warehouse/droppoints/fetch-notified'
  )

  // 2) read *all* records (NOTIFIED + ACCEPTED) from your own DB
  const { data: all } = await axios.get<Record[]>(
    'http://localhost:8085/warehouse/droppoints/all'
  )

  // 3) replace the UI list wholesale
  records.value = all
}

  
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
  .button{
    padding: 4px 8px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.85rem;

   transition: transform 0.1s ease, box-shadow 0.1s ease;
  }
 
.button:active {
   transform: scale(0.95);
    box-shadow: inset 0 2px 4px rgba(0,0,0,0.2);
     }
.button:hover { 
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  }

  .button.process { background: #3182ce; color: white; }
  .button.deliver { background: #38a169; color: white; }
  </style>
  