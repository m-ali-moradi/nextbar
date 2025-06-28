<template>
  <div class="stock-page">
    <!-- bind a key so DashboardSummary remounts on change -->
    <DashboardSummary :key="refreshKey" />

    <h3>Beverage Stock</h3>

    <div class="controls">
      <button
        class="button process"
        @click="adding = true"
        :disabled="adding || deleteMode"
      >+ Add</button>

      <button
        class="button reject"
        @click="deleteMode ? confirmDelete() : (deleteMode = true)"
        :disabled="adding"
      >
        {{ deleteMode ? 'Confirm Delete' : '– Delete' }}
      </button>

      <button
        v-if="deleteMode"
        class="button"
        @click="() => { deleteMode = false; selectedId = null }"
      >Cancel</button>
    </div>

    <!-- inline add form -->
    <div v-if="adding" class="add-form">
      <input
        v-model="newType"
        placeholder="Beverage name"
        class="input"
      />
      <input
        v-model.number="newQty"
        type="number"
        placeholder="Quantity"
        class="input"
      />
      <button class="button process" @click="confirmAdd">Save</button>
      <button class="button" @click="cancelAdd">Cancel</button>
    </div>

    <div
      ref="tableEl"
      class="table-container"
    >
      <table>
        <thead>
          <tr>
            <th v-if="deleteMode">Select</th>
            <th>ID</th>
            <th>Beverage</th>
            <th>Quantity</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in stock" :key="item.id">
            <td v-if="deleteMode">
              <input
                type="radio"
                name="selected"
                :value="item.id"
                v-model="selectedId"
              />
            </td>
            <td>{{ item.id }}</td>
            <td>{{ item.beverageType }}</td>
            <td>{{ item.quantity }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'
import DashboardSummary from './DashboardSummary.vue'

interface BeverageStock {
  id: number
  beverageType: string
  quantity: number
}

const stock      = ref<BeverageStock[]>([])
const adding     = ref(false)
const deleteMode = ref(false)
const selectedId = ref<number|null>(null)
const newType    = ref('')
const newQty     = ref<number|null>(null)
const tableEl    = ref<HTMLDivElement|null>(null)

// bump this to force DashboardSummary remount
const refreshKey = ref(0)

async function loadStock(scrollToBottom = false) {
  const { data } = await axios.get<BeverageStock[]>(
    'http://localhost:8085/warehouse/stock'
  )
  stock.value = data

  // force the summary to update
  refreshKey.value++

  if (scrollToBottom) {
    await nextTick()
    if (tableEl.value) {
      tableEl.value.scrollTop = tableEl.value.scrollHeight
    }
  }
}

function cancelAdd() {
  newType.value = ''
  newQty.value  = null
  adding.value  = false
}

async function confirmAdd() {
  // ── validation ──
  if (!newType.value.trim() || newQty.value == null) {
    return alert('Please enter both a name and a quantity.')
  }
  if (newQty.value < 0) {
    return alert('Quantity cannot be negative.')
  }

  await axios.post(
    'http://localhost:8085/warehouse/stock',
    { beverageType: newType.value.trim(), quantity: newQty.value }
  )
  newType.value = ''
  newQty.value  = null
  adding.value  = false

  // reload & scroll
  await loadStock(true)
}

async function confirmDelete() {
  if (selectedId.value == null) return
  if (!confirm(`Do you really want to delete item #${selectedId.value}?`)) return

  await axios.delete(
    `http://localhost:8085/warehouse/stock/${selectedId.value}`
  )
  selectedId.value = null
  deleteMode.value = false

  // reload (no scroll)
  await loadStock()
}

onMounted(() => loadStock())
</script>

<style scoped>
.stock-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.controls {
  display: flex;
  gap: 0.5rem;
  margin: 1rem 0;
}

.add-form {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.input {
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  flex: 1;
}

.button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.1s ease, box-shadow 0.1s ease;
}
.button.process { background: #3182ce; color: #fff; }
.button.reject  { background: #e53e3e; color: #fff; }
.button:hover {
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.button:active {
  transform: scale(0.95);
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.2);
}

/* reduce height so last row is reachable */
.table-container {
  flex: 1;
  max-height: 300px;
  /* min-height: 0; */
  overflow: auto;
  border: 1px solid #ddd;
  border-radius: 6px;
}
.table-container table {
  width: 100%;
  border-collapse: collapse;
}
.table-container thead {
  background: #f0f4f8;
  position: sticky;
  top: 0;
}
th, td {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}
</style>
