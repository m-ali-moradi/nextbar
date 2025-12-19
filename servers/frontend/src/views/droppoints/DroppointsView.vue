<template>
  <div class="droppoints-container">
    <header class="page-header">
      <h1 class="text-3xl font-bold text-gray-800">Manage Drop Points</h1>
      <button @click="openModal()" class="btn-primary">
        <span class="text-lg">+</span> New Drop Point
      </button>
    </header>

    <!-- Loading State -->
    <div v-if="loading" class="loading-state">
      <p>Loading drop points...</p>
    </div>

    <!-- Error State -->
    <div v-if="error" class="error-banner">
      {{ error }}
    </div>

    <!-- Drop Points Table -->
    <div v-if="!loading && droppoints.length > 0" class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Location</th>
            <th>Capacity</th>
            <th>Current Empties</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="droppoint in droppoints" :key="droppoint.id">
            <td>{{ droppoint.id }}</td>
            <td>{{ droppoint.location }}</td>
            <td>{{ droppoint.capacity }}</td>
            <td>{{ droppoint.current_empties }}</td>
            <td>
              <span 
                class="status-badge" 
                :class="getStatusClass(droppoint.status)"
              >
                {{ formatStatus(droppoint.status) }}
              </span>
            </td>
            <td class="actions-cell">
              <button @click="openModal(droppoint)" class="btn-edit">
                Edit
              </button>
              <button @click="handleDelete(droppoint.id!)" class="btn-delete">
                Delete
              </button>
              <button 
                v-if="droppoint.status === 'EMPTY'" 
                @click="handleAddEmpty(droppoint.id!)" 
                class="btn-action"
              >
                Add Empty
              </button>
              <button 
                v-if="droppoint.status === 'FULL'" 
                @click="handleNotifyWarehouse(droppoint.id!)" 
                class="btn-warning"
              >
                Notify Warehouse
              </button>
              <button 
                v-if="droppoint.status === 'FULL_AND_NOTIFIED_TO_WAREHOUSE'" 
                @click="handleVerifyTransfer(droppoint.id!)" 
                class="btn-success"
              >
                Verify Transfer
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && droppoints.length === 0" class="empty-state">
      <p>No drop points found. Create one to get started!</p>
    </div>

    <!-- Modal Form -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Edit Drop Point' : 'New Drop Point' }}</h2>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>

        <form @submit.prevent="handleSubmit" class="modal-form">
          <div class="form-group">
            <label for="location">Location</label>
            <input 
              id="location" 
              v-model="formData.location" 
              type="text" 
              required 
              placeholder="e.g., Main Entrance"
            />
          </div>

          <div class="form-group">
            <label for="capacity">Capacity</label>
            <input 
              id="capacity" 
              v-model.number="formData.capacity" 
              type="number" 
              required 
              min="1"
              placeholder="Maximum bottle capacity"
            />
          </div>

          <div v-if="isEditing" class="form-group">
            <label for="current_empties">Current Empties</label>
            <input 
              id="current_empties" 
              v-model.number="formData.current_empties" 
              type="number" 
              min="0"
              :max="formData.capacity"
            />
          </div>

          <div v-if="isEditing" class="form-group">
            <label for="status">Status</label>
            <select id="status" v-model="formData.status">
              <option value="EMPTY">Empty</option>
              <option value="FULL">Full</option>
              <option value="FULL_AND_NOTIFIED_TO_WAREHOUSE">Full & Notified</option>
            </select>
          </div>

          <div class="modal-actions">
            <button type="submit" class="btn-primary">
              {{ isEditing ? 'Update' : 'Create' }}
            </button>
            <button type="button" @click="closeModal" class="btn-secondary">
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useDroppointStore } from '@/stores/droppointStore';
import { DropPoint } from '@/api/droppointApi';
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';

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

// Methods
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

const getStatusClass = (status: string) => {
  switch (status) {
    case 'EMPTY': return 'status-empty';
    case 'FULL': return 'status-full';
    case 'FULL_AND_NOTIFIED_TO_WAREHOUSE': return 'status-notified';
    default: return '';
  }
};

const formatStatus = (status: string) => {
  return status.replace(/_/g, ' ').toLowerCase()
    .split(' ')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

// Lifecycle
onMounted(() => {
  droppointStore.fetchDropPoints();
});
</script>

<style scoped>
.droppoints-container {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.btn-primary {
  background-color: #3b82f6;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  border: none;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: background-color 0.2s;
}

.btn-primary:hover {
  background-color: #2563eb;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
}

.error-banner {
  background-color: #fee2e2;
  color: #991b1b;
  padding: 1rem;
  border-radius: 0.5rem;
  margin-bottom: 1rem;
}

.table-container {
  background: white;
  border-radius: 0.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead {
  background-color: #f9fafb;
  border-bottom: 2px solid #e5e7eb;
}

.data-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.data-table td {
  padding: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.data-table tbody tr:hover {
  background-color: #f9fafb;
}

.status-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-empty {
  background-color: #dbeafe;
  color: #1e40af;
}

.status-full {
  background-color: #fef3c7;
  color: #92400e;
}

.status-notified {
  background-color: #fce7f3;
  color: #831843;
}

.actions-cell {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.btn-edit, .btn-delete, .btn-action, .btn-warning, .btn-success, .btn-secondary {
  padding: 0.375rem 0.75rem;
  border-radius: 0.375rem;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-edit {
  background-color: #3b82f6;
  color: white;
}

.btn-edit:hover {
  background-color: #2563eb;
}

.btn-delete {
  background-color: #ef4444;
  color: white;
}

.btn-delete:hover {
  background-color: #dc2626;
}

.btn-action {
  background-color: #10b981;
  color: white;
}

.btn-action:hover {
  background-color: #059669;
}

.btn-warning {
  background-color: #f59e0b;
  color: white;
}

.btn-warning:hover {
  background-color: #d97706;
}

.btn-success {
  background-color: #8b5cf6;
  color: white;
}

.btn-success:hover {
  background-color: #7c3aed;
}

.btn-secondary {
  background-color: #e5e7eb;
  color: #374151;
}

.btn-secondary:hover {
  background-color: #d1d5db;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 0.75rem;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: #6b7280;
  cursor: pointer;
  padding: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.375rem;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #f3f4f6;
}

.modal-form {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(147, 197, 253, 0.5);
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.modal-actions button {
  flex: 1;
  padding: 0.75rem;
  border-radius: 0.5rem;
  font-weight: 600;
}
</style>
