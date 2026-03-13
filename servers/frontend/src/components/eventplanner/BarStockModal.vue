<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-content max-w-2xl animate-scale-in">
        <!-- Header -->
        <div class="modal-header">
          <div class="flex items-center gap-4">
            <div class="w-12 h-12 rounded-xl bg-bar-100 flex items-center justify-center">
              <i class="fas fa-boxes text-bar-600 text-xl"></i>
            </div>
            <div>
              <h3 class="text-xl font-bold text-slate-900">{{ bar?.name }} - Stock Management</h3>
              <p class="text-slate-500 text-sm">Manage inventory from the warehouse</p>
            </div>
          </div>
          <button 
            @click="$emit('close')"
            class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <!-- Body -->
        <div class="modal-body">
          <!-- Loading State -->
          <div v-if="loading" class="flex items-center justify-center py-12">
            <div class="w-10 h-10 border-4 border-bar-200 border-t-bar-600 rounded-full animate-spin"></div>
          </div>

          <template v-else>
            <!-- Current Stock -->
            <div class="mb-6">
              <h4 class="text-lg font-semibold text-slate-900 mb-3">Current Stock</h4>
              <div v-if="stocks.length > 0" class="space-y-2">
                <div 
                  v-for="stock in stocks" 
                  :key="stock.id"
                  class="flex items-center justify-between p-3 bg-slate-50 rounded-lg"
                >
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded-lg bg-bar-100 flex items-center justify-center">
                      <i class="fas fa-wine-bottle text-bar-600 text-sm"></i>
                    </div>
                    <div>
                      <p class="font-medium text-slate-900">{{ stock.itemName || stock.productName }}</p>
                      <p class="text-xs text-slate-500">
                        Quantity: {{ stock.quantity ?? stock.allocatedQuantity ?? 0 }}
                      </p>
                    </div>
                  </div>
                  <button
                    @click="confirmDeleteStock(stock.id)"
                    class="p-2 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors"
                    title="Remove"
                  >
                    <i class="fas fa-trash-alt text-sm"></i>
                  </button>
                </div>
              </div>
              <div v-else class="text-center py-8 text-slate-500">
                <i class="fas fa-box-open text-3xl mb-2 text-slate-300"></i>
                <p>No stock allocated yet</p>
              </div>
            </div>

            <!-- Add Stock Form -->
            <div class="border-t border-slate-100 pt-6">
              <h4 class="text-lg font-semibold text-slate-900 mb-3">Add Stock from Warehouse</h4>
              
              <div v-if="warehouseInventory.length === 0" class="text-center py-6 text-slate-500">
                <i class="fas fa-warehouse text-2xl mb-2 text-slate-300"></i>
                <p>No warehouse inventory available</p>
              </div>

              <form v-else @submit.prevent="handleAddStock" class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <!-- Product Select -->
                <div class="md:col-span-1">
                  <label class="label" for="productSelect">Product</label>
                  <select
                    id="productSelect"
                    v-model.number="addForm.productId"
                    class="input"
                    required
                  >
                    <option value="">Select product...</option>
                    <option 
                      v-for="item in warehouseInventory" 
                      :key="item.productId"
                      :value="item.productId"
                    >
                      {{ item.productName }} (Avail: {{ item.availableQuantity }})
                    </option>
                  </select>
                </div>

                <!-- Quantity -->
                <div class="md:col-span-1">
                  <label class="label" for="quantity">Quantity</label>
                  <input
                    id="quantity"
                    v-model.number="addForm.quantity"
                    type="number"
                    min="1"
                    :max="selectedProductMaxQty"
                    class="input"
                    required
                  />
                </div>

                <!-- Add Button -->
                <div class="md:col-span-1 flex items-end">
                  <button
                    type="submit"
                    :disabled="!addForm.productId || addForm.quantity < 1 || addingStock"
                    class="btn-primary w-full"
                  >
                    <i v-if="addingStock" class="fas fa-spinner animate-spin mr-2"></i>
                    <i v-else class="fas fa-plus mr-2"></i>
                    Add Stock
                  </button>
                </div>
              </form>
            </div>
          </template>
        </div>
        
        <!-- Footer -->
        <div class="modal-footer justify-end">
          <button 
            type="button"
            @click="$emit('close')"
            class="btn-secondary"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { toast } from 'vue3-toastify';
import { eventApi } from '@/api/eventApi';
import type { EventBar, BarStock, WarehouseStockDto } from '@/api/eventApi';

interface Props {
  bar: EventBar;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  close: [];
  updated: [];
}>();

// ============ State ============
const loading = ref(false);
const addingStock = ref(false);
const stocks = ref<BarStock[]>([]);
const warehouseInventory = ref<WarehouseStockDto[]>([]);

const addForm = ref({
  productId: 0,
  quantity: 10,
});

// ============ Computed ============
const selectedProductMaxQty = computed(() => {
  const selected = warehouseInventory.value.find(i => i.productId === addForm.value.productId);
  return selected?.availableQuantity || 0;
});

// ============ Methods ============
const loadData = async () => {
  loading.value = true;
  try {
    const [stockResponse, inventoryResponse] = await Promise.all([
      eventApi.getBarStocks(props.bar.id),
      eventApi.getWarehouseInventory()
    ]);
    stocks.value = stockResponse.data || [];
    warehouseInventory.value = inventoryResponse.data || [];
  } catch (err) {
    console.error('Error loading stock data:', err);
    toast.error('Failed to load stock data');
  } finally {
    loading.value = false;
  }
};

const handleAddStock = async () => {
  if (!addForm.value.productId || addForm.value.quantity < 1) return;

  const selectedProduct = warehouseInventory.value.find(i => i.productId === addForm.value.productId);
  if (!selectedProduct) {
    toast.error('Please select a valid product');
    return;
  }

  addingStock.value = true;
  try {
    await eventApi.addBarStock(props.bar.id, {
      itemName: selectedProduct.productName || selectedProduct.beverageType || '',
      quantity: addForm.value.quantity,
    });
    toast.success('Stock added successfully');
    
    // Reset form and reload
    addForm.value.productId = 0;
    addForm.value.quantity = 10;
    await loadData();
    emit('updated');
  } catch (err) {
    const message = err instanceof Error ? err.message : 'Failed to add stock';
    toast.error(message);
  } finally {
    addingStock.value = false;
  }
};

const confirmDeleteStock = async (stockId: number) => {
  if (!confirm('Are you sure you want to remove this stock?')) return;

  try {
    await eventApi.deleteBarStock(props.bar.id, stockId);
    toast.success('Stock removed');
    await loadData();
    emit('updated');
  } catch (err) {
    toast.error('Failed to remove stock');
  }
};

// ============ Lifecycle ============
onMounted(() => {
  loadData();
});

// Reload when bar changes
watch(() => props.bar.id, () => {
  loadData();
});
</script>
