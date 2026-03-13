<template>
  <!-- Stats -->
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Total Items</p>
          <p class="text-2xl font-bold text-slate-900 mt-1">{{ stock.length }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-warehouse-100 flex items-center justify-center">
          <i class="fas fa-boxes text-xl text-warehouse-600"></i>
        </div>
      </div>
    </div>
    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Total Quantity</p>
          <p class="text-2xl font-bold text-blue-600 mt-1">{{ totalQuantity }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-blue-100 flex items-center justify-center">
          <i class="fas fa-cubes text-xl text-blue-600"></i>
        </div>
      </div>
    </div>
    <div class="card p-5 card-interactive">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm text-slate-500 font-medium">Total Value</p>
          <p class="text-2xl font-bold text-emerald-600 mt-1">€{{ totalValue.toFixed(2) }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl bg-emerald-100 flex items-center justify-center">
          <i class="fas fa-euro-sign text-xl text-emerald-600"></i>
        </div>
      </div>
    </div>
    <div class="card p-5 card-interactive" :class="lowStockCount > 0 ? 'bg-amber-50 border-amber-200' : ''">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm font-medium" :class="lowStockCount > 0 ? 'text-amber-700' : 'text-slate-500'">Low Stock</p>
          <p class="text-2xl font-bold mt-1" :class="lowStockCount > 0 ? 'text-amber-700' : 'text-slate-900'">{{ lowStockCount }}</p>
        </div>
        <div class="w-12 h-12 rounded-xl flex items-center justify-center" :class="lowStockCount > 0 ? 'bg-amber-100' : 'bg-slate-100'">
          <i class="fas fa-exclamation-triangle text-xl" :class="lowStockCount > 0 ? 'text-amber-600' : 'text-slate-400'"></i>
        </div>
      </div>
    </div>
  </div>

  <!-- Loading -->
  <BaseLoadingSpinner v-if="loading" color="warehouse" message="Loading stock..." />

  <!-- Stock Table -->
  <div v-else class="card overflow-hidden">
    <div class="px-6 py-4 border-b border-slate-100 space-y-3">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <h2 class="font-semibold text-slate-900">Stock Inventory</h2>
        <button @click="openAddModal" class="btn-primary">
          <i class="fas fa-plus"></i>
          <span>Add Stock</span>
        </button>
      </div>
      <div class="flex flex-wrap items-center gap-2">
        <div class="relative">
          <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"></i>
          <input v-model="search" type="text" placeholder="Search products..." class="input pl-10 pr-4 py-2 w-64" />
        </div>
        <select v-model="statusFilter" class="input py-2 w-36">
          <option value="">All status</option>
          <option value="in_stock">In Stock</option>
          <option value="low">Low Stock</option>
          <option value="out">Out of Stock</option>
        </select>
        <button class="btn-secondary py-2 px-3 text-sm" @click="fetchData" :disabled="loading">
          <i class="fas fa-sync-alt text-xs"></i>
          Refresh
        </button>
      </div>
    </div>

    <div v-if="filtered.length" class="overflow-x-auto">
      <table class="table-modern">
        <thead>
          <tr>
            <th>ID</th>
            <th>Product</th>
            <th>Quantity</th>
            <th>Unit Price</th>
            <th>Value</th>
            <th>Min Level</th>
            <th>Status</th>
            <th class="text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filtered" :key="item.id" :class="isOut(item) ? 'bg-red-50/50' : (isLow(item) ? 'bg-amber-50/50' : '')">
            <td class="font-mono text-slate-500">#{{ item.id }}</td>
            <td class="font-medium text-slate-900">{{ item.productName || item.beverageType }}</td>
            <td>
              <span class="font-semibold" :class="isOut(item) ? 'text-red-600' : (isLow(item) ? 'text-amber-700' : 'text-slate-900')">
                {{ item.quantity }}
              </span>
            </td>
            <td>€{{ (item.unitPrice || 0).toFixed(2) }}</td>
            <td class="font-medium">€{{ ((item.quantity * (item.unitPrice || 0))).toFixed(2) }}</td>
            <td class="text-slate-500">{{ item.minStockLevel ?? 10 }}</td>
            <td>
              <span class="badge" :class="isOut(item) ? 'badge-danger' : (isLow(item) ? 'badge-warning' : 'badge-success')">
                <span class="w-1.5 h-1.5 rounded-full mr-1.5" :class="isOut(item) ? 'bg-red-500' : (isLow(item) ? 'bg-amber-500' : 'bg-emerald-500')"></span>
                {{ isOut(item) ? 'Out of Stock' : (isLow(item) ? 'Low Stock' : 'In Stock') }}
              </span>
            </td>
            <td>
              <div class="flex items-center justify-end gap-1">
                <button
                  @click="openEditModal(item)"
                  class="p-2 rounded-lg text-slate-400 hover:text-primary-600 hover:bg-primary-50 transition-colors"
                  title="Edit quantity"
                >
                  <i class="fas fa-edit"></i>
                </button>
                <button
                  @click="confirmDelete(item)"
                  class="p-2 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-colors"
                  title="Delete"
                >
                  <i class="fas fa-trash-alt"></i>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <BaseEmptyState
      v-else
      icon="fas fa-box-open"
      :color="search || statusFilter ? 'slate' : 'warehouse'"
      :title="search || statusFilter ? 'No items match your filters' : 'No stock items yet'"
    >
      <button v-if="!search && !statusFilter" @click="openAddModal" class="text-sm text-primary-600 hover:text-primary-700 mt-2 font-medium">
        Add your first product
      </button>
    </BaseEmptyState>
  </div>

  <!-- Add / Edit Stock Modal -->
  <Transition name="modal">
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-content max-w-md">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
          <h2 class="text-lg font-semibold text-slate-900">{{ editingItem ? 'Edit Stock' : 'Add Stock' }}</h2>
          <button @click="showModal = false" class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <form @submit.prevent="handleSubmit" class="p-6 space-y-4">
          <div>
            <label class="label">Product Name</label>
            <input v-model="form.productName" class="input" placeholder="e.g., Beer, Cola" required :disabled="!!editingItem" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="label">Quantity</label>
              <input v-model.number="form.quantity" type="number" class="input" min="0" required />
            </div>
            <div>
              <label class="label">Unit Price (€)</label>
              <input v-model.number="form.unitPrice" type="number" step="0.01" min="0" class="input" />
            </div>
          </div>
          <div>
            <label class="label">Min Stock Level</label>
            <input v-model.number="form.minStockLevel" type="number" min="0" class="input" placeholder="e.g., 10" />
            <p class="text-xs text-slate-400 mt-1">Alert threshold for low stock warnings</p>
          </div>
          <div class="flex gap-3 pt-2">
            <button type="button" @click="showModal = false" class="btn-secondary flex-1">Cancel</button>
            <button type="submit" class="btn-primary flex-1" :disabled="saving">
              <template v-if="saving"><i class="fas fa-spinner fa-spin"></i> Saving...</template>
              <template v-else><i class="fas fa-save"></i> {{ editingItem ? 'Update' : 'Add Stock' }}</template>
            </button>
          </div>
        </form>
      </div>
    </div>
  </Transition>

  <!-- Confirm Dialog -->
  <ConfirmDialog
    :isOpen="confirmDialog.show"
    :title="confirmDialog.title"
    :message="confirmDialog.message"
    :confirmText="confirmDialog.confirmText"
    :variant="confirmDialog.variant"
    :loading="confirmDialog.loading"
    @confirm="confirmDialog.onConfirm"
    @cancel="closeConfirm"
  />
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useWarehouseStockQuery, useCreateStock, useUpdateStockQuantity, useDeleteStockItem } from '@/composables/queries/useWarehouseQueries';
import ConfirmDialog from '@/components/common/ConfirmDialog.vue';
import { useConfirmDialog } from '@/composables/useConfirmDialog';
import { toast } from 'vue3-toastify';
import BaseLoadingSpinner from '@/components/common/BaseLoadingSpinner.vue';
import BaseEmptyState from '@/components/common/BaseEmptyState.vue';

const { data: stockData, isLoading: loading, refetch } = useWarehouseStockQuery();
const createStockMutation = useCreateStock();
const updateStockMutation = useUpdateStockQuantity();
const deleteStockMutation = useDeleteStockItem();

const saving = ref(false);
const search = ref('');
const statusFilter = ref('');
const showModal = ref(false);
const editingItem = ref(null);

const form = reactive({
  productName: '',
  quantity: 0,
  unitPrice: 0,
  minStockLevel: 10,
});

const { state: confirmDialog, open: openConfirm, close: closeConfirm } = useConfirmDialog();

const stock = computed(() => stockData.value ?? []);
const totalQuantity = computed(() => stock.value.reduce((s, i) => s + (i.quantity || 0), 0));
const totalValue = computed(() => stock.value.reduce((s, i) => s + (i.quantity * (i.unitPrice || 0)), 0));
const lowStockCount = computed(() => stock.value.filter(i => isLow(i)).length);

function isLow(item) {
  return item.lowStock || item.quantity < (item.minStockLevel || 10);
}

function isOut(item) {
  return (item.quantity === 0);
}

const filtered = computed(() => {
  const q = search.value.toLowerCase().trim();
  return stock.value.filter(i => {
    const name = (i.productName || i.beverageType || '').toLowerCase();
    if (q && !name.includes(q) && !String(i.id).includes(q)) return false;
    if (statusFilter.value === 'low' && !isLow(i)) return false;
    if (statusFilter.value === 'in_stock' && isLow(i)) return false;
    if (statusFilter.value === 'out' && !isOut(i)) return false;
    return true;
  });
});

function fetchData() {
  refetch();
}

function openAddModal() {
  editingItem.value = null;
  Object.assign(form, { productName: '', quantity: 0, unitPrice: 0, minStockLevel: 10 });
  showModal.value = true;
}

function openEditModal(item) {
  editingItem.value = item;
  Object.assign(form, {
    productName: item.productName || item.beverageType,
    quantity: item.quantity,
    unitPrice: item.unitPrice || 0,
    minStockLevel: item.minStockLevel || 10,
  });
  showModal.value = true;
}

async function handleSubmit() {
  saving.value = true;
  try {
    if (editingItem.value) {
      const beverageType = editingItem.value.beverageType || editingItem.value.productName;
      const quantity = form.quantity;
      await updateStockMutation.mutateAsync({ beverageType, quantity });
      toast.success('Stock updated');
    } else {
      await createStockMutation.mutateAsync({
        beverageType: form.productName,
        quantity: form.quantity,
        minStockLevel: form.minStockLevel,
      });
      toast.success(`${form.productName} added to stock`);
    }
    showModal.value = false;
  } catch (e: any) {
    toast.error(e?.message ?? 'Failed to save stock');
  } finally {
    saving.value = false;
  }
}

function confirmDelete(item) {
  openConfirm({
    title: 'Delete Stock Item',
    message: `Permanently delete "${item.productName || item.beverageType}"? This cannot be undone.`,
    confirmText: 'Delete',
    variant: 'danger',
    onConfirm: async () => {
      await deleteStockMutation.mutateAsync(item.id);
      toast.success('Stock item deleted');
    },
  });
}

</script>

<style scoped>
.modal-enter-active, .modal-leave-active { transition: all 0.3s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-from .modal-content, .modal-leave-to .modal-content { transform: scale(0.95); }
</style>
