<template>
  <div class="card p-12 text-center">
    <div
      class="w-20 h-20 rounded-2xl flex items-center justify-center mx-auto mb-6"
      :class="iconBgClass"
    >
      <i :class="[icon, 'text-3xl', iconColorClass]" />
    </div>
    <h3 class="text-xl font-semibold text-slate-900 mb-2">{{ title }}</h3>
    <p v-if="description" class="text-slate-500 mb-6 max-w-sm mx-auto">{{ description }}</p>
    <!-- Action slot for an optional button -->
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  icon?: string;
  /** Tailwind color name for icon bg/text (e.g. 'bar', 'slate') */
  color?: string;
  title: string;
  description?: string;
}>(), {
  icon: 'fas fa-inbox',
  color: 'slate',
  description: '',
});

const BG_MAP: Record<string, string> = {
  bar: 'bg-bar-100',
  droppoint: 'bg-droppoint-100',
  event: 'bg-event-100',
  warehouse: 'bg-warehouse-100',
  primary: 'bg-primary-100',
  slate: 'bg-slate-100',
  blue: 'bg-blue-100',
  amber: 'bg-amber-100',
  emerald: 'bg-emerald-100',
  red: 'bg-red-100',
};

const COLOR_MAP: Record<string, string> = {
  bar: 'text-bar-400',
  droppoint: 'text-droppoint-400',
  event: 'text-event-400',
  warehouse: 'text-warehouse-400',
  primary: 'text-primary-400',
  slate: 'text-slate-400',
  blue: 'text-blue-400',
  amber: 'text-amber-400',
  emerald: 'text-emerald-400',
  red: 'text-red-400',
};

const iconBgClass = computed(() => BG_MAP[props.color] ?? 'bg-slate-100');
const iconColorClass = computed(() => COLOR_MAP[props.color] ?? 'text-slate-400');
</script>
