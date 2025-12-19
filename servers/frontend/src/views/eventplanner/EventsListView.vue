<template>
  <div class="events-container">
    <header class="page-header">
      <h1 class="text-3xl font-bold text-gray-800">Event Management</h1>
      <button @click="navigateToCreate" class="btn-primary">
        <span class="text-lg">+</span> Create Event
      </button>
    </header>

    <!-- Error State -->
    <div v-if="error" class="error-banner">
      {{ error }}
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <p>Loading events...</p>
    </div>

    <!-- Events Table -->
    <div v-if="!loading && events.length > 0" class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Date</th>
            <th>Location</th>
            <th>Duration</th>
            <th>Status</th>
            <th>Beverages</th>
            <th># Bars</th>
            <th># Drop Points</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="event in events" :key="event.eventId">
            <td>{{ event.eventId }}</td>
            <td>{{ event.name }}</td>
            <td>{{ formatDate(event.date) }}</td>
            <td>{{ event.location }}</td>
            <td>{{ event.duration }}h</td>
            <td>
              <span
                class="status-badge"
                :class="getStatusClass(event.status)"
              >
                {{ event.status }}
              </span>
            </td>
            <td>{{ getBeverageNames(event.beverages) }}</td>
            <td>{{ event.bars?.length || 0 }}</td>
            <td>{{ event.dropPoints?.length || 0 }}</td>
            <td class="actions-cell">
              <button @click="viewEvent(event.eventId!)" class="btn-view">
                Details
              </button>
              <button @click="editEvent(event.eventId!)" class="btn-edit">
                Edit
              </button>
              <button @click="deleteEventConfirm(event.eventId!)" class="btn-delete">
                Delete
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && events.length === 0" class="empty-state">
      <p>No events found. Create your first event to get started!</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useEventStore } from '@/stores/eventStore';
import { Beverage } from '@/api/eventApi';
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';

const router = useRouter();
const eventStore = useEventStore();

// Computed
const events = computed(() => eventStore.events);
const loading = computed(() => eventStore.loading);
const error = computed(() => eventStore.error);

// Methods
const navigateToCreate = () => {
  router.push('/events/new');
};

const viewEvent = (id: number) => {
  router.push(`/events/${id}`);
};

const editEvent = (id: number) => {
  router.push(`/events/${id}/edit`);
};

const deleteEventConfirm = async (id: number) => {
  if (!confirm('Are you sure you want to delete this event?')) return;

  try {
    await eventStore.deleteEvent(id);
    toast.success('Event deleted successfully');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to delete event';
    toast.error(errorMessage);
  }
};

const formatDate = (date: string) => {
  if (!date) return 'N/A';
  return new Date(date).toLocaleDateString();
};

const getBeverageNames = (beverages: Beverage[]) => {
  if (!beverages || beverages.length === 0) return 'None';
  return beverages.map(b => b.name).join(', ');
};

const getStatusClass = (status: string) => {
  switch (status) {
    case 'PLANNED':
      return 'status-planned';
    case 'ONGOING':
      return 'status-ongoing';
    case 'COMPLETED':
      return 'status-completed';
    default:
      return '';
  }
};

// Lifecycle
onMounted(() => {
  eventStore.fetchEvents();
});
</script>

<style scoped>
.events-container {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.btn-primary {
  background-color: #3b82f6;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  border: none;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: background-color 0.2s;
}

.btn-primary:hover {
  background-color: #2563eb;
}

.error-banner {
  background-color: #fee2e2;
  color: #991b1b;
  padding: 1rem;
  border-radius: 0.5rem;
  margin-bottom: 1rem;
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
}

.table-container {
  background: white;
  border-radius: 0.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead {
  background-color: #f9fafb;
  border-bottom: 2px solid #e5e7eb;
}

.data-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.data-table td {
  padding: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.data-table tbody tr:hover {
  background-color: #f9fafb;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-planned {
  background-color: #dbeafe;
  color: #1e40af;
}

.status-ongoing {
  background-color: #fef3c7;
  color: #92400e;
}

.status-completed {
  background-color: #d1fae5;
  color: #065f46;
}

.actions-cell {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.btn-view,
.btn-edit,
.btn-delete {
  padding: 0.375rem 0.75rem;
  border-radius: 0.375rem;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-view {
  background-color: #8b5cf6;
  color: white;
}

.btn-view:hover {
  background-color: #7c3aed;
}

.btn-edit {
  background-color: #3b82f6;
  color: white;
}

.btn-edit:hover {
  background-color: #2563eb;
}

.btn-delete {
  background-color: #ef4444;
  color: white;
}

.btn-delete:hover {
  background-color: #dc2626;
}
</style>
