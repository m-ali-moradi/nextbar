<template>
  <div class="flex min-h-screen bg-slate-50">
    <Sidebar />
    
    <div class="flex-1 ml-72">
      <Navbar />

      <main class="p-6">
        <!-- Page Header -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-bold text-slate-900">Events</h1>
            <p class="text-slate-500 mt-1">Plan and manage your events</p>
          </div>
          <button @click="navigateToCreate" class="btn-primary">
            <i class="fas fa-plus"></i>
            <span>Create Event</span>
          </button>
        </div>

        <!-- Stats Overview -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Total Events</p>
                <p class="text-2xl font-bold text-slate-900 mt-1">{{ events.length }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-event-100 flex items-center justify-center">
                <i class="fas fa-calendar-alt text-xl text-event-600"></i>
              </div>
            </div>
          </div>
          
          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Planned</p>
                <p class="text-2xl font-bold text-blue-600 mt-1">{{ plannedCount }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-blue-100 flex items-center justify-center">
                <i class="fas fa-clock text-xl text-blue-600"></i>
              </div>
            </div>
          </div>

          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Ongoing</p>
                <p class="text-2xl font-bold text-amber-600 mt-1">{{ ongoingCount }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-amber-100 flex items-center justify-center">
                <i class="fas fa-play-circle text-xl text-amber-600"></i>
              </div>
            </div>
          </div>

          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Completed</p>
                <p class="text-2xl font-bold text-emerald-600 mt-1">{{ completedCount }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-emerald-100 flex items-center justify-center">
                <i class="fas fa-check-circle text-xl text-emerald-600"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="flex items-center justify-center py-20">
          <div class="text-center">
            <div class="w-12 h-12 border-4 border-event-200 border-t-event-600 rounded-full animate-spin mx-auto mb-4"></div>
            <p class="text-slate-500">Loading events...</p>
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="card p-8 text-center">
          <div class="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4">
            <i class="fas fa-exclamation-triangle text-2xl text-red-600"></i>
          </div>
          <h3 class="text-lg font-semibold text-slate-900 mb-2">Error Loading Events</h3>
          <p class="text-slate-500 mb-4">{{ error }}</p>
          <button @click="eventStore.fetchEvents" class="btn-primary">
            <i class="fas fa-redo"></i>
            <span>Try Again</span>
          </button>
        </div>

        <!-- Events Grid (Cards for better visual appeal) -->
        <div v-else-if="events.length > 0" class="space-y-6">
          <!-- Upcoming/Ongoing Events -->
          <div v-if="upcomingEvents.length > 0">
            <h2 class="text-lg font-semibold text-slate-900 mb-4">Upcoming & Active Events</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
              <div
                v-for="event in upcomingEvents"
                :key="event.eventId"
                class="card group card-interactive overflow-hidden"
              >
                <!-- Card Header with Gradient -->
                <div class="h-20 bg-gradient-to-br from-event-500 to-event-700 p-5 relative">
                  <span 
                    class="absolute top-3 right-3 px-3 py-1 rounded-full text-xs font-semibold"
                    :class="getStatusBadgeClass(event.status)"
                  >
                    {{ event.status }}
                  </span>
                  <div class="w-10 h-10 rounded-lg bg-white/20 backdrop-blur-sm 
                              flex items-center justify-center">
                    <i class="fas fa-calendar-alt text-white"></i>
                  </div>
                </div>
                
                <!-- Card Body -->
                <div class="p-5">
                  <h3 class="text-lg font-semibold text-slate-900 mb-2 group-hover:text-event-600 transition-colors">
                    {{ event.name }}
                  </h3>
                  
                  <div class="space-y-2 text-sm text-slate-500 mb-4">
                    <div class="flex items-center gap-2">
                      <i class="fas fa-calendar w-4 text-slate-400"></i>
                      <span>{{ formatDate(event.date) }}</span>
                    </div>
                    <div class="flex items-center gap-2">
                      <i class="fas fa-map-marker-alt w-4 text-slate-400"></i>
                      <span>{{ event.location }}</span>
                    </div>
                    <div class="flex items-center gap-2">
                      <i class="fas fa-clock w-4 text-slate-400"></i>
                      <span>{{ event.duration }} hours</span>
                    </div>
                  </div>

                  <!-- Resources Summary -->
                  <div class="flex items-center gap-4 mb-4 pt-4 border-t border-slate-100">
                    <div class="flex items-center gap-1.5 text-sm">
                      <div class="w-6 h-6 rounded bg-bar-100 flex items-center justify-center">
                        <i class="fas fa-glass-martini text-xs text-bar-600"></i>
                      </div>
                      <span class="font-medium text-slate-700">{{ event.bars?.length || 0 }} bars</span>
                    </div>
                    <div class="flex items-center gap-1.5 text-sm">
                      <div class="w-6 h-6 rounded bg-droppoint-100 flex items-center justify-center">
                        <i class="fas fa-map-marker-alt text-xs text-droppoint-600"></i>
                      </div>
                      <span class="font-medium text-slate-700">{{ event.dropPoints?.length || 0 }} points</span>
                    </div>
                  </div>
                  
                  <div class="flex gap-2">
                    <button
                      @click="viewEvent(event.eventId!)"
                      class="flex-1 py-2 bg-event-50 text-event-600 rounded-lg font-medium
                             hover:bg-event-100 transition-colors text-sm"
                    >
                      <i class="fas fa-eye mr-1.5"></i>
                      View
                    </button>
                    <button
                      @click="editEvent(event.eventId!)"
                      class="flex-1 py-2 bg-slate-100 text-slate-700 rounded-lg font-medium
                             hover:bg-slate-200 transition-colors text-sm"
                    >
                      <i class="fas fa-edit mr-1.5"></i>
                      Edit
                    </button>
                    <button
                      @click="deleteEventConfirm(event.eventId!)"
                      class="py-2 px-3 bg-slate-100 text-red-500 rounded-lg font-medium
                             hover:bg-red-50 transition-colors text-sm"
                    >
                      <i class="fas fa-trash-alt"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Completed Events (Collapsed Table) -->
          <div v-if="pastEvents.length > 0">
            <h2 class="text-lg font-semibold text-slate-900 mb-4">Past Events</h2>
            <div class="card overflow-hidden">
              <div class="overflow-x-auto">
                <table class="table-modern">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Date</th>
                      <th>Location</th>
                      <th>Duration</th>
                      <th>Bars</th>
                      <th>Drop Points</th>
                      <th class="text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="event in pastEvents" :key="event.eventId">
                      <td class="font-medium text-slate-900">{{ event.name }}</td>
                      <td>{{ formatDate(event.date) }}</td>
                      <td>{{ event.location }}</td>
                      <td>{{ event.duration }}h</td>
                      <td>{{ event.bars?.length || 0 }}</td>
                      <td>{{ event.dropPoints?.length || 0 }}</td>
                      <td>
                        <div class="flex items-center justify-end gap-2">
                          <button
                            @click="viewEvent(event.eventId!)"
                            class="p-2 rounded-lg text-slate-500 hover:text-event-600 
                                   hover:bg-event-50 transition-colors"
                          >
                            <i class="fas fa-eye"></i>
                          </button>
                          <button
                            @click="deleteEventConfirm(event.eventId!)"
                            class="p-2 rounded-lg text-slate-500 hover:text-red-600 
                                   hover:bg-red-50 transition-colors"
                          >
                            <i class="fas fa-trash-alt"></i>
                          </button>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="card p-12 text-center">
          <div class="w-20 h-20 rounded-2xl bg-event-100 flex items-center justify-center mx-auto mb-6">
            <i class="fas fa-calendar-alt text-3xl text-event-400"></i>
          </div>
          <h3 class="text-xl font-semibold text-slate-900 mb-2">No events yet</h3>
          <p class="text-slate-500 mb-6 max-w-sm mx-auto">
            Create your first event to start planning bars and drop points.
          </p>
          <button @click="navigateToCreate" class="btn-primary">
            <i class="fas fa-plus"></i>
            <span>Create Your First Event</span>
          </button>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useEventStore } from '@/stores/eventStore';
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';
import Sidebar from '@/components/common/Sidebar.vue';
import Navbar from '@/components/common/Navbar.vue';

const router = useRouter();
const eventStore = useEventStore();

// Computed
const events = computed(() => eventStore.events);
const loading = computed(() => eventStore.loading);
const error = computed(() => eventStore.error);

const plannedCount = computed(() => events.value.filter(e => e.status === 'PLANNED').length);
const ongoingCount = computed(() => events.value.filter(e => e.status === 'ONGOING').length);
const completedCount = computed(() => events.value.filter(e => e.status === 'COMPLETED').length);

const upcomingEvents = computed(() => 
  events.value.filter(e => e.status === 'PLANNED' || e.status === 'ONGOING')
);

const pastEvents = computed(() => 
  events.value.filter(e => e.status === 'COMPLETED')
);

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
  return new Date(date).toLocaleDateString('en-US', {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};

const getStatusBadgeClass = (status: string) => {
  switch (status) {
    case 'PLANNED': return 'bg-blue-500/20 text-white';
    case 'ONGOING': return 'bg-amber-500/20 text-white';
    case 'COMPLETED': return 'bg-emerald-500/20 text-white';
    default: return 'bg-white/20 text-white';
  }
};

// Lifecycle
onMounted(() => {
  eventStore.fetchEvents();
});
</script>

<style scoped>
/* Minimal scoped styles - most styling via Tailwind utilities */
</style>
