<template>
  <Transition name="modal">
    <div
      v-if="isOpen"
      class="modal-overlay"
      @click.self="$emit('close')"
    >
      <div class="modal-content">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-bar-100 flex items-center justify-center">
              <i class="fas fa-glass-martini text-bar-600"></i>
            </div>
            <h2 class="text-lg font-semibold text-slate-900">Add New Bar</h2>
          </div>
          <button 
            @click="$emit('close')" 
            class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>

        <form @submit.prevent="addBar" class="p-6 space-y-5">
          <div>
            <label for="barName" class="label">Bar Name</label>
            <input
              v-model="formData.name"
              id="barName"
              type="text"
              class="input"
              placeholder="e.g., Main Stage Bar"
              required
            />
          </div>

          <div>
            <label for="barLocation" class="label">Location</label>
            <input
              v-model="formData.location"
              id="barLocation"
              type="text"
              class="input"
              placeholder="e.g., North Wing"
              required
            />
          </div>

          <div>
            <label for="barStockCapacity" class="label">Stock Capacity</label>
            <input
              v-model.number="formData.maxCapacity"
              id="barStockCapacity"
              type="number"
              class="input"
              placeholder="Maximum items the bar can hold"
              required
              min="1"
              max="1000"
            />
          </div>

          <div class="flex gap-3 pt-2">
            <button type="submit" class="btn-primary flex-1">
              <i class="fas fa-plus"></i>
              <span>Create Bar</span>
            </button>
            <button 
              type="button" 
              @click="$emit('close')" 
              class="btn-secondary flex-1"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue';
import { barApi } from '@/api';
import type { CreateBarPayload } from '@/api/types';

const props = defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'bar-added'): void;
}>();

const formData = reactive<CreateBarPayload>({
  name: '',
  location: '',
  maxCapacity: 100,
});

// Reset form when modal opens
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    formData.name = '';
    formData.location = '';
    formData.maxCapacity = 100;
  }
});

async function addBar() {
  if (formData.name && formData.location && formData.maxCapacity > 0) {
    try {
      await barApi.addBar({
        name: formData.name,
        location: formData.location,
        maxCapacity: formData.maxCapacity,
      });
      emit('close');
      emit('bar-added');
    } catch (error) {
      console.error('Error adding bar:', error);
    }
  }
}
</script>

<style scoped>
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.95);
}
</style>
