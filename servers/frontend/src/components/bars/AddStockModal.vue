<template>
  <Transition name="modal">
    <div
      v-if="isOpen"
      class="modal-overlay"
      @click.self="$emit('close')"
    >
      <div class="modal-content max-w-md">
        <!-- Header -->
        <div class="px-6 py-5 border-b border-slate-100">
          <div class="flex items-center gap-4">
            <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-bar-400 to-bar-600 
                        flex items-center justify-center">
              <i class="fas fa-plus text-white text-2xl"></i>
            </div>
            <div class="flex-1">
              <h2 class="text-xl font-bold text-slate-900">Add Stock</h2>
              <p class="text-base text-slate-500">Add drinks to this bar's inventory</p>
            </div>
            <button 
              @click="$emit('close')" 
              class="p-3 rounded-xl text-slate-400 hover:text-slate-600 
                     hover:bg-slate-100 transition-colors"
            >
              <i class="fas fa-times text-xl"></i>
            </button>
          </div>
        </div>

        <!-- Body -->
        <form @submit.prevent="handleSubmit" class="p-6">
          <!-- Product Select -->
          <div class="mb-6">
            <label for="product" class="label text-base">Select Product</label>
            <div class="relative mt-2">
              <div class="absolute left-4 top-1/2 -translate-y-1/2">
                <i class="fas fa-wine-bottle text-slate-400"></i>
              </div>
              <select
                v-model="formData.productId"
                id="product"
                required
                class="input pl-12 text-base appearance-none cursor-pointer"
              >
                <option value="" disabled>Choose a product...</option>
                <option
                  v-for="product in products"
                  :key="product.id"
                  :value="product.id"
                >
                  {{ product.name }} ({{ product.unitType }})
                </option>
              </select>
              <div class="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none">
                <i class="fas fa-chevron-down text-slate-400"></i>
              </div>
            </div>
          </div>

          <!-- Quantity Input -->
          <div class="mb-6">
            <label class="label text-base">Quantity</label>
            <div class="flex items-center gap-4 mt-2">
              <button 
                type="button"
                @click="decreaseQuantity"
                :disabled="formData.quantity <= 1"
                class="w-14 h-14 rounded-xl text-2xl font-bold transition-all duration-200 
                       flex items-center justify-center
                       bg-slate-100 text-slate-700 hover:bg-slate-200
                       disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <i class="fas fa-minus"></i>
              </button>
              
              <input
                type="number"
                v-model.number="formData.quantity"
                id="quantity"
                required
                min="1"
                class="flex-1 h-14 text-center text-2xl font-bold border-2 border-slate-200 rounded-xl
                       focus:border-bar-500 focus:ring-0 focus:outline-none"
              />
              
              <button 
                type="button"
                @click="increaseQuantity"
                class="w-14 h-14 rounded-xl text-2xl font-bold transition-all duration-200 
                       flex items-center justify-center
                       bg-bar-100 text-bar-700 hover:bg-bar-200"
              >
                <i class="fas fa-plus"></i>
              </button>
            </div>
            
            <!-- Quick Select Buttons -->
            <div class="flex gap-2 mt-4">
              <button 
                type="button"
                v-for="preset in [5, 10, 25, 50, 100]"
                :key="preset"
                @click="formData.quantity = preset"
                class="flex-1 py-2.5 rounded-lg text-sm font-semibold transition-all
                       border-2 border-slate-200 text-slate-600 hover:border-bar-300 hover:text-bar-600"
                :class="{ 'border-bar-500 bg-bar-50 text-bar-700': formData.quantity === preset }"
              >
                {{ preset }}
              </button>
            </div>
          </div>

          <!-- Summary -->
          <div class="p-4 bg-bar-50 rounded-xl border border-bar-100 mb-6">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-bar-100 flex items-center justify-center">
                  <i class="fas fa-boxes text-bar-600"></i>
                </div>
                <span class="text-base font-medium text-bar-800">Adding to Stock</span>
              </div>
              <span class="text-2xl font-bold text-bar-700">{{ formData.quantity }}</span>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex gap-3">
            <button type="button" @click="$emit('close')" class="btn-secondary flex-1">
              <i class="fas fa-times"></i>
              Cancel
            </button>
            <button
              type="submit"
              :disabled="!formData.productId || formData.quantity < 1"
              class="btn-primary flex-1"
            >
              <i class="fas fa-plus"></i>
              Add Stock
            </button>
          </div>
        </form>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { barApi } from '@/api';
import type { Product, StockOperationPayload } from '@/api/types';

const props = defineProps<{
  isOpen: boolean;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
  (e: 'stock-added', payload: StockOperationPayload): void;
}>();

const route = useRoute();
const products = ref<Product[]>([]);

const SAMPLE_PRODUCTS: Product[] = [
  { id: 'cola', name: 'Cola', unitType: 'Bottle' },
  { id: 'water', name: 'Water', unitType: 'Bottle' },
  { id: 'orange-juice', name: 'Orange Juice', unitType: 'Bottle' },
];

const formData = reactive({
  productId: '',
  quantity: 10,
});

// Fetch products and reset form when modal opens
watch(() => props.isOpen, async (newVal) => {
  if (newVal) {
    formData.productId = '';
    formData.quantity = 10;
    await fetchProducts();
  }
});

async function fetchProducts() {
  try {
    const response = await barApi.getProducts();
    const fetchedProducts = Array.isArray(response.data) ? response.data : [];
    products.value = fetchedProducts.length ? fetchedProducts : SAMPLE_PRODUCTS;
  } catch (error) {
    console.error('Error fetching products:', error);
    products.value = SAMPLE_PRODUCTS;
  }
}

function decreaseQuantity() {
  if (formData.quantity > 1) {
    formData.quantity--;
  }
}

function increaseQuantity() {
  formData.quantity++;
}

async function handleSubmit() {
  if (formData.productId && formData.quantity > 0) {
    const barId = route.params.barId as string;
    // Resolve productName from the selected productId
    const selectedProduct = products.value.find(p => p.id === formData.productId);
    const productName = selectedProduct?.name || formData.productId;
    try {
      await barApi.addStock(barId, {
        productName,
        quantity: formData.quantity,
      });
      emit('close');
      emit('stock-added', { productName, quantity: formData.quantity });
    } catch (error) {
      console.error('Error adding stock:', error);
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

/* Hide number input spinners */
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type="number"] {
  -moz-appearance: textfield;
  appearance: textfield;
}

/* Custom select styling */
select {
  background-image: none;
}
</style>
