<!-- 

<template>
  <div>
    <h2>Beverage Stock</h2>
    <table>
      <thead>
        <tr>
          <th>Beverage</th>
          <th>Quantity</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in stock" :key="item.id">
          <td>{{ item.beverageType }}</td>
          <td>{{ item.quantity }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

  
  <script setup>
  import { ref, onMounted } from 'vue'
  const stock = ref([])
  
  onMounted(async () => {
    const res = await fetch('http://localhost:8080/api/warehouse/stock')
    stock.value = await res.json()
  })
  </script>
  
  <style scoped>
table {
  width: 100%;
  border-collapse: collapse;
}
th, td {
  padding: 0.75rem;
  border-bottom: 1px solid #ccc;
  text-align: left;
}
</style> -->

<template>
  <div>
    <DashboardSummary />

    <h3>Beverage Stock</h3>
    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>Beverage</th>
            <th>Quantity</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in stock" :key="item.beverageType">
            <td>{{ item.beverageType }}</td>
            <td>{{ item.quantity }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import axios from 'axios';
import DashboardSummary from './DashboardSummary.vue';

interface BeverageStock {
  beverageType: string;
  quantity: number;
}

const stock = ref<BeverageStock[]>([]);

onMounted(async () => {
  const { data } = await axios.get<BeverageStock[]>('http://localhost:8085/warehouse/stock');
  stock.value = data;
});
</script>

<style scoped>
.table-container {
  /* limit height so the table scrolls */
  max-height: 400px;          /* or use e.g. 60vh */
  overflow-y: auto;
  border: 1px solid #ddd;     /* optional: match your table styling */
  border-radius: 6px;
}
.table-container table {
  width: 100%;
  border-collapse: collapse;
}
.table-container thead {
  background: #f0f4f8;
  position: sticky;           /* keep header row visible */
  top: 0;
}
.table-container th,
.table-container td {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}
</style>