<template>
  <Transition name="modal">
    <div
      v-if="isOpen"
      class="modal-overlay"
      @click.self="$emit('close')"
    >
      <div class="modal-content max-w-md">
        <!-- Header with Product Info -->
        <div class="px-6 py-6 border-b border-slate-100">
          <div class="flex items-center gap-4">
            <div 
              class="w-16 h-16 rounded-2xl flex items-center justify-center"
              :class="getProductBgClass(productName)"
            >
              <i class="fas fa-wine-bottle text-white text-2xl"></i>
            </div>
            <div class="flex-1">
              <h2 class="text-xl font-bold text-slate-900">Sell Item</h2>
              <p class="text-lg font-semibold" :class="getProductTextClass(productName)">
                {{ productName }}
              </p>
            </div>
            <button 
              @click="$emit('close')" 
              class="p-3 rounded-xl text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
            >
              <i class="fas fa-times text-xl"></i>
            </button>
          </div>
        </div>

        <!-- Body -->
        <div class="p-6">
          <!-- Stock Info -->
          <div class="flex items-center justify-between p-4 bg-slate-50 rounded-xl mb-6">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-lg bg-white flex items-center justify-center shadow-sm">
                <i class="fas fa-boxes text-slate-400"></i>
              </div>
              <div>
                <p class="text-sm text-slate-500">Available Stock</p>
                <p class="text-xl font-bold text-slate-900">{{ availableQuantity }} units</p>
              </div>
            </div>
            <span 
              class="badge"
              :class="availableQuantity > 10 ? 'badge-success' : availableQuantity > 0 ? 'badge-warning' : 'badge-danger'"
            >
              {{ availableQuantity > 10 ? 'In Stock' : availableQuantity > 0 ? 'Low Stock' : 'Empty' }}
            </span>
          </div>

          <!-- Quantity Selector -->
          <div class="mb-6">
            <label class="label text-base">Select Quantity</label>
            <div class="flex items-center gap-4 mt-3">
              <button 
                @click="decreaseQuantity"
                :disabled="quantity <= 1"
                class="w-14 h-14 rounded-xl text-2xl font-bold transition-all duration-200 
                       flex items-center justify-center
                       bg-slate-100 text-slate-700 hover:bg-slate-200
                       disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <i class="fas fa-minus"></i>
              </button>
              
              <input
                v-model.number="quantity"
                type="number"
                :max="availableQuantity"
                min="1"
                @input="validateQuantity"
                class="flex-1 h-14 text-center text-2xl font-bold border-2 border-slate-200 rounded-xl
                       focus:border-primary-500 focus:ring-0 focus:outline-none"
              />
              
              <button 
                @click="increaseQuantity"
                :disabled="quantity >= availableQuantity"
                class="w-14 h-14 rounded-xl text-2xl font-bold transition-all duration-200 
                       flex items-center justify-center
                       bg-primary-100 text-primary-700 hover:bg-primary-200
                       disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <i class="fas fa-plus"></i>
              </button>
            </div>
            
            <!-- Quick Select Buttons -->
            <div class="flex gap-2 mt-4">
              <button 
                v-for="preset in quickPresets"
                :key="preset"
                @click="quantity = Math.min(preset, availableQuantity)"
                :disabled="preset > availableQuantity"
                class="flex-1 py-2.5 rounded-lg text-sm font-semibold transition-all
                       border-2 border-slate-200 text-slate-600 hover:border-primary-300 hover:text-primary-600
                       disabled:opacity-40 disabled:cursor-not-allowed"
                :class="{ 'border-primary-500 bg-primary-50 text-primary-700': quantity === preset }"
              >
                {{ preset }}
              </button>
              <button 
                @click="quantity = availableQuantity"
                :disabled="availableQuantity === 0"
                class="flex-1 py-2.5 rounded-lg text-sm font-semibold transition-all
                       border-2 border-amber-200 text-amber-600 hover:border-amber-400 hover:bg-amber-50
                       disabled:opacity-40 disabled:cursor-not-allowed"
              >
                All
              </button>
            </div>
          </div>

          <!-- Summary -->
          <div class="p-4 bg-emerald-50 rounded-xl border border-emerald-100 mb-6">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-emerald-100 flex items-center justify-center">
                  <i class="fas fa-shopping-cart text-emerald-600"></i>
                </div>
                <span class="text-base font-medium text-emerald-800">Total to Sell</span>
              </div>
              <span class="text-2xl font-bold text-emerald-700">{{ quantity }}</span>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-6 py-4 border-t border-slate-100 flex gap-3">
          <button type="button" @click="$emit('close')" class="btn-secondary flex-1">
            <i class="fas fa-times"></i>
            Cancel
          </button>
          <button
            @click="confirmSale"
            :disabled="quantity === 0 || quantity > availableQuantity"
            class="btn-success flex-1"
          >
            <i class="fas fa-check"></i>
            Confirm Sale
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

const props = defineProps<{
  isOpen: boolean;
  item: Record<string, any> | null;
  barId: string;
}>();

const emit = defineEmits<{
  close: [];
  'sale-confirmed': [payload: { quantity: number }];
}>();

const quantity = ref(1);
const quickPresets = [1, 5, 10, 20];

const productName = computed(() => props.item?.name ?? props.item?.productName ?? 'Unknown Product');
const availableQuantity = computed(() => props.item?.quantity ?? 0);

watch(() => props.isOpen, (newVal) => {
  if (newVal) quantity.value = 1;
});

function getProductBgClass(name: string): string {
  const lowerName = String(name ?? '').toLowerCase();
  if (lowerName.includes('fanta')) return 'bg-gradient-to-br from-amber-400 to-amber-600';
  if (lowerName.includes('coca') || lowerName.includes('zero')) return 'bg-gradient-to-br from-slate-700 to-slate-900';
  if (lowerName.includes('red bull')) return 'bg-gradient-to-br from-red-400 to-red-600';
  if (lowerName.includes('beer')) return 'bg-gradient-to-br from-yellow-400 to-yellow-600';
  if (lowerName.includes('wine')) return 'bg-gradient-to-br from-purple-400 to-purple-600';
  return 'bg-gradient-to-br from-bar-400 to-bar-600';
}

function getProductTextClass(name: string): string {
  const lowerName = String(name ?? '').toLowerCase();
  if (lowerName.includes('fanta')) return 'text-amber-600';
  if (lowerName.includes('coca') || lowerName.includes('zero')) return 'text-slate-700';
  if (lowerName.includes('red bull')) return 'text-red-600';
  if (lowerName.includes('beer')) return 'text-yellow-600';
  if (lowerName.includes('wine')) return 'text-purple-600';
  return 'text-bar-600';
}

function decreaseQuantity() {
  if (quantity.value > 1) quantity.value--;
}

function increaseQuantity() {
  if (quantity.value < availableQuantity.value) quantity.value++;
}

function validateQuantity() {
  if (quantity.value < 1) quantity.value = 1;
  else if (quantity.value > availableQuantity.value) quantity.value = availableQuantity.value;
}

function confirmSale() {
  if (quantity.value === 0 || quantity.value > availableQuantity.value) return;
  emit('sale-confirmed', { quantity: quantity.value });
  emit('close');
  quantity.value = 1;
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

/* Hide number input spinners */
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type="number"] {
  -moz-appearance: textfield;
}
</style>
