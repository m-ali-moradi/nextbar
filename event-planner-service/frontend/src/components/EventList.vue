<template>
  <div class="container">
    <header class="header">
      <h1>All Events</h1>
      <nav>
        <router-link to="/events" class="nav-link active">All Events</router-link>
        <router-link to="/event/new" class="nav-link">Create Event</router-link>
      </nav>
    </header>

    <div v-if="success" class="alert success">{{ success }}</div>
    <div v-if="error"   class="alert error">{{ error }}</div>

    <div class="toolbar">
      <router-link to="/event/new" class="btn primary">
        + Create New Event
      </router-link>
    </div>

    <table class="events-table">
      <thead>
        <tr>
          <th>ID</th><th>Name</th><th>Date</th><th>Location</th>
          <th>Duration</th><th>Status</th><th>Beverages</th>
          <th># Bars</th><th># Drops</th><th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="ev in events" :key="ev.eventId">
          <td>{{ ev.eventId }}</td>
          <td>{{ ev.name }}</td>
          <td>{{ ev.date }}</td>
          <td>{{ ev.location }}</td>
          <td>{{ ev.duration }}h</td>
          <td>{{ ev.status }}</td>
          <td>{{ (ev.beverages || []).map(b => b.name).join(', ') }}</td>
          <td>{{ (ev.bars       || []).length }}</td>
          <td>{{ (ev.dropPoints || []).length }}</td>
          <td class="actions">
            <router-link :to="`/event/${ev.eventId}`"      class="btn secondary">Details</router-link>
            <router-link :to="`/event/${ev.eventId}/edit`" class="btn info">Edit</router-link>
            <button @click="deleteEvent(ev.eventId)" class="btn danger">Delete</button>
          </td>
        </tr>
        <tr v-if="events.length === 0">
          <td colspan="10" class="no-data">No events found.</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const events  = ref([])
const success = ref('')
const error   = ref('')

async function fetchEvents() {
  success.value = ''
  error.value   = ''
  try {
    // relative path; Vue dev server will proxy /api → backend
    const res = await axios.get('/api/events')
    events.value = res.data.map(ev => ({
      ...ev,
      beverages:   ev.beverages   || [],
      bars:        ev.bars        || [],
      dropPoints:  ev.dropPoints  || []
    }))
  } catch (err) {
    console.error('Error fetching events:', err)
    error.value = err.response?.data?.message || 'Failed to load events.'
  }
}

async function deleteEvent(id) {
  if (!confirm('Are you sure you want to delete this event?')) return
  try {
    await axios.delete(`/api/events/${id}`)
    success.value = 'Event deleted successfully.'
    await fetchEvents()
  } catch (err) {
    console.error('Error deleting event:', err)
    error.value = err.response?.data?.message || 'Failed to delete event.'
  }
}

onMounted(fetchEvents)
</script>

<style scoped>
/* Container and Layout */
.container {
  max-width: 900px;
  margin: 2rem auto;
  padding: 0 1rem;
}

/* Header */
.header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  flex-wrap: wrap;
  margin-bottom: 1rem;
}
.header h1 {
  margin: 0;
  font-size: 2rem;
  color: #333;
}
nav {
  display: flex;
  gap: 1rem;
}
.nav-link {
  text-decoration: none;
  color: #666;
  font-weight: 500;
  transition: color 0.2s;
}
.nav-link.active,
.nav-link:hover {
  color: #007acc;
}

/* Alerts */
.alert {
  padding: 0.75rem 1rem;
  margin-bottom: 1rem;
  border-radius: 4px;
  font-size: 0.95rem;
}
.success { background: #e6ffed; color: #2d7a3e; }
.error   { background: #ffe6e6; color: #8a2424; }

/* Toolbar */
.toolbar {
  text-align: right;
  margin-bottom: 1rem;
}

/* Buttons */
.btn {
  display: inline-block;
  padding: 0.4rem 0.8rem;
  margin: 0 0.2rem;
  border: none;
  border-radius: 4px;
  font-size: 0.875rem;
  cursor: pointer;
  text-decoration: none;
  color: white;
  transition: background 0.2s;
}
.primary   { background: #007acc; }
.secondary { background: #555; }
.info      { background: #17a2b8; }
.danger    { background: #dc3545; }
.btn:hover { filter: brightness(0.9); }

/* Table */
.events-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}
.events-table thead {
  background: #f5f5f5;
}
.events-table th,
.events-table td {
  padding: 0.6rem 0.8rem;
  border: 1px solid #ddd;
  text-align: left;
}
.events-table tbody tr:nth-child(even) {
  background: #fafafa;
}
.actions {
  white-space: nowrap;
}

/* No-data row */
.no-data {
  text-align: center;
  padding: 1rem;
  color: #777;
}
</style>
