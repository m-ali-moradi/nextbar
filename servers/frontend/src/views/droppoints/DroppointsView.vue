<template>
  <div class="flex min-h-screen bg-slate-50">
    <Sidebar />
    
    <div class="flex-1 ml-72">
      <Navbar />

      <main class="p-6">
        <!-- Page Header -->
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-bold text-slate-900">Drop Points</h1>
            <p class="text-slate-500 mt-1">Manage bottle collection points</p>
          </div>
          <button @click="openModal()" class="btn-primary">
            <i class="fas fa-plus"></i>
            <span>New Drop Point</span>
          </button>
        </div>

        <!-- Stats Overview -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Total Points</p>
                <p class="text-2xl font-bold text-slate-900 mt-1">{{ droppoints.length }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-droppoint-100 flex items-center justify-center">
                <i class="fas fa-map-marker-alt text-xl text-droppoint-600"></i>
              </div>
            </div>
          </div>
          
          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Empty</p>
                <p class="text-2xl font-bold text-blue-600 mt-1">{{ emptyCount }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-blue-100 flex items-center justify-center">
                <i class="fas fa-check text-xl text-blue-600"></i>
              </div>
            </div>
          </div>

          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Full</p>
                <p class="text-2xl font-bold text-amber-600 mt-1">{{ fullCount }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-amber-100 flex items-center justify-center">
                <i class="fas fa-exclamation-circle text-xl text-amber-600"></i>
              </div>
            </div>
          </div>

          <div class="card p-5 card-interactive">
            <div class="flex items-center justify-between">
              <div>
                <p class="text-sm text-slate-500 font-medium">Notified</p>
                <p class="text-2xl font-bold text-violet-600 mt-1">{{ notifiedCount }}</p>
              </div>
              <div class="w-12 h-12 rounded-xl bg-violet-100 flex items-center justify-center">
                <i class="fas fa-bell text-xl text-violet-600"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="flex items-center justify-center py-20">
          <div class="text-center">
            <div class="w-12 h-12 border-4 border-droppoint-200 border-t-droppoint-600 rounded-full animate-spin mx-auto mb-4"></div>
            <p class="text-slate-500">Loading drop points...</p>
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="card p-8 text-center">
          <div class="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-4">
            <i class="fas fa-exclamation-triangle text-2xl text-red-600"></i>
          </div>
          <h3 class="text-lg font-semibold text-slate-900 mb-2">Error Loading Data</h3>
          <p class="text-slate-500 mb-4">{{ error }}</p>
          <button @click="droppointStore.fetchDropPoints" class="btn-primary">
            <i class="fas fa-redo"></i>
            <span>Try Again</span>
          </button>
        </div>

        <!-- Drop Points Table -->
        <div v-else-if="droppoints.length > 0" class="card overflow-hidden">
          <div class="px-6 py-4 border-b border-slate-100">
            <h2 class="font-semibold text-slate-900">All Drop Points</h2>
          </div>
          
          <div class="overflow-x-auto">
            <table class="table-modern">
              <thead>
                <tr>
                  <th>Location</th>
                  <th>Capacity</th>
                  <th>Current Fill</th>
                  <th>Status</th>
                  <th class="text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="droppoint in droppoints" :key="droppoint.id">
                  <td>
                    <div class="flex items-center gap-3">
                      <div class="w-10 h-10 rounded-xl bg-droppoint-100 flex items-center justify-center">
                        <i class="fas fa-map-marker-alt text-droppoint-600"></i>
                      </div>
                      <div>
                        <p class="font-medium text-slate-900">{{ droppoint.location }}</p>
                        <p class="text-xs text-slate-500">#{{ droppoint.id }}</p>
                      </div>
                    </div>
                  </td>
                  <td>{{ droppoint.capacity }} bottles</td>
                  <td>
                    <div class="flex items-center gap-3">
                      <div class="flex-1 h-2 bg-slate-100 rounded-full overflow-hidden max-w-[120px]">
                        <div 
                          class="h-full rounded-full transition-all duration-300"
                          :class="getFillBarClass(droppoint)"
                          :style="{ width: getFillPercentage(droppoint) + '%' }"
                        ></div>
                      </div>
                      <span class="text-sm font-medium text-slate-600">
                        {{ droppoint.current_empties }}/{{ droppoint.capacity }}
                      </span>
                    </div>
                  </td>
                  <td>
                    <span class="badge" :class="getStatusBadgeClass(droppoint.status)">
                      {{ formatStatus(droppoint.status) }}
                    </span>
                  </td>
                  <td>
                    <div class="flex items-center justify-end gap-2">
                      <button 
                        @click="openModal(droppoint)" 
                        class="p-2 rounded-lg text-slate-500 hover:text-slate-700 hover:bg-slate-100 transition-colors"
                        title="Edit"
                      >
                        <i class="fas fa-edit"></i>
                      </button>
                      <button 
                        @click="handleDelete(droppoint.id!)" 
                        class="p-2 rounded-lg text-slate-500 hover:text-red-600 hover:bg-red-50 transition-colors"
                        title="Delete"
                      >
                        <i class="fas fa-trash-alt"></i>
                      </button>
                      
                      <!-- Action Buttons based on status -->
                      <button 
                        v-if="droppoint.status === 'EMPTY'" 
                        @click="handleAddEmpty(droppoint.id!)" 
                        class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-droppoint-600 text-white 
                               rounded-lg text-sm font-medium hover:bg-droppoint-700 transition-colors"
                      >
                        <i class="fas fa-plus"></i>
                        Add
                      </button>
                      <button 
                        v-if="droppoint.status === 'FULL'" 
                        @click="handleNotifyWarehouse(droppoint.id!)" 
                        class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-amber-500 text-white 
                               rounded-lg text-sm font-medium hover:bg-amber-600 transition-colors"
                      >
                        <i class="fas fa-bell"></i>
                        Notify
                      </button>
                      <button 
                        v-if="droppoint.status === 'FULL_AND_NOTIFIED_TO_WAREHOUSE'" 
                        @click="handleVerifyTransfer(droppoint.id!)" 
                        class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-violet-600 text-white 
                               rounded-lg text-sm font-medium hover:bg-violet-700 transition-colors"
                      >
                        <i class="fas fa-check"></i>
                        Verify
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="card p-12 text-center">
          <div class="w-20 h-20 rounded-2xl bg-droppoint-100 flex items-center justify-center mx-auto mb-6">
            <i class="fas fa-map-marker-alt text-3xl text-droppoint-400"></i>
          </div>
          <h3 class="text-xl font-semibold text-slate-900 mb-2">No drop points yet</h3>
          <p class="text-slate-500 mb-6 max-w-sm mx-auto">
            Create your first drop point to start collecting empties.
          </p>
          <button @click="openModal()" class="btn-primary">
            <i class="fas fa-plus"></i>
            <span>Create Drop Point</span>
          </button>
        </div>

        <!-- Modal Form -->
        <Transition name="modal">
          <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
            <div class="modal-content">
              <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between">
                <h2 class="text-lg font-semibold text-slate-900">
                  {{ isEditing ? 'Edit Drop Point' : 'New Drop Point' }}
                </h2>
                <button 
                  @click="closeModal" 
                  class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
                >
                  <i class="fas fa-times"></i>
                </button>
              </div>

              <form @submit.prevent="handleSubmit" class="p-6 space-y-5">
                <div>
                  <label for="location" class="label">Location</label>
                  <input 
                    id="location" 
                    v-model="formData.location" 
                    type="text" 
                    required 
                    placeholder="e.g., Main Entrance"
                    class="input"
                  />
                </div>

                <div>
                  <label for="capacity" class="label">Capacity (bottles)</label>
                  <input 
                    id="capacity" 
                    v-model.number="formData.capacity" 
                    type="number" 
                    required 
                    min="1"
                    placeholder="Maximum bottle capacity"
                    class="input"
                  />
                </div>

                <div v-if="isEditing">
                  <label for="current_empties" class="label">Current Empties</label>
                  <input 
                    id="current_empties" 
                    v-model.number="formData.current_empties" 
                    type="number" 
                    min="0"
                    :max="formData.capacity"
                    class="input"
                  />
                </div>

                <div v-if="isEditing">
                  <label for="status" class="label">Status</label>
                  <select id="status" v-model="formData.status" class="input">
                    <option value="EMPTY">Empty</option>
                    <option value="FULL">Full</option>
                    <option value="FULL_AND_NOTIFIED_TO_WAREHOUSE">Full & Notified</option>
                  </select>
                </div>

                <div class="flex gap-3 pt-2">
                  <button type="submit" class="btn-primary flex-1">
                    {{ isEditing ? 'Update' : 'Create' }}
                  </button>
                  <button type="button" @click="closeModal" class="btn-secondary flex-1">
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>
        </Transition>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useDroppointStore } from '@/stores/droppointStore';
import { DropPoint } from '@/api/droppointApi';
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';
import Sidebar from '@/components/common/Sidebar.vue';
import Navbar from '@/components/common/Navbar.vue';

const droppointStore = useDroppointStore();

// State
const showModal = ref(false);
const isEditing = ref(false);
const editingId = ref<number | null>(null);

const formData = ref<Partial<DropPoint>>({
  location: '',
  capacity: 100,
  current_empties: 0,
  status: 'EMPTY',
});

// Computed
const droppoints = computed(() => droppointStore.droppoints);
const loading = computed(() => droppointStore.loading);
const error = computed(() => droppointStore.error);

const emptyCount = computed(() => droppoints.value.filter(d => d.status === 'EMPTY').length);
const fullCount = computed(() => droppoints.value.filter(d => d.status === 'FULL').length);
const notifiedCount = computed(() => droppoints.value.filter(d => d.status === 'FULL_AND_NOTIFIED_TO_WAREHOUSE').length);

// Methods
const getFillPercentage = (droppoint: DropPoint) => {
  if (!droppoint.capacity) return 0;
  return Math.min(100, (droppoint.current_empties / droppoint.capacity) * 100);
};

const getFillBarClass = (droppoint: DropPoint) => {
  const percentage = getFillPercentage(droppoint);
  if (percentage >= 90) return 'bg-red-500';
  if (percentage >= 70) return 'bg-amber-500';
  return 'bg-droppoint-500';
};

const getStatusBadgeClass = (status: string) => {
  switch (status) {
    case 'EMPTY': return 'badge-info';
    case 'FULL': return 'badge-warning';
    case 'FULL_AND_NOTIFIED_TO_WAREHOUSE': return 'bg-violet-100 text-violet-700';
    default: return 'badge-neutral';
  }
};

const formatStatus = (status: string) => {
  if (status === 'FULL_AND_NOTIFIED_TO_WAREHOUSE') return 'Notified';
  return status.replace(/_/g, ' ').toLowerCase()
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

const openModal = (droppoint?: DropPoint) => {
  showModal.value = true;
  if (droppoint && droppoint.id) {
    isEditing.value = true;
    editingId.value = droppoint.id;
    formData.value = { ...droppoint };
  } else {
    isEditing.value = false;
    editingId.value = null;
    formData.value = {
      location: '',
      capacity: 100,
      current_empties: 0,
      status: 'EMPTY',
    };
  }
};

const closeModal = () => {
  showModal.value = false;
  isEditing.value = false;
  editingId.value = null;
  formData.value = {
    location: '',
    capacity: 100,
    current_empties: 0,
    status: 'EMPTY',
  };
};

const handleSubmit = async () => {
  try {
    if (isEditing.value && editingId.value) {
      await droppointStore.updateDropPoint(editingId.value, formData.value);
      toast.success(`Drop point "${formData.value.location}" updated successfully`);
    } else {
      await droppointStore.createDropPoint({
        location: formData.value.location!,
        capacity: formData.value.capacity!,
        current_empties: formData.value.current_empties,
        status: 'EMPTY',
      });
      toast.success(`Drop point "${formData.value.location}" created successfully`);
    }
    closeModal();
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Operation failed';
    toast.error(errorMessage);
  }
};

const handleDelete = async (id: number) => {
  if (!confirm('Are you sure you want to delete this drop point?')) return;
  
  try {
    await droppointStore.deleteDropPoint(id);
    toast.success('Drop point deleted successfully');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to delete drop point';
    toast.error(errorMessage);
  }
};

const handleAddEmpty = async (id: number) => {
  try {
    await droppointStore.addEmpties(id);
    toast.success('Empty bottle added to drop point');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to add empty';
    toast.error(errorMessage);
  }
};

const handleNotifyWarehouse = async (id: number) => {
  if (!confirm('Notify warehouse to collect empties?')) return;
  
  try {
    await droppointStore.notifyWarehouse(id);
    toast.info('Warehouse notified successfully');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to notify warehouse';
    toast.error(errorMessage);
  }
};

const handleVerifyTransfer = async (id: number) => {
  if (!confirm('Verify that empties have been transferred to warehouse?')) return;
  
  try {
    await droppointStore.verifyTransfer(id);
    toast.success('Transfer verified successfully');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to verify transfer';
    toast.error(errorMessage);
  }
};

// Lifecycle
onMounted(() => {
  droppointStore.fetchDropPoints();
});
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
