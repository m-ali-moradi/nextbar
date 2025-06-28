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
  
  /**  Only loads whatever is already in your DB */
//   async function loadNotified() {
//     try {
//       const { data: notified } = await axios.get<Record[]>(
//         'http://localhost:8085/warehouse/droppoints/notified'
//       )
//       // rebuild the records list, preserving ACCEPTED
//       const accepted = records.value.filter(r => r.status === 'ACCEPTED')
//       records.value = [...accepted, ...notified]
//     }
//     catch (e) {
//       console.error('Load failed', e)
//     }
//   }
  
//   /** Fires when you click the button */
//   async function refresh() {
//     try {
//       // 1) pull in any brand-new notifications
//       await axios.post(
//         'http://localhost:8085/warehouse/droppoints/fetch-notified'
//       )
//       // 2) then re-load from your DB
//       await loadNotified()
//     }
//     catch (e) {
//       console.error('Refresh failed', e)
//     }
//   }

/** Pull in new notifications *and* then overwrite entire table with exactly what’s in the DB */
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
  .button{
    padding: 4px 8px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.85rem;

   /* Smooth transform & shadow transitions */
   transition: transform 0.1s ease, box-shadow 0.1s ease;
  }
 
 /* “Pressed” state */
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
  