
<template>
  <div>
    <h2>Empty Bottles</h2>
    <ul>
      <p>Current empty bottles: {{ count }}</p>
    </ul>
  </div>
</template>


<script setup>
import { ref, onMounted } from 'vue'
const count = ref(0)

onMounted(async () => {
  const res = await fetch('http://localhost:8085/api/warehouse/empty-stock')
  count.value = await res.json()
})
</script>



<style scoped>
ul {
  list-style: none;
  padding: 0;
}
</style>
