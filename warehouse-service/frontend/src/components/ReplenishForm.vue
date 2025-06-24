<template>
  <div>
    <h2>Replenish Beverage</h2>
    <form @submit.prevent="submitForm">
      <input v-model="beverageType" placeholder="Beverage Type" />
      <input v-model.number="quantity" type="number" placeholder="Quantity" />
      <button type="submit">Replenish</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const beverageType = ref('')
const quantity = ref(0)

async function submitForm() {
  await fetch('http://localhost:8085/api/warehouse/replenish', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ beverageType: beverageType.value, quantity: quantity.value })
  })
  alert('Replenished!')
}
</script>
