<template>
  <div class="card group card-interactive overflow-hidden">
    <!-- Card Header with Gradient -->
    <div 
      class="h-24 relative p-5"
      :class="headerGradient"
    >
      <!-- Status Badge -->
      <div class="absolute top-4 right-4">
        <StatusBadge :status="event.status" variant="light" />
      </div>
      
      <!-- Icon -->
      <div 
        class="w-12 h-12 rounded-xl flex items-center justify-center"
        :class="iconClass"
      >
        <i :class="statusIcon" class="text-lg"></i>
      </div>
      
      <!-- Live indicator for running events -->
      <div 
        v-if="event.status === 'RUNNING'"
        class="absolute bottom-4 right-4 flex items-center gap-1.5"
      >
        <span class="relative flex h-2.5 w-2.5">
          <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-white opacity-75"></span>
          <span class="relative inline-flex rounded-full h-2.5 w-2.5 bg-white"></span>
        </span>
        <span class="text-xs font-semibold text-white/90">LIVE</span>
      </div>
    </div>
    
    <!-- Card Body -->
    <div class="p-5">
      <!-- Title & Organizer -->
      <h3 class="text-lg font-bold text-slate-900 mb-1 group-hover:text-event-600 transition-colors line-clamp-1">
        {{ event.name }}
      </h3>
      <p v-if="event.organizerName" class="text-sm text-slate-500 mb-4">
        by {{ event.organizerName }}
      </p>
      
      <!-- Event Info -->
      <div class="space-y-2.5 text-sm text-slate-600 mb-5">
        <div class="flex items-center gap-2.5">
          <i class="fas fa-calendar w-4 text-slate-400"></i>
          <span>{{ formatDate(event.date) }}</span>
        </div>
        <div v-if="event.location" class="flex items-center gap-2.5">
          <i class="fas fa-map-marker-alt w-4 text-slate-400"></i>
          <span class="line-clamp-1">{{ event.location }}</span>
        </div>
      </div>

      <!-- Resources Summary -->
      <div class="flex items-center gap-4 mb-5 pt-4 border-t border-slate-100">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 rounded-lg bg-bar-100 flex items-center justify-center">
            <i class="fas fa-glass-martini text-sm text-bar-600"></i>
          </div>
          <div>
            <p class="text-sm font-semibold text-slate-900">{{ event.barCount }}</p>
            <p class="text-xs text-slate-500">Bars</p>
          </div>
        </div>
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 rounded-lg bg-droppoint-100 flex items-center justify-center">
            <i class="fas fa-map-marker-alt text-sm text-droppoint-600"></i>
          </div>
          <div>
            <p class="text-sm font-semibold text-slate-900">{{ event.dropPointCount }}</p>
            <p class="text-xs text-slate-500">Points</p>
          </div>
        </div>
        <div v-if="event.isPublic" class="ml-auto">
          <span class="badge badge-info text-xs">
            <i class="fas fa-globe mr-1"></i>
            Public
          </span>
        </div>
      </div>
      
      <!-- Actions -->
      <div class="flex gap-2">
        <button
          @click.stop="$emit('view', event.id)"
          class="flex-1 py-2.5 bg-event-50 text-event-600 rounded-xl font-semibold
                 hover:bg-event-100 transition-colors text-sm"
        >
          <i class="fas fa-eye mr-1.5"></i>
          View
        </button>
        
        <!-- Start Event (only for SCHEDULED) -->
        <button
          v-if="event.status === 'SCHEDULED'"
          @click.stop="$emit('start', event.id)"
          class="flex-1 py-2.5 bg-emerald-50 text-emerald-600 rounded-xl font-semibold
                 hover:bg-emerald-100 transition-colors text-sm"
        >
          <i class="fas fa-play mr-1.5"></i>
          Start
        </button>
        
        <!-- Complete Event (only for RUNNING) -->
        <button
          v-if="event.status === 'RUNNING'"
          @click.stop="$emit('complete', event.id)"
          class="flex-1 py-2.5 bg-blue-50 text-blue-600 rounded-xl font-semibold
                 hover:bg-blue-100 transition-colors text-sm"
        >
          <i class="fas fa-check mr-1.5"></i>
          Complete
        </button>
        
        <!-- Edit (only for SCHEDULED) -->
        <button
          v-if="event.status === 'SCHEDULED'"
          @click.stop="$emit('edit', event.id)"
          class="py-2.5 px-3 bg-slate-100 text-slate-700 rounded-xl font-semibold
                 hover:bg-slate-200 transition-colors text-sm"
          title="Edit Event"
        >
          <i class="fas fa-edit"></i>
        </button>
        
        <!-- Delete (only for SCHEDULED) -->
        <button
          v-if="event.status === 'SCHEDULED'"
          @click.stop="$emit('delete', event.id)"
          class="py-2.5 px-3 bg-slate-100 text-red-500 rounded-xl font-semibold
                 hover:bg-red-50 transition-colors text-sm"
          title="Delete Event"
        >
          <i class="fas fa-trash-alt"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { EventSummary } from '@/api/eventApi';
import { formatDate } from '@/composables/useDateFormat';
import StatusBadge from './StatusBadge.vue';

interface Props {
  event: EventSummary;
}

const props = defineProps<Props>();

defineEmits<{
  view: [id: number];
  edit: [id: number];
  start: [id: number];
  complete: [id: number];
  delete: [id: number];
}>();

const headerGradient = computed(() => {
  switch (props.event.status) {
    case 'RUNNING':
      return 'bg-gradient-to-br from-amber-500 to-orange-600';
    case 'COMPLETED':
      return 'bg-gradient-to-br from-emerald-500 to-teal-600';
    case 'CANCELLED':
      return 'bg-gradient-to-br from-slate-500 to-slate-700';
    case 'SCHEDULED':
    default:
      return 'bg-gradient-to-br from-event-500 to-event-700';
  }
});

const iconClass = computed(() => {
  switch (props.event.status) {
    case 'RUNNING':
      return 'bg-white/20 backdrop-blur-sm text-white';
    case 'COMPLETED':
      return 'bg-white/20 backdrop-blur-sm text-white';
    case 'CANCELLED':
      return 'bg-white/20 backdrop-blur-sm text-white';
    case 'SCHEDULED':
    default:
      return 'bg-white/20 backdrop-blur-sm text-white';
  }
});

const statusIcon = computed(() => {
  switch (props.event.status) {
    case 'RUNNING':
      return 'fas fa-play';
    case 'COMPLETED':
      return 'fas fa-check';
    case 'CANCELLED':
      return 'fas fa-ban';
    case 'SCHEDULED':
    default:
      return 'fas fa-calendar-alt';
  }
});

</script>
