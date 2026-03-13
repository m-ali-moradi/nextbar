<template>
  <Transition name="modal">
    <div v-if="isOpen" class="modal-overlay" @click.self="emit('close')">
      <div class="modal-content max-w-md">
        <!-- Header -->
        <div class="px-6 py-5 border-b border-slate-100">
          <div class="flex items-center gap-4">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-amber-400 to-amber-600 
                        flex items-center justify-center">
              <i class="fas fa-truck text-white text-2xl"></i>
            </div>
            <div class="flex-1">
              <h2 class="text-xl font-bold text-slate-900">Request Supply</h2>
              <p class="text-base font-medium text-amber-600">{{ displayName }}</p>
            </div>
            <button 
              @click="emit('close')" 
              class="p-3 rounded-xl text-slate-400 hover:text-slate-600 
                     hover:bg-slate-100 transition-colors"
            >
              <i class="fas fa-times text-xl"></i>
            </button>
          </div>
        </div>

        <!-- Body -->
        <div class="p-6">
          <!-- Info Card -->
          <div class="flex items-center gap-4 p-4 bg-amber-50 rounded-xl border border-amber-100 mb-6">
            <div class="w-10 h-10 rounded-lg bg-amber-100 flex items-center justify-center">
              <i class="fas fa-info-circle text-amber-600"></i>
            </div>
            <div>
              <p class="text-sm text-amber-800">
                Request stock replenishment from the warehouse
              </p>
              <p class="text-xs text-amber-600 mt-1">Maximum 50 units per request</p>
            </div>
          </div>

          <!-- Quantity Selector -->
          <div class="mb-6">
            <label class="label text-base">Quantity to Request</label>
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
                id="supply-quantity"
                v-model.number="quantity"
                type="number"
                min="1"
                max="50"
                @blur="validateQuantity"
                class="flex-1 h-14 text-center text-2xl font-bold border-2 border-slate-200 rounded-xl
                       focus:border-amber-500 focus:ring-0 focus:outline-none"
              />
              
              <button 
                @click="increaseQuantity"
                :disabled="quantity >= 50"
                class="w-14 h-14 rounded-xl text-2xl font-bold transition-all duration-200 
                       flex items-center justify-center
                       bg-amber-100 text-amber-700 hover:bg-amber-200
                       disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <i class="fas fa-plus"></i>
              </button>
            </div>
            
            <!-- Quick Select Buttons -->
            <div class="flex gap-2 mt-4">
              <button 
                v-for="preset in [5, 10, 20, 30, 50]"
                :key="preset"
                @click="quantity = preset"
                class="flex-1 py-2.5 rounded-lg text-sm font-semibold transition-all
                       border-2 border-slate-200 text-slate-600 hover:border-amber-300 hover:text-amber-600
                       disabled:opacity-40 disabled:cursor-not-allowed"
                :class="{ 'border-amber-500 bg-amber-50 text-amber-700': quantity === preset }"
              >
                {{ preset }}
              </button>
            </div>
          </div>

          <!-- Summary -->
          <div class="p-4 bg-slate-50 rounded-xl border border-slate-200">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-white shadow-sm flex items-center justify-center">
                  <i class="fas fa-boxes text-slate-400"></i>
                </div>
                <span class="text-base font-medium text-slate-700">Total Request</span>
              </div>
              <span class="text-2xl font-bold text-slate-900">{{ quantity }} units</span>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="px-6 py-4 border-t border-slate-100 flex gap-3">
          <button type="button" @click="emit('close')" class="btn-secondary flex-1">
            <i class="fas fa-times"></i>
            Cancel
          </button>
          <button
            @click="requestSupply"
            :disabled="!canSubmit"
            class="btn-warning flex-1"
          >
            <i class="fas fa-truck"></i>
            Request Supply
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed, ref, watch } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  item: Object,
  barId: String,
});

const emit = defineEmits(['close', 'supply-requested']);

const quantity = ref(10);

watch(
  () => props.isOpen,
  (open) => {
    if (open) quantity.value = 10;
  }
);

const displayName = computed(() => {
  const raw = props.item?.name ?? props.item?.productName ?? 'Unknown product';
  return String(raw);
});

const canSubmit = computed(() => {
  return !!(props.item?.productId || props.item?.productName) && quantity.value >= 1 && quantity.value <= 50;
});

function decreaseQuantity() {
  if (quantity.value > 1) {
    quantity.value--;
  }
}

function increaseQuantity() {
  if (quantity.value < 50) {
    quantity.value++;
  }
}

function validateQuantity() {
  if (Number.isNaN(quantity.value)) quantity.value = 1;
  if (quantity.value < 1) quantity.value = 1;
  if (quantity.value > 50) quantity.value = 50;
}

function requestSupply() {
  validateQuantity();
  if (!canSubmit.value) return;
  emit('supply-requested', { quantity: quantity.value });
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
