<template>
  <span 
    class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold uppercase tracking-wide"
    :class="badgeClass"
  >
    <i v-if="showIcon" :class="iconClass" class="text-[10px]"></i>
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { EventStatus } from '@/api/eventApi';

interface Props {
  status: EventStatus;
  variant?: 'default' | 'light' | 'outline';
  showIcon?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
  showIcon: true,
});

const label = computed(() => {
  switch (props.status) {
    case 'SCHEDULED':
      return 'Scheduled';
    case 'RUNNING':
      return 'Running';
    case 'COMPLETED':
      return 'Completed';
    case 'CANCELLED':
      return 'Cancelled';
    default:
      return props.status;
  }
});

const iconClass = computed(() => {
  switch (props.status) {
    case 'SCHEDULED':
      return 'fas fa-clock';
    case 'RUNNING':
      return 'fas fa-play';
    case 'COMPLETED':
      return 'fas fa-check';
    case 'CANCELLED':
      return 'fas fa-ban';
    default:
      return 'fas fa-circle';
  }
});

const badgeClass = computed(() => {
  const statusStyles: Record<EventStatus, Record<string, string>> = {
    SCHEDULED: {
      default: 'bg-blue-100 text-blue-700',
      light: 'bg-white/20 text-white backdrop-blur-sm',
      outline: 'border-2 border-blue-500 text-blue-600 bg-transparent',
    },
    RUNNING: {
      default: 'bg-amber-100 text-amber-700',
      light: 'bg-white/20 text-white backdrop-blur-sm',
      outline: 'border-2 border-amber-500 text-amber-600 bg-transparent',
    },
    COMPLETED: {
      default: 'bg-emerald-100 text-emerald-700',
      light: 'bg-white/20 text-white backdrop-blur-sm',
      outline: 'border-2 border-emerald-500 text-emerald-600 bg-transparent',
    },
    CANCELLED: {
      default: 'bg-red-100 text-red-700',
      light: 'bg-white/20 text-white backdrop-blur-sm',
      outline: 'border-2 border-red-500 text-red-600 bg-transparent',
    },
  };

  return statusStyles[props.status]?.[props.variant] || statusStyles.SCHEDULED.default;
});
</script>
