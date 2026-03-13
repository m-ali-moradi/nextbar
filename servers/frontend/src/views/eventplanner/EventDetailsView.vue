<template>
  <div>
        <!-- Loading State -->
        <BaseLoadingSpinner v-if="loading && !event" color="event" size="lg" message="Loading event details..." />

        <!-- Event Not Found -->
        <div v-else-if="!event && !loading" class="card p-12 text-center">
          <div class="w-20 h-20 rounded-2xl bg-red-100 flex items-center justify-center mx-auto mb-6">
            <i class="fas fa-calendar-times text-3xl text-red-600"></i>
          </div>
          <h3 class="text-xl font-semibold text-slate-900 mb-2">Event Not Found</h3>
          <p class="text-slate-500 mb-6">The event you're looking for doesn't exist or has been removed.</p>
          <button @click="goBack" class="btn-primary">
            <i class="fas fa-arrow-left"></i>
            <span>Back to Events</span>
          </button>
        </div>

        <!-- Event Details -->
        <template v-else-if="event">
          <!-- Page Header -->
          <div class="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4 mb-8">
            <div class="flex items-start gap-4">
              <button 
                @click="goBack"
                class="p-3 rounded-xl bg-white border border-slate-200 text-slate-600 hover:bg-slate-50 transition-colors"
              >
                <i class="fas fa-arrow-left"></i>
              </button>
              <div>
                <div class="flex items-center gap-3 mb-1">
                  <h1 class="text-3xl font-bold text-slate-900">{{ event.name }}</h1>
                  <StatusBadge :status="event.status" />
                </div>
                <p class="text-slate-500">
                  <span v-if="event.organizerName">Organized by {{ event.organizerName }}</span>
                  <span v-if="event.organizerName && event.location"> • </span>
                  <span v-if="event.location">{{ event.location }}</span>
                </p>
              </div>
            </div>
            
            <!-- Event Actions -->
            <div class="flex flex-wrap gap-3">
              <button
                v-if="event.status === 'SCHEDULED'"
                @click="editEvent"
                class="btn-secondary"
              >
                <i class="fas fa-edit"></i>
                <span>Edit Event</span>
              </button>
              <button
                v-if="event.status === 'SCHEDULED'"
                @click="confirmStartEvent"
                class="btn-success"
              >
                <i class="fas fa-play"></i>
                <span>Start Event</span>
              </button>
              <button
                v-if="event.status === 'RUNNING'"
                @click="confirmCompleteEvent"
                class="btn-primary"
              >
                <i class="fas fa-check"></i>
                <span>Complete Event</span>
              </button>
              <button
                v-if="event.status === 'RUNNING'"
                @click="confirmCancelEvent"
                class="btn-danger"
              >
                <i class="fas fa-ban"></i>
                <span>Cancel Event</span>
              </button>
            </div>
          </div>

          <!-- Overview Cards -->
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
            <!-- Event Info Card -->
            <div class="card p-6">
              <h3 class="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2">
                <i class="fas fa-info-circle text-event-500"></i>
                Event Details
              </h3>
              <div class="space-y-4">
                <div class="flex justify-between items-center py-2 border-b border-slate-100">
                  <span class="text-slate-500">Date</span>
                  <span class="font-semibold text-slate-900">{{ formatDate(event.date) }}</span>
                </div>
                <div class="flex justify-between items-center py-2 border-b border-slate-100">
                  <span class="text-slate-500">Location</span>
                  <span class="font-semibold text-slate-900">{{ event.location || 'Not set' }}</span>
                </div>
                <div class="flex justify-between items-center py-2 border-b border-slate-100">
                  <span class="text-slate-500">Expected Attendees</span>
                  <span class="font-semibold text-slate-900">{{ event.attendeesCount ?? 'N/A' }}</span>
                </div>
                <div class="flex justify-between items-center py-2 border-b border-slate-100">
                  <span class="text-slate-500">Max Capacity</span>
                  <span class="font-semibold text-slate-900">{{ event.maxAttendees ?? 'N/A' }}</span>
                </div>
                <div class="flex justify-between items-center py-2">
                  <span class="text-slate-500">Visibility</span>
                  <span class="badge" :class="event.isPublic ? 'badge-info' : 'badge-neutral'">
                    {{ event.isPublic ? 'Public' : 'Private' }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Organizer Card -->
            <div class="card p-6">
              <h3 class="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2">
                <i class="fas fa-user text-event-500"></i>
                Organizer
              </h3>
              <div class="space-y-4">
                <div class="flex items-center gap-4">
                  <div class="w-14 h-14 rounded-xl bg-event-100 flex items-center justify-center">
                    <i class="fas fa-user text-xl text-event-600"></i>
                  </div>
                  <div>
                    <p class="font-semibold text-slate-900">{{ event.organizerName || 'Not specified' }}</p>
                    <p class="text-sm text-slate-500">Event Organizer</p>
                  </div>
                </div>
                <div v-if="event.organizerEmail" class="flex items-center gap-3 text-sm">
                  <i class="fas fa-envelope w-4 text-slate-400"></i>
                  <span class="text-slate-600">{{ event.organizerEmail }}</span>
                </div>
                <div v-if="event.organizerPhone" class="flex items-center gap-3 text-sm">
                  <i class="fas fa-phone w-4 text-slate-400"></i>
                  <span class="text-slate-600">{{ event.organizerPhone }}</span>
                </div>
              </div>
            </div>

            <!-- Status Timeline -->
            <div class="card p-6">
              <h3 class="text-lg font-bold text-slate-900 mb-4 flex items-center gap-2">
                <i class="fas fa-stream text-event-500"></i>
                Event Progress
              </h3>
              <div class="relative">
                <!-- Timeline -->
                <div class="absolute left-4 top-0 bottom-0 w-0.5 bg-slate-200"></div>
                
                <div class="space-y-6">
                  <!-- Created Step -->
                  <div class="relative flex items-center gap-4">
                    <div 
                      class="w-8 h-8 rounded-full flex items-center justify-center z-10"
                      :class="'bg-emerald-500 text-white'"
                    >
                      <i class="fas fa-check text-xs"></i>
                    </div>
                    <div>
                      <p class="font-semibold text-slate-900">Created</p>
                      <p class="text-sm text-slate-500">{{ formatDate(event.createdAt) }}</p>
                    </div>
                  </div>
                  
                  <!-- Scheduled Step -->
                  <div class="relative flex items-center gap-4">
                    <div 
                      class="w-8 h-8 rounded-full flex items-center justify-center z-10"
                      :class="getTimelineStepClass('SCHEDULED')"
                    >
                      <i class="fas" :class="event.status !== 'SCHEDULED' || isAfterScheduled ? 'fa-check text-xs' : 'fa-clock text-xs'"></i>
                    </div>
                    <div>
                      <p class="font-semibold text-slate-900">Scheduled</p>
                      <p class="text-sm text-slate-500">{{ formatDate(event.date) }}</p>
                    </div>
                  </div>
                  
                  <!-- Running Step -->
                  <div class="relative flex items-center gap-4">
                    <div 
                      class="w-8 h-8 rounded-full flex items-center justify-center z-10"
                      :class="getTimelineStepClass('RUNNING')"
                    >
                      <i class="fas" :class="event.status === 'RUNNING' ? 'fa-play text-xs' : (event.status === 'COMPLETED' ? 'fa-check text-xs' : 'fa-circle text-xs')"></i>
                    </div>
                    <div>
                      <p class="font-semibold" :class="event.status === 'RUNNING' ? 'text-amber-600' : 'text-slate-900'">
                        {{ event.status === 'RUNNING' ? 'In Progress' : 'Started' }}
                      </p>
                      <p class="text-sm text-slate-500">{{ event.status === 'RUNNING' ? 'Active now' : 'Pending' }}</p>
                    </div>
                  </div>
                  
                  <!-- Completed Step -->
                  <div class="relative flex items-center gap-4">
                    <div 
                      class="w-8 h-8 rounded-full flex items-center justify-center z-10"
                      :class="getTimelineStepClass('COMPLETED')"
                    >
                      <i class="fas fa-flag-checkered text-xs"></i>
                    </div>
                    <div>
                      <p class="font-semibold text-slate-900">Completed</p>
                      <p class="text-sm text-slate-500">{{ event.status === 'COMPLETED' ? formatDate(event.updatedAt) : 'Not yet' }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Description -->
          <div v-if="event.description" class="card p-6 mb-8">
            <h3 class="text-lg font-bold text-slate-900 mb-3">Description</h3>
            <p class="text-slate-600 leading-relaxed">{{ event.description }}</p>
          </div>

          <!-- Tabs for Bars & Drop Points -->
          <div class="tabs mb-6">
            <button
              @click="activeTab = 'bars'"
              class="tab"
              :class="activeTab === 'bars' && 'tab-active'"
            >
              <i class="fas fa-glass-martini mr-2"></i>
              Bars ({{ event.bars?.length || 0 }})
            </button>
            <button
              @click="activeTab = 'droppoints'"
              class="tab"
              :class="activeTab === 'droppoints' && 'tab-active'"
            >
              <i class="fas fa-map-marker-alt mr-2"></i>
              Drop Points ({{ event.dropPoints?.length || 0 }})
            </button>
          </div>

          <!-- Bars Tab -->
          <div v-if="activeTab === 'bars'" class="animate-fade-in">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-bold text-slate-900">Bars</h3>
              <button
                v-if="event.status === 'SCHEDULED'"
                @click="selectedBarForEdit = null; showAddBarModal = true"
                class="btn-secondary text-sm"
              >
                <i class="fas fa-plus"></i>
                <span>Add Bar</span>
              </button>
            </div>

            <div v-if="event.bars && event.bars.length > 0" class="card overflow-hidden">
              <table class="table-modern">
                <thead>
                  <tr>
                    <th>Bar Name</th>
                    <th>Location</th>
                    <th>Assigned Staff</th>
                    <th>Status</th>
                    <th>Stock Items</th>
                    <th class="text-right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="bar in event.bars" :key="bar.id">
                    <td>
                      <div class="flex items-center gap-3">
                        <div class="w-10 h-10 rounded-lg bg-bar-100 flex items-center justify-center">
                          <i class="fas fa-glass-martini text-bar-600"></i>
                        </div>
                        <span class="font-semibold text-slate-900">{{ bar.name }}</span>
                      </div>
                    </td>
                    <td>{{ bar.location || 'Not set' }}</td>
                    <td>
                      <span v-if="bar.assignedStaff" class="badge badge-info">
                        <i class="fas fa-user mr-1"></i>
                        {{ bar.assignedStaff }}
                      </span>
                      <span v-else class="text-slate-400">Unassigned</span>
                    </td>
                    <td>
                      <span 
                        class="badge"
                        :class="bar.active ? 'badge-success' : 'badge-neutral'"
                      >
                        {{ bar.active ? 'Active' : 'Inactive' }}
                      </span>
                    </td>
                    <td>
                      <span class="text-slate-600">{{ bar.stockCount || 0 }} items</span>
                    </td>
                    <td>
                      <div class="flex items-center justify-end gap-2">
                        <button
                          @click="viewBarStocks(bar)"
                          class="p-2 rounded-lg text-slate-500 hover:text-bar-600 hover:bg-bar-50 transition-colors"
                          title="View Stocks"
                        >
                          <i class="fas fa-boxes"></i>
                        </button>
                        <button
                          v-if="event.status === 'SCHEDULED'"
                          @click="editBar(bar)"
                          class="p-2 rounded-lg text-slate-500 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                          title="Edit"
                        >
                          <i class="fas fa-edit"></i>
                        </button>
                        <button
                          v-if="event.status === 'SCHEDULED'"
                          @click="confirmDeleteBar(bar.id)"
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

            <!-- Empty Bars State -->
            <div v-else class="card p-12 text-center">
              <div class="w-16 h-16 rounded-2xl bg-bar-100 flex items-center justify-center mx-auto mb-4">
                <i class="fas fa-glass-martini text-2xl text-bar-400"></i>
              </div>
              <h4 class="text-lg font-semibold text-slate-900 mb-2">No bars configured</h4>
              <p class="text-slate-500 mb-4">Add bars to this event to get started.</p>
              <button
                v-if="event.status === 'SCHEDULED'"
                @click="selectedBarForEdit = null; showAddBarModal = true"
                class="btn-primary"
              >
                <i class="fas fa-plus"></i>
                <span>Add First Bar</span>
              </button>
            </div>
          </div>

          <!-- Drop Points Tab -->
          <div v-if="activeTab === 'droppoints'" class="animate-fade-in">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-bold text-slate-900">Drop Points</h3>
              <button
                v-if="event.status === 'SCHEDULED'"
                @click="selectedDropPointForEdit = null; showAddDropPointModal = true"
                class="btn-secondary text-sm"
              >
                <i class="fas fa-plus"></i>
                <span>Add Drop Point</span>
              </button>
            </div>

            <div v-if="event.dropPoints && event.dropPoints.length > 0" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
              <div
                v-for="dp in event.dropPoints"
                :key="dp.id"
                class="card p-5 card-interactive"
              >
                <div class="flex items-start justify-between mb-4">
                  <div class="flex items-center gap-3">
                    <div class="w-12 h-12 rounded-xl bg-droppoint-100 flex items-center justify-center">
                      <i class="fas fa-map-marker-alt text-lg text-droppoint-600"></i>
                    </div>
                    <div>
                      <h4 class="font-semibold text-slate-900">{{ dp.name }}</h4>
                      <p class="text-sm text-slate-500">{{ dp.location || 'No location' }}</p>
                    </div>
                  </div>
                  <div v-if="event.status === 'SCHEDULED'" class="flex gap-1">
                    <button
                      @click="editDropPoint(dp)"
                      class="p-2 rounded-lg text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                    >
                      <i class="fas fa-edit text-sm"></i>
                    </button>
                    <button
                      @click="confirmDeleteDropPoint(dp.id)"
                      class="p-2 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors"
                    >
                      <i class="fas fa-trash-alt text-sm"></i>
                    </button>
                  </div>
                </div>
                
                <div class="space-y-2 text-sm">
                  <div class="flex justify-between">
                    <span class="text-slate-500">Capacity</span>
                    <span class="font-medium text-slate-900">{{ dp.capacity ?? 0 }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-slate-500">Staff</span>
                    <span v-if="dp.assignedStaff" class="font-medium text-slate-900">
                      <i class="fas fa-user text-droppoint-500 mr-1"></i>
                      {{ dp.assignedStaff }}
                    </span>
                    <span v-else class="text-slate-400">Unassigned</span>
                  </div>
                  <div v-if="dp.eventOccupancy !== undefined" class="flex justify-between">
                    <span class="text-slate-500">Occupancy</span>
                    <span class="font-medium text-slate-900">{{ dp.eventOccupancy }}%</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Empty Drop Points State -->
            <div v-else class="card p-12 text-center">
              <div class="w-16 h-16 rounded-2xl bg-droppoint-100 flex items-center justify-center mx-auto mb-4">
                <i class="fas fa-map-marker-alt text-2xl text-droppoint-400"></i>
              </div>
              <h4 class="text-lg font-semibold text-slate-900 mb-2">No drop points configured</h4>
              <p class="text-slate-500 mb-4">Add drop points for attendee collection areas.</p>
              <button
                v-if="event.status === 'SCHEDULED'"
                @click="selectedDropPointForEdit = null; showAddDropPointModal = true"
                class="btn-primary"
              >
                <i class="fas fa-plus"></i>
                <span>Add First Drop Point</span>
              </button>
            </div>
          </div>
        </template>

    <!-- Confirm Modal -->
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

    <!-- Add Bar Modal -->
    <AddBarModal
      v-if="showAddBarModal"
      :event-id="Number(route.params.id)"
      :bar="selectedBarForEdit"
      @close="closeBarModal"
      @saved="onBarSaved"
    />

    <!-- Add Drop Point Modal -->
    <AddDropPointModal
      v-if="showAddDropPointModal"
      :event-id="Number(route.params.id)"
      :drop-point="selectedDropPointForEdit"
      @close="closeDropPointModal"
      @saved="onDropPointSaved"
    />

    <!-- Bar Stock Modal -->
    <BarStockModal
      v-if="showBarStockModal && selectedBarForStock"
      :bar="selectedBarForStock"
      @close="showBarStockModal = false; selectedBarForStock = null"
      @updated="refreshEvent"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useQueryClient } from '@tanstack/vue-query';
import {
  useEventQuery,
  useStartEvent,
  useCompleteEvent,
  useCancelEvent,
  useDeleteEventBar,
  useDeleteEventDropPoint,
} from '@/composables/queries/useEventQueries';
import { queryKeys } from '@/composables/queries/queryKeys';
import { EventStatus, EventBar, EventDropPoint } from '@/api/eventApi';
import { toast } from 'vue3-toastify';
import { formatDate } from '@/composables/useDateFormat';
import StatusBadge from '@/components/eventplanner/StatusBadge.vue';
import ConfirmModal from '@/components/eventplanner/ConfirmModal.vue';
import AddBarModal from '@/components/eventplanner/AddBarModal.vue';
import AddDropPointModal from '@/components/eventplanner/AddDropPointModal.vue';
import BarStockModal from '@/components/eventplanner/BarStockModal.vue';
import BaseLoadingSpinner from '@/components/common/BaseLoadingSpinner.vue';

const route = useRoute();
const router = useRouter();
const queryClient = useQueryClient();

// ============ Vue Query ============
const eventId = computed(() => Number(route.params.id));
const { data: event, isLoading: loading, refetch: refetchEvent } = useEventQuery(eventId);

// ============ Mutations ============
const startEventMutation = useStartEvent();
const completeEventMutation = useCompleteEvent();
const cancelEventMutation = useCancelEvent();
const deleteBarMutation = useDeleteEventBar();
const deleteDropPointMutation = useDeleteEventDropPoint();

// ============ State ============
const activeTab = ref<'bars' | 'droppoints'>('bars');
const showAddBarModal = ref(false);
const showAddDropPointModal = ref(false);
const showBarStockModal = ref(false);
const selectedBarForEdit = ref<EventBar | null>(null);
const selectedBarForStock = ref<EventBar | null>(null);
const selectedDropPointForEdit = ref<EventDropPoint | null>(null);
const showConfirmModal = ref(false);
const confirmModal = ref({
  title: '',
  message: '',
  confirmText: 'Confirm',
  confirmClass: 'btn-primary',
  loading: false,
  onConfirm: () => {},
});

// ============ Computed ============
const isAfterScheduled = computed(() => {
  const s = event.value?.status;
  return s === 'RUNNING' || s === 'COMPLETED' || s === 'CANCELLED';
});

// ============ Methods ============
const goBack = () => {
  router.push('/events');
};

const editEvent = () => {
  router.push(`/events/${route.params.id}/edit`);
};

const getTimelineStepClass = (step: EventStatus) => {
  const statusOrder: EventStatus[] = ['SCHEDULED', 'RUNNING', 'COMPLETED'];
  const currentIndex = statusOrder.indexOf(event.value?.status || 'SCHEDULED');
  const stepIndex = statusOrder.indexOf(step);

  if (event.value?.status === 'CANCELLED') {
    return 'bg-slate-300 text-white';
  }
  
  if (stepIndex < currentIndex) {
    return 'bg-emerald-500 text-white';
  } else if (stepIndex === currentIndex) {
    if (step === 'RUNNING') return 'bg-amber-500 text-white';
    if (step === 'COMPLETED') return 'bg-emerald-500 text-white';
    return 'bg-blue-500 text-white';
  }
  return 'bg-slate-300 text-white';
};

// Confirmation handlers
const closeConfirmModal = () => {
  showConfirmModal.value = false;
  confirmModal.value.loading = false;
};

const confirmStartEvent = () => {
  confirmModal.value = {
    title: 'Start Event',
    message: 'Are you sure you want to start this event? All bars and drop points will be activated.',
    confirmText: 'Start Event',
    confirmClass: 'btn-success',
    loading: false,
    onConfirm: async () => {
      confirmModal.value.loading = true;
      try {
        await startEventMutation.mutateAsync(eventId.value);
        toast.success('Event started successfully!');
        closeConfirmModal();
      } catch (err) {
        toast.error(err instanceof Error ? err.message : 'Failed to start event');
        confirmModal.value.loading = false;
      }
    },
  };
  showConfirmModal.value = true;
};

const confirmCompleteEvent = () => {
  confirmModal.value = {
    title: 'Complete Event',
    message: 'Mark this event as completed? This action cannot be undone.',
    confirmText: 'Complete',
    confirmClass: 'btn-primary',
    loading: false,
    onConfirm: async () => {
      confirmModal.value.loading = true;
      try {
        await completeEventMutation.mutateAsync(eventId.value);
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

const confirmCancelEvent = () => {
  confirmModal.value = {
    title: 'Cancel Event',
    message: 'Are you sure you want to cancel this event? This action cannot be undone.',
    confirmText: 'Cancel Event',
    confirmClass: 'btn-danger',
    loading: false,
    onConfirm: async () => {
      confirmModal.value.loading = true;
      try {
        await cancelEventMutation.mutateAsync(eventId.value);
        toast.success('Event cancelled');
        closeConfirmModal();
      } catch (err) {
        toast.error('Failed to cancel event');
        confirmModal.value.loading = false;
      }
    },
  };
  showConfirmModal.value = true;
};

// Bar handlers
const viewBarStocks = (bar: EventBar) => {
  selectedBarForStock.value = bar;
  showBarStockModal.value = true;
};

const refreshEvent = async () => {
  await refetchEvent();
};

const closeBarModal = () => {
  showAddBarModal.value = false;
  selectedBarForEdit.value = null;
};

const editBar = (bar: EventBar) => {
  selectedBarForEdit.value = bar;
  showAddBarModal.value = true;
};

const confirmDeleteBar = (barId: number) => {
  confirmModal.value = {
    title: 'Delete Bar',
    message: 'Are you sure you want to remove this bar from the event?',
    confirmText: 'Delete',
    confirmClass: 'btn-danger',
    loading: false,
    onConfirm: async () => {
      confirmModal.value.loading = true;
      try {
        await deleteBarMutation.mutateAsync(barId);
        toast.success('Bar deleted');
        closeConfirmModal();
        await queryClient.invalidateQueries({ queryKey: queryKeys.events.detail(eventId.value) });
      } catch (err) {
        toast.error('Failed to delete bar');
        confirmModal.value.loading = false;
      }
    },
  };
  showConfirmModal.value = true;
};

const onBarSaved = async () => {
  closeBarModal();
  await refetchEvent();
  toast.success('Bar saved successfully');
};

// Drop Point handlers
const editDropPoint = (dp: EventDropPoint) => {
  selectedDropPointForEdit.value = dp;
  showAddDropPointModal.value = true;
};

const closeDropPointModal = () => {
  showAddDropPointModal.value = false;
  selectedDropPointForEdit.value = null;
};

const confirmDeleteDropPoint = (dpId: number) => {
  confirmModal.value = {
    title: 'Delete Drop Point',
    message: 'Are you sure you want to remove this drop point from the event?',
    confirmText: 'Delete',
    confirmClass: 'btn-danger',
    loading: false,
    onConfirm: async () => {
      confirmModal.value.loading = true;
      try {
        await deleteDropPointMutation.mutateAsync(dpId);
        toast.success('Drop point deleted');
        closeConfirmModal();
        await queryClient.invalidateQueries({ queryKey: queryKeys.events.detail(eventId.value) });
      } catch (err) {
        toast.error('Failed to delete drop point');
        confirmModal.value.loading = false;
      }
    },
  };
  showConfirmModal.value = true;
};

const onDropPointSaved = async () => {
  closeDropPointModal();
  await refetchEvent();
  toast.success('Drop point saved successfully');
};
</script>
