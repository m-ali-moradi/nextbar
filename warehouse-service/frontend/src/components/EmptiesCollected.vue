<template>
    <div>
      <h2>Empties Collected</h2>
      <button class="button process" @click="load()">Refresh</button>
  
      <div class="table-container" v-if="records.length">
        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>Drop-Point ID</th>
              <th>Location</th>
              <th>Quantity</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in records" :key="r.id">
              <td>{{ r.id }}</td>
              <td>{{ r.dropPointId }}</td>
              <td>{{ r.dropPointLocation }}</td>
              <td>{{ r.quantity }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <p v-else>No empties recorded yet.</p>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import axios from 'axios'
  
  interface EmptiesRecord {
    id:                 number
    dropPointId:        number
    dropPointLocation:  string
    quantity:           number
  }
  
  const records = ref<EmptiesRecord[]>([])
  
  async function load() {
    try {
      const { data } = await axios.get<EmptiesRecord[]>(
        'http://localhost:8085/warehouse/empties'
      )
      records.value = data
    }
    catch(e) {
      console.error('Could not load empties:', e)
    }
  }
  
  onMounted(load)
  </script>
  
  <style scoped>
  .table-container { margin-top:1rem; overflow-x:auto; }
  table { width:100%; border-collapse:collapse }
  th,td { padding:8px 12px; border:1px solid #ddd }
  .button { padding:4px 8px; border:none; border-radius:4px; cursor:pointer; background:#3182ce; color:#fff }
  .button:hover { background:#2b6cb0 }
  </style>
  