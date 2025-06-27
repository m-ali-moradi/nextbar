<template>
  <div class="container">
    <!-- Breadcrumb / Back link -->
    <nav class="breadcrumb">
      <!--router-link to="/events" class="breadcrumb-item">All Events</router-link-->
      <!--span class="breadcrumb-item active">{{ event?.name }}</span-->
    </nav>

    <!-- Page Title + Actions -->
    <div class="page-header">
      <h1>{{ event?.name }}</h1>
      <div class="actions">
        <router-link
          v-if="event"
          :to="`/events/${event.eventId}/edit`"
          class="btn info"
        >
          Edit Event
        </router-link>
        <router-link to="/events" class="btn secondary">Back to List</router-link>
      </div>
    </div>

    <!-- Event Summary Card -->
    <section class="card">
      <header class="card-header">Summary</header>
      <div class="card-body">
        <div class="summary-list">
          <div class="summary-item"><span class="label">ID:</span> {{ event?.eventId }}</div>
          <div class="summary-item"><span class="label">Date:</span> {{ event?.date }}</div>
          <div class="summary-item"><span class="label">Location:</span> {{ event?.location }}</div>
          <div class="summary-item"><span class="label">Duration (hrs):</span> {{ event?.duration }}</div>
          <div class="summary-item"><span class="label">Status:</span> {{ event?.status }}</div>
        </div>
      </div>
    </section>

    <!-- Beverages Card -->
    <section class="card">
      <header class="card-header">Allocated Beverages</header>
      <div class="card-body p-0">
        <table class="table">
          <thead>
            <tr><th>Name</th><th>Price</th></tr>
          </thead>
          <tbody>
            <tr v-for="bev in event?.beverages || []" :key="bev.id">
              <td>{{ bev.name }}</td>
              <td>{{ formatPrice(bev.price) }}</td>
            </tr>
            <tr v-if="!(event?.beverages?.length)">
              <td colspan="2" class="no-data">No beverages assigned.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Bars Card -->
    <section class="card">
      <header class="card-header">Bars</header>
      <div class="card-body p-0">
        <table class="table">
          <thead>
            <tr>
              <th>Name</th><th>Location</th><th>Capacity</th><th>Stock</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="bar in event?.bars || []" :key="bar.barId">
              <td>{{ bar.barName }}</td>
              <td>{{ bar.location }}</td>
              <td>{{ bar.totalCapacity }}</td>
              <td>
                <ul class="stock-list">
                  <li v-for="(qty, name) in bar.beverageStock" :key="name">
                    {{ name }}: {{ qty }}
                  </li>
                  <li v-if="!Object.keys(bar.beverageStock || {}).length" class="no-data">
                    No stock defined.
                  </li>
                </ul>
              </td>
            </tr>
            <tr v-if="!(event?.bars?.length)">
              <td colspan="4" class="no-data">No bars defined.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Drop Points Card -->
    <section class="card">
      <header class="card-header">Drop Points</header>
      <div class="card-body p-0">
        <table class="table">
          <thead>
            <tr><th>Location</th><th>Capacity</th></tr>
          </thead>
          <tbody>
            <tr v-for="dp in event?.dropPoints || []" :key="dp.dropPointId">
              <td>{{ dp.location }}</td>
              <td>{{ dp.capacity }}</td>
            </tr>
            <tr v-if="!(event?.dropPoints?.length)">
              <td colspan="2" class="no-data">No drop points defined.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const event = ref(null);

function formatPrice(p) {
  return p != null ? p.toFixed(2) : '';
}

onMounted(async () => {
  const id = route.params.id;
  try {
    const res = await axios.get(`/api/events/${id}`);
    event.value = res.data;
  } catch (e) {
    console.error('Failed to load event details', e);
  }
});
</script>

<style scoped>
.container {
  max-width: 900px;
  margin: 2rem auto;
  padding: 0 1rem;
}
.breadcrumb {
  display: flex;
  gap: 0.5rem;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}
.breadcrumb-item {
  text-decoration: none;
  color: #007acc;
}
.breadcrumb-item.active {
  color: #333;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.page-header h1 {
  margin: 0;
  font-size: 2rem;
}
.actions .btn {
  margin-left: 0.5rem;
}
.card {
  border: 1px solid #ddd;
  border-radius: 6px;
  margin-bottom: 1.5rem;
}
.card-header {
  background: #f5f5f5;
  padding: 0.75rem 1rem;
  font-weight: 600;
}
.card-body {
  padding: 1rem;
}
.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}
.table th,
.table td {
  border: 1px solid #ddd;
  padding: 0.6rem 0.8rem;
  text-align: left;
}
.stock-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.stock-list li {
  margin-bottom: 0.25rem;
}
.no-data {
  text-align: center;
  color: #777;
  padding: 0.5rem 0;
}
.btn {
  padding: 0.4rem 0.8rem;
  text-decoration: none;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}
.btn.info {
  background: #17a2b8;
  color: white;
}
.btn.secondary {
  background: #6c757d;
  color: white;
}

.summary-list {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  font-size: 0.95rem;
}
.summary-item {
  line-height: 1.4;
}
.label {
  font-weight: 600;
  color: #444;
  margin-right: 0.3rem;
}

</style>
