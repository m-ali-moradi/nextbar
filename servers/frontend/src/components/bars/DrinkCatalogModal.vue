<template>
  <Transition name="modal">
    <div 
      v-if="isOpen" 
      class="modal-overlay"
      @click.self="$emit('close')"
    >
      <div class="modal-content max-w-md">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-amber-100 flex items-center justify-center">
              <i class="fas fa-wine-glass-alt text-amber-600"></i>
            </div>
            <div>
              <h2 class="text-lg font-semibold text-slate-900">Drinks Catalog</h2>
              <p class="text-xs text-slate-500">Available beverages for bars</p>
            </div>
          </div>
          <button 
            @click="$emit('close')" 
            class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="p-6">
          <div v-if="products.length > 0" class="space-y-3">
            <div
              v-for="product in products"
              :key="product.id"
              class="flex items-center gap-4 p-4 bg-slate-50 rounded-xl hover:bg-slate-100 transition-colors"
            >
              <div class="w-10 h-10 rounded-lg bg-white shadow-sm flex items-center justify-center">
                <i class="fas fa-wine-bottle text-slate-400"></i>
              </div>
              <div class="flex-1">
                <p class="font-medium text-slate-900">{{ product.name }}</p>
                <p class="text-sm text-slate-500">{{ product.unitType }}</p>
              </div>
              <span class="text-xs font-mono text-slate-400">#{{ product.id }}</span>
            </div>
          </div>

          <div v-else class="text-center py-8">
            <i class="fas fa-wine-glass text-4xl text-slate-300 mb-4"></i>
            <p class="text-slate-500">No products available</p>
          </div>
        </div>

        <div class="px-6 py-4 border-t border-slate-100">
          <button 
            @click="$emit('close')" 
            class="btn-secondary w-full"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { barApi } from '@/api';

const props = defineProps<{
  isOpen: boolean;
}>();

defineEmits<{
  close: [];
}>();

const products = ref<any[]>([]);

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    fetchProducts();
  }
});

async function fetchProducts() {
  try {
    const response = await barApi.getProducts();
    products.value = response.data;
  } catch (error) {
    console.error('Error fetching products:', error);
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