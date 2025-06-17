

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
    const res = await fetch('http://localhost:8082/warehouse/stock')
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
</style>