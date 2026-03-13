<template>
  <Transition name="confirm-dialog">
    <div
      v-if="isOpen"
      class="modal-overlay"
      @click.self="handleCancel"
    >
      <div class="modal-content max-w-md">
        <div class="p-6">
          <!-- Icon -->
          <div class="flex items-center justify-center mb-5">
            <div
              class="w-14 h-14 rounded-2xl flex items-center justify-center"
              :class="variant === 'danger'
                ? 'bg-red-100'
                : variant === 'warning'
                  ? 'bg-amber-100'
                  : 'bg-blue-100'"
            >
              <i
                :class="[
                  iconClass || (variant === 'danger' ? 'fas fa-trash-alt' : variant === 'warning' ? 'fas fa-exclamation-triangle' : 'fas fa-question-circle'),
                  'text-2xl',
                  variant === 'danger' ? 'text-red-600' : variant === 'warning' ? 'text-amber-600' : 'text-blue-600'
                ]"
              ></i>
            </div>
          </div>

          <!-- Content -->
          <div class="text-center mb-6">
            <h3 class="text-lg font-bold text-slate-900 mb-2">{{ title }}</h3>
            <p class="text-sm text-slate-500 leading-relaxed">{{ message }}</p>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-3">
            <button
              type="button"
              class="btn-secondary flex-1"
              @click="handleCancel"
              :disabled="loading"
            >
              {{ cancelText }}
            </button>
            <button
              type="button"
              class="flex-1"
              :class="variant === 'danger' ? 'btn-danger' : variant === 'warning' ? 'btn-warning' : 'btn-primary'"
              @click="handleConfirm"
              :disabled="loading"
            >
              <i v-if="loading" class="fas fa-spinner fa-spin"></i>
              <span>{{ confirmText }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
const props = defineProps({
  isOpen: { type: Boolean, default: false },
  title: { type: String, default: 'Are you sure?' },
  message: { type: String, default: 'This action cannot be undone.' },
  confirmText: { type: String, default: 'Confirm' },
  cancelText: { type: String, default: 'Cancel' },
  variant: { type: String, default: 'danger', validator: v => ['danger', 'warning', 'info'].includes(v) },
  iconClass: { type: String, default: '' },
  loading: { type: Boolean, default: false },
});

const emit = defineEmits(['confirm', 'cancel']);

function handleConfirm() {
  emit('confirm');
}

function handleCancel() {
  if (!props.loading) emit('cancel');
}
</script>

<style scoped>
.confirm-dialog-enter-active,
.confirm-dialog-leave-active {
  transition: all 0.25s ease;
}

.confirm-dialog-enter-from,
.confirm-dialog-leave-to {
  opacity: 0;
}

.confirm-dialog-enter-from .modal-content,
.confirm-dialog-leave-to .modal-content {
  transform: scale(0.92) translateY(8px);
}
</style>
