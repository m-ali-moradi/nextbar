<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="$emit('cancel')">
      <div class="modal-content max-w-md animate-scale-in">
        <!-- Header -->
        <div class="modal-header">
          <h3 class="text-xl font-bold text-slate-900">{{ title }}</h3>
          <button 
            @click="$emit('cancel')"
            class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <!-- Body -->
        <div class="modal-body">
          <div class="flex items-start gap-4">
            <div 
              class="flex-shrink-0 w-12 h-12 rounded-xl flex items-center justify-center"
              :class="iconContainerClass"
            >
              <i :class="iconClass" class="text-xl"></i>
            </div>
            <p class="text-slate-600 text-base leading-relaxed pt-2">{{ message }}</p>
          </div>
        </div>
        
        <!-- Footer -->
        <div class="modal-footer justify-end">
          <button 
            @click="$emit('cancel')"
            class="btn-secondary"
            :disabled="loading"
          >
            Cancel
          </button>
          <button 
            @click="$emit('confirm')"
            :class="confirmClass"
            :disabled="loading"
          >
            <span v-if="loading" class="flex items-center gap-2">
              <i class="fas fa-spinner animate-spin"></i>
              Processing...
            </span>
            <span v-else>{{ confirmText }}</span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  title: string;
  message: string;
  confirmText?: string;
  confirmClass?: string;
  loading?: boolean;
  type?: 'info' | 'warning' | 'danger' | 'success';
}

const props = withDefaults(defineProps<Props>(), {
  confirmText: 'Confirm',
  confirmClass: 'btn-primary',
  loading: false,
  type: 'info',
});

defineEmits<{
  confirm: [];
  cancel: [];
}>();

const iconContainerClass = computed(() => {
  if (props.confirmClass?.includes('danger')) return 'bg-red-100 text-red-600';
  if (props.confirmClass?.includes('success')) return 'bg-emerald-100 text-emerald-600';
  if (props.confirmClass?.includes('warning')) return 'bg-amber-100 text-amber-600';
  return 'bg-primary-100 text-primary-600';
});

const iconClass = computed(() => {
  if (props.confirmClass?.includes('danger')) return 'fas fa-exclamation-triangle';
  if (props.confirmClass?.includes('success')) return 'fas fa-check-circle';
  if (props.confirmClass?.includes('warning')) return 'fas fa-exclamation-circle';
  return 'fas fa-info-circle';
});
</script>
