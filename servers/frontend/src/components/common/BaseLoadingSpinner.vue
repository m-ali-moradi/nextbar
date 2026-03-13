<template>
  <div class="flex items-center justify-center py-20">
    <div class="text-center">
      <div
        class="rounded-full animate-spin mx-auto mb-4"
        :class="[sizeClasses, colorClasses]"
      />
      <p v-if="message" class="text-slate-500">{{ message }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  /** Tailwind color name (e.g. 'bar', 'warehouse', 'primary') */
  color?: string;
  message?: string;
  size?: 'sm' | 'md' | 'lg';
}>(), {
  color: 'primary',
  message: '',
  size: 'md',
});

const SIZE_MAP: Record<string, string> = {
  sm: 'w-8 h-8 border-[3px]',
  md: 'w-12 h-12 border-4',
  lg: 'w-16 h-16 border-4',
};

const COLOR_MAP: Record<string, string> = {
  primary: 'border-primary-200 border-t-primary-600',
  bar: 'border-bar-200 border-t-bar-600',
  droppoint: 'border-droppoint-200 border-t-droppoint-600',
  event: 'border-event-200 border-t-event-600',
  warehouse: 'border-warehouse-200 border-t-warehouse-600',
  slate: 'border-slate-200 border-t-slate-600',
};

const sizeClasses = computed(() => SIZE_MAP[props.size] ?? SIZE_MAP.md);
const colorClasses = computed(() => COLOR_MAP[props.color] ?? COLOR_MAP.primary);
</script>
