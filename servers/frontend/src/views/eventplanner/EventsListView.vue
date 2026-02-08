<template>
  <div class="flex min-h-screen bg-slate-50">
    <Sidebar />
    
    <div class="flex-1 ml-72">
      <Navbar />

      <main class="p-6 lg:p-8">
        <!-- Page Header -->
        <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-8">
          <div>
            <h1 class="text-3xl font-bold text-slate-900">Event Planner</h1>
            <p class="text-slate-500 mt-1">Plan, manage, and monitor your events</p>
          </div>
          <button @click="navigateToCreate" class="btn-primary w-full lg:w-auto">
            <i class="fas fa-plus"></i>
            <span>Create Event</span>
          </button>
        </div>

        <!-- Search & Filters -->
        <div class="card p-4 mb-6">
          <div class="flex flex-col lg:flex-row gap-4">
            <!-- Search -->
            <div class="relative flex-1">
              <i class="fas fa-search absolute left-4 top-1/2 -translate-y-1/2 text-slate-400"></i>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Search events by name, location..."
                class="input pl-11"
              />
            </div>

            <!-- Status Filter -->
            <div class="flex flex-wrap gap-2">
              <button
                v-for="status in statusFilters"
                :key="status.value"
                @click="toggleStatusFilter(status.value)"
                class="px-4 py-2.5 rounded-xl text-sm font-semibold transition-all duration-200"
                :class="activeStatusFilters.includes(status.value) 
                  ? status.activeClass 
                  : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
              >
                <i :class="status.icon" class="mr-1.5"></i>
                {{ status.label }}
              </button>
            </div>

            <!-- Date Range -->
            <div class="flex gap-2">
              <input
                v-model="dateFrom"
                type="date"
                class="input text-sm"
                placeholder="From"
              />
              <input
                v-model="dateTo"
                type="date"
                class="input text-sm"
                placeholder="To"
              />
            </div>

            <!-- Clear Filters -->
            <button
              v-if="hasActiveFilters"
              @click="clearFilters"
              class="btn-ghost text-sm px-3"
            >
              <i class="fas fa-times mr-1"></i>
              Clear
            </button>
          </div>
        </div>

        <!-- Stats Overview -->
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <div class="stat-card">
            <div class="flex items-center justify-between">
              <div>
                <p class="stat-label">Total Events</p>
                <p class="stat-value text-slate-900">{{ events.length }}</p>
              </div>
              <div class="stat-icon bg-event-100 text-event-600">
                <i class="fas fa-calendar-alt"></i>
              </div>
            </div>
          </div>
          
          <div class="stat-card">
            <div class="flex items-center justify-between">
              <div>
                <p class="stat-label">Scheduled</p>
                <p class="stat-value text-blue-600">{{ statusCounts.scheduled }}</p>
              </div>
              <div class="stat-icon bg-blue-100 text-blue-600">
                <i class="fas fa-clock"></i>
              </div>
            </div>
          </div>

          <div class="stat-card">
            <div class="flex items-center justify-between">
              <div>
                <p class="stat-label">Running</p>
                <p class="stat-value text-amber-600">{{ statusCounts.running }}</p>
              </div>
              <div class="stat-icon bg-amber-100 text-amber-600">
                <i class="fas fa-play-circle"></i>
              </div>
            </div>
          </div>

          <div class="stat-card">
            <div class="flex items-center justify-between">
              <div>
                <p class="stat-label">Completed</p>
                <p class="stat-value text-emerald-600">{{ statusCounts.completed }}</p>
              </div>
              <div class="stat-icon bg-emerald-100 text-emerald-600">
                <i class="fas fa-check-circle"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="flex items-center justify-center py-20">
          <div class="text-center">
            <div class="w-16 h-16 border-4 border-event-200 border-t-event-600 rounded-full animate-spin mx-auto mb-4"></div>
            <p class="text-slate-500 text-lg">Loading events...</p>
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="card p-12 text-center">
          <div class="w-20 h-20 rounded-2xl bg-red-100 flex items-center justify-center mx-auto mb-6">
            <i class="fas fa-exclamation-triangle text-3xl text-red-600"></i>
          </div>
          <h3 class="text-xl font-semibold text-slate-900 mb-2">Error Loading Events</h3>
          <p class="text-slate-500 mb-6">{{ error }}</p>
          <button @click="eventStore.fetchEvents" class="btn-primary">
            <i class="fas fa-redo"></i>
            <span>Try Again</span>
          </button>
        </div>

        <!-- Events Content -->
        <div v-else-if="filteredEvents.length > 0" class="space-y-8">
          <!-- Active Events (Running) -->
          <section v-if="activeEvents.length > 0">
            <div class="flex items-center gap-3 mb-4">
              <div class="w-3 h-3 rounded-full bg-amber-500 animate-pulse"></div>
              <h2 class="text-lg font-bold text-slate-900">Active Events</h2>
              <span class="badge badge-warning">{{ activeEvents.length }}</span>
            </div>
            <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
              <EventCard
                v-for="event in activeEvents"
                :key="event.id"
                :event="event"
                @view="viewEvent"
                @edit="editEvent"
                @start="confirmStartEvent"
                @complete="confirmCompleteEvent"
                @delete="confirmDeleteEvent"
              />
            </div>
          </section>

          <!-- Upcoming Events (Scheduled) -->
          <section v-if="upcomingFilteredEvents.length > 0">
            <div class="flex items-center gap-3 mb-4">
              <i class="fas fa-calendar-check text-blue-500"></i>
              <h2 class="text-lg font-bold text-slate-900">Scheduled Events</h2>
              <span class="badge badge-info">{{ upcomingFilteredEvents.length }}</span>
            </div>
            <div class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
              <EventCard
                v-for="event in upcomingFilteredEvents"
                :key="event.id"
                :event="event"
                @view="viewEvent"
                @edit="editEvent"
                @start="confirmStartEvent"
                @delete="confirmDeleteEvent"
              />
            </div>
          </section>

          <!-- Past Events -->
          <section v-if="pastFilteredEvents.length > 0">
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center gap-3">
                <i class="fas fa-history text-slate-400"></i>
                <h2 class="text-lg font-bold text-slate-900">Past Events</h2>
                <span class="badge badge-neutral">{{ pastFilteredEvents.length }}</span>
              </div>
              <button
                @click="showPastEvents = !showPastEvents"
                class="text-sm text-slate-500 hover:text-slate-700"
              >
                {{ showPastEvents ? 'Hide' : 'Show' }}
                <i :class="showPastEvents ? 'fas fa-chevron-up' : 'fas fa-chevron-down'" class="ml-1"></i>
              </button>
            </div>
            <div v-if="showPastEvents" class="card overflow-hidden animate-slide-up">
              <table class="table-modern">
                <thead>
                  <tr>
                    <th>Event</th>
                    <th>Date</th>
                    <th>Location</th>
                    <th>Status</th>
                    <th>Resources</th>
                    <th class="text-right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="event in pastFilteredEvents" :key="event.id">
                    <td>
                      <div class="font-semibold text-slate-900">{{ event.name }}</div>
                      <div class="text-sm text-slate-500">{{ event.organizerName || 'No organizer' }}</div>
                    </td>
                    <td>{{ formatDate(event.date) }}</td>
                    <td>{{ event.location || 'N/A' }}</td>
                    <td>
                      <StatusBadge :status="event.status" />
                    </td>
                    <td>
                      <div class="flex items-center gap-3">
                        <span class="text-sm">
                          <i class="fas fa-glass-martini text-bar-500 mr-1"></i>
                          {{ event.barCount }}
                        </span>
                        <span class="text-sm">
                          <i class="fas fa-map-marker-alt text-droppoint-500 mr-1"></i>
                          {{ event.dropPointCount }}
                        </span>
                      </div>
                    </td>
                    <td>
                      <div class="flex items-center justify-end gap-2">
                        <button
                          @click="viewEvent(event.id)"
                          class="p-2 rounded-lg text-slate-500 hover:text-event-600 hover:bg-event-50 transition-colors"
                          title="View Details"
                        >
                          <i class="fas fa-eye"></i>
                        </button>
                        <button
                          @click="confirmDeleteEvent(event.id)"
                          class="p-2 rounded-lg text-slate-500 hover:text-red-600 hover:bg-red-50 transition-colors"
                          title="Delete"
                        >
                          <i class="fas fa-trash-alt"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </div>

        <!-- Empty State -->
        <div v-else class="empty-state card p-12">
          <div class="empty-icon bg-event-100 text-event-400">
            <i class="fas fa-calendar-alt"></i>
          </div>
          <h3 v-if="hasActiveFilters">No events match your filters</h3>
          <h3 v-else>No events yet</h3>
          <p v-if="hasActiveFilters">
            Try adjusting your search criteria or clear filters.
          </p>
          <p v-else>
            Create your first event to start planning bars and drop points.
          </p>
          <button v-if="!hasActiveFilters" @click="navigateToCreate" class="btn-primary mt-4">
            <i class="fas fa-plus"></i>
            <span>Create Your First Event</span>
          </button>
          <button v-else @click="clearFilters" class="btn-secondary mt-4">
            <i class="fas fa-times"></i>
            <span>Clear Filters</span>
          </button>
        </div>
      </main>
    </div>

    <!-- Confirmation Modal -->
    <ConfirmModal
      v-if="showConfirmModal"
      :title="confirmModal.title"
      :message="confirmModal.message"
      :confirm-text="confirmModal.confirmText"
      :confirm-class="confirmModal.confirmClass"
      :loading="confirmModal.loading"
      @confirm="confirmModal.onConfirm"
      @cancel="closeConfirmModal"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useEventStore } from '@/stores/eventStore';
import { EventSummary, EventStatus } from '@/api/eventApi';
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';
import Sidebar from '@/components/common/Sidebar.vue';
import Navbar from '@/components/common/Navbar.vue';
import EventCard from '@/components/eventplanner/EventCard.vue';
import StatusBadge from '@/components/eventplanner/StatusBadge.vue';
import ConfirmModal from '@/components/eventplanner/ConfirmModal.vue';

const router = useRouter();
const eventStore = useEventStore();

// ============ State ============
const searchQuery = ref('');
const activeStatusFilters = ref<EventStatus[]>([]);
const dateFrom = ref('');
const dateTo = ref('');
const showPastEvents = ref(false);
const showConfirmModal = ref(false);
const confirmModal = ref({
  title: '',
  message: '',
  confirmText: 'Confirm',
  confirmClass: 'btn-primary',
  loading: false,
  onConfirm: () => {},
});

// Status filter config
const statusFilters = [
  { value: 'SCHEDULED' as EventStatus, label: 'Scheduled', icon: 'fas fa-clock', activeClass: 'bg-blue-100 text-blue-700' },
  { value: 'RUNNING' as EventStatus, label: 'Running', icon: 'fas fa-play', activeClass: 'bg-amber-100 text-amber-700' },
  { value: 'COMPLETED' as EventStatus, label: 'Completed', icon: 'fas fa-check', activeClass: 'bg-emerald-100 text-emerald-700' },
  { value: 'CANCELLED' as EventStatus, label: 'Cancelled', icon: 'fas fa-ban', activeClass: 'bg-red-100 text-red-700' },
];

// ============ Computed ============
const events = computed(() => eventStore.events);
const loading = computed(() => eventStore.loading);
const error = computed(() => eventStore.error);
const statusCounts = computed(() => eventStore.statusCounts);

const hasActiveFilters = computed(() =>
  searchQuery.value.trim() !== '' ||
  activeStatusFilters.value.length > 0 ||
  dateFrom.value !== '' ||
  dateTo.value !== ''
);

const filteredEvents = computed(() => {
  let result = [...events.value];

  // Search filter
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(e =>
      e.name.toLowerCase().includes(query) ||
      e.location?.toLowerCase().includes(query) ||
      e.organizerName?.toLowerCase().includes(query)
    );
  }

  // Status filter
  if (activeStatusFilters.value.length > 0) {
    result = result.filter(e => activeStatusFilters.value.includes(e.status));
  }

  // Date range filter
  if (dateFrom.value) {
    result = result.filter(e => e.date >= dateFrom.value);
  }
  if (dateTo.value) {
    result = result.filter(e => e.date <= dateTo.value);
  }

  return result;
});

const activeEvents = computed(() =>
  filteredEvents.value.filter(e => e.status === 'RUNNING')
);

const upcomingFilteredEvents = computed(() =>
  filteredEvents.value.filter(e => e.status === 'SCHEDULED')
);

const pastFilteredEvents = computed(() =>
  filteredEvents.value.filter(e => e.status === 'COMPLETED' || e.status === 'CANCELLED')
);

// ============ Methods ============
const toggleStatusFilter = (status: EventStatus) => {
  const index = activeStatusFilters.value.indexOf(status);
  if (index > -1) {
    activeStatusFilters.value.splice(index, 1);
  } else {
    activeStatusFilters.value.push(status);
  }
};

const clearFilters = () => {
  searchQuery.value = '';
  activeStatusFilters.value = [];
  dateFrom.value = '';
  dateTo.value = '';
};

const navigateToCreate = () => {
  router.push('/events/new');
};

const viewEvent = (id: number) => {
  router.push(`/events/${id}`);
};

const editEvent = (id: number) => {
  router.push(`/events/${id}/edit`);
};

const formatDate = (date: string) => {
  if (!date) return 'N/A';
  return new Date(date).toLocaleDateString('en-US', {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};

// Confirmation modals
const closeConfirmModal = () => {
  showConfirmModal.value = false;
  confirmModal.value.loading = false;
};

const confirmStartEvent = (id: number) => {
  confirmModal.value = {
    title: 'Start Event',
    message: 'Are you sure you want to start this event? This will activate all assigned bars and drop points.',
    confirmText: 'Start Event',
    confirmClass: 'btn-success',
    loading: false,
    onConfirm: async () => {
      confirmModal.value.loading = true;
      try {
        await eventStore.startEvent(id);
        toast.success('Event started successfully!');
        closeConfirmModal();
      } catch (err) {
        toast.error('Failed to start event');
        confirmModal.value.loading = false;
      }
    },
  };
  showConfirmModal.value = true;
};

const confirmCompleteEvent = (id: number) => {
  confirmModal.value = {
    title: 'Complete Event',
    message: 'Are you sure you want to mark this event as completed? This action cannot be undone.',
    confirmText: 'Complete Event',
    confirmClass: 'btn-primary',
    loading: false,
    onConfirm: async () => {
      confirmModal.value.loading = true;
      try {
        await eventStore.completeEvent(id);
        toast.success('Event completed!');
        closeConfirmModal();
      } catch (err) {
        toast.error('Failed to complete event');
        confirmModal.value.loading = false;
      }
    },
  };
  showConfirmModal.value = true;
};

const confirmDeleteEvent = (id: number) => {
  confirmModal.value = {
    title: 'Delete Event',
    message: 'Are you sure you want to delete this event? This action cannot be undone and will remove all associated bars and drop points.',
    confirmText: 'Delete',
    confirmClass: 'btn-danger',
    loading: false,
    onConfirm: async () => {
      confirmModal.value.loading = true;
      try {
        await eventStore.deleteEvent(id);
        toast.success('Event deleted successfully');
        closeConfirmModal();
      } catch (err) {
        toast.error('Failed to delete event');
        confirmModal.value.loading = false;
      }
    },
  };
  showConfirmModal.value = true;
};

// ============ Lifecycle ============
onMounted(() => {
  eventStore.fetchEvents();
});
</script>
