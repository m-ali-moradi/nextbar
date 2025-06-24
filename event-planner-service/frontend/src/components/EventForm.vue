<template>
  <div class="form-container">
    <h1>{{ isEdit ? 'Edit Event' : 'Create Event' }}</h1>
    <form @submit.prevent="handleSubmit">
      <!-- ====== BASIC DETAILS ====== -->
      <section class="section">
        <h2>Basic Details</h2>
        <div class="form-group">
          <label for="name">Event Name</label>
          <input id="name" v-model="form.name" type="text" required />
        </div>
        <div class="form-group">
          <label for="date">Date</label>
          <input id="date" v-model="form.date" type="date" required />
        </div>
        <div class="form-group">
          <label for="location">Location</label>
          <input id="location" v-model="form.location" type="text" required />
        </div>
        <div class="form-group">
          <label for="duration">Duration (hrs)</label>
          <input id="duration" v-model.number="form.duration" type="number" min="1" />
        </div>
        <div class="form-group">
          <label for="status">Status</label>
          <select id="status" v-model="form.status" required>
            <option value="">Select…</option>
            <option>PLANNED</option>
            <option>ONGOING</option>
            <option>COMPLETED</option>
          </select>
        </div>
      </section>

      <!-- ====== BEVERAGE SELECTION ====== -->
      <section class="section">
        <h2>Select Beverages</h2>
        <div class="form-group">
          <select v-model="form.beverageIds" multiple required>
            <option
              v-for="bev in allBeverages"
              :key="bev.id"
              :value="bev.id"
            >
              {{ bev.name }} ({{ bev.price }})
            </option>
          </select>
          <small class="hint">
            Hold Ctrl (Cmd) to select multiple.
          </small>
        </div>
      </section>

      <!-- ====== BARS CONFIGURATION ====== -->
      <section class="section">
        <h2>Bars Configuration</h2>
        <button type="button" class="btn add" @click="addBar">
          + Add Bar
        </button>
        <div v-if="form.bars.length === 0" class="alert">Add at least one bar</div>

        <div
          v-for="(bar, i) in form.bars"
          :key="i"
          class="bar-block"
        >
          <div class="bar-header">
            <h3>Bar {{ i + 1 }}</h3>
            <button type="button" class="btn remove" @click="removeBar(i)">×</button>
          </div>

          <div class="form-group">
            <label>Name</label>
            <input v-model="bar.barName" type="text" required />
          </div>
          <div class="form-group">
            <label>Location</label>
            <input v-model="bar.location" type="text" required />
          </div>
          <div class="form-group">
            <label>Total Capacity</label>
            <input v-model.number="bar.totalCapacity" type="number" min="0" required />
          </div>

          <div class="form-group">
            <label>Assign Beverage Quantities</label>
            <div
              v-for="bevId in form.beverageIds"
              :key="bevId"
              class="assign-row"
            >
              <input
                v-model.number="bar.beverageStock[bevId]"
                type="number"
                min="0"
                :placeholder="`Qty for ${getBeverageName(bevId)}`"
              />
              <span class="bev-label">{{ getBeverageName(bevId) }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- ====== DROP POINTS ====== -->
      <section class="section">
        <h2>Drop-Points</h2>
        <button type="button" class="btn add" @click="addDropPoint">
          + Add Drop Point
        </button>
        <div v-if="form.dropPoints.length === 0" class="alert">Add at least one drop point</div>

        <div
          v-for="(dp, i) in form.dropPoints"
          :key="i"
          class="dp-block"
        >
          <div class="bar-header">
            <h3>Drop Point {{ i + 1 }}</h3>
            <button type="button" class="btn remove" @click="removeDropPoint(i)">×</button>
          </div>
          <div class="form-group">
            <label>Location</label>
            <input v-model="dp.location" type="text" required />
          </div>
          <div class="form-group">
            <label>Capacity</label>
            <input v-model.number="dp.capacity" type="number" min="0" required />
          </div>
        </div>
      </section>

      <!-- ====== SUBMIT ====== -->
      <div class="actions">
        <button type="submit" class="btn submit">
          {{ isEdit ? 'Save Changes' : 'Create Event' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const isEdit = computed(() => !!route.params.id)

// Main form state
const form = reactive({
  eventId: null,
  name: '',
  date: '',
  location: '',
  duration: 1,
  status: '',
  beverageIds: [],
  bars: [],
  dropPoints: []
})

// Load beverages and (if edit mode) event details
const allBeverages = ref([])

onMounted(async () => {
  try {
    const bevRes = await axios.get('/api/beverages')
    allBeverages.value = bevRes.data

    if (isEdit.value) {
      const eventRes = await axios.get(`/api/events/${route.params.id}`)
      const data = eventRes.data

      form.eventId = data.eventId
      form.name = data.name
      form.date = data.date
      form.location = data.location
      form.duration = data.duration
      form.status = data.status
      form.beverageIds = data.beverages.map(b => b.id)
      form.bars = data.bars || []
      form.dropPoints = data.dropPoints || []
    }
  } catch (e) {
    console.error('Error loading form data:', e)
  }
})

// Helpers
function getBeverageName(id) {
  const b = allBeverages.value.find(x => x.id === id)
  return b ? b.name : id
}

// Bars
function addBar() {
  const stock = Object.fromEntries(form.beverageIds.map(id => [id, 0]))
  form.bars.push({
    barName: '',
    location: '',
    totalCapacity: 0,
    beverageStock: stock
  })
}
function removeBar(i) {
  form.bars.splice(i, 1)
}

// Drop-Points
function addDropPoint() {
  form.dropPoints.push({ location: '', capacity: 0 })
}
function removeDropPoint(i) {
  form.dropPoints.splice(i, 1)
}

// Submission
async function handleSubmit() {
  const payload = {
    name: form.name,
    date: form.date,
    location: form.location,
    duration: form.duration,
    status: form.status,
    beverages: form.beverageIds.map(id =>
      allBeverages.value.find(b => b.id === id)
    ),
    bars: form.bars.map(bar => ({
      barName: bar.barName,
      location: bar.location,
      totalCapacity: bar.totalCapacity,
      beverageStock: bar.beverageStock
    })),
    dropPoints: form.dropPoints.map(dp => ({
      location: dp.location,
      capacity: dp.capacity
    }))
  }

  try {
    if (isEdit.value) {
      await axios.put(`/api/events/${form.eventId}`, payload)
    } else {
      await axios.post('/api/events', payload)
    }
    window.location.href = '/events'
  } catch (err) {
    alert(err.response?.data?.message || 'Error saving event.')
  }
}
</script>

<style scoped>
.form-container {
  max-width: 700px;
  margin: 2rem auto;
  padding: 1rem 1.5rem;
  background: #fafafa;
  border: 1px solid #ddd;
  border-radius: 6px;
}
.section {
  margin-bottom: 2rem;
}
.section h2 {
  margin-bottom: 1rem;
  font-size: 1.25rem;
  color: #333;
}
.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
}
.form-group input,
.form-group select {
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.hint {
  font-size: 0.85rem;
  color: #666;
  margin-top: -0.5rem;
  margin-bottom: 1rem;
}
.btn {
  padding: 0.4rem 0.9rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.btn.add {
  background: #007acc;
  color: #fff;
  margin-bottom: 1rem;
}
.btn.remove {
  background: #dc3545;
  color: #fff;
  font-size: 0.9rem;
  padding: 0.2rem 0.6rem;
}
.bar-block,
.dp-block {
  border: 1px solid #ccc;
  padding: 1rem;
  border-radius: 6px;
  margin-bottom: 1rem;
}
.bar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}
.assign-row {
  display: flex;
  align-items: center;
  margin-bottom: 0.6rem;
}
.assign-row input {
  width: 4rem;
  margin-right: 0.5rem;
  padding: 0.3rem;
}
.bev-label {
  flex-shrink: 0;
}
.alert {
  background: #ffecec;
  color: #a94442;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}
.actions {
  text-align: right;
}
.btn.submit {
  background: #28a745;
  color: #fff;
  font-size: 1rem;
  padding: 0.6rem 1.2rem;
}
</style>
