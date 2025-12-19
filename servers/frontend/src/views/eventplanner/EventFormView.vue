<template>
  <div class="event-form-container">
    <header class="page-header">
      <h1 class="text-3xl font-bold text-gray-800">
        {{ isEdit ? 'Edit Event' : 'Create Event' }}
      </h1>
      <button @click="goBack" class="btn-secondary">
        ← Back to Events
      </button>
    </header>

    <form @submit.prevent="handleSubmit" class="event-form">
      <!-- Basic Details Section -->
      <section class="form-section">
        <h2 class="section-title">Basic Details</h2>

        <div class="form-grid">
          <div class="form-group">
            <label for="name">Event Name *</label>
            <input
              id="name"
              v-model="formData.name"
              type="text"
              required
              placeholder="e.g., Summer Festival 2025"
            />
          </div>

          <div class="form-group">
            <label for="date">Date *</label>
            <input
              id="date"
              v-model="formData.date"
              type="date"
              required
            />
          </div>

          <div class="form-group">
            <label for="location">Location *</label>
            <input
              id="location"
              v-model="formData.location"
              type="text"
              required
              placeholder="e.g., Central Park"
            />
          </div>

          <div class="form-group">
            <label for="duration">Duration (hours) *</label>
            <input
              id="duration"
              v-model.number="formData.duration"
              type="number"
              required
              min="1"
              placeholder="e.g., 4"
            />
          </div>

          <div class="form-group">
            <label for="status">Status *</label>
            <select id="status" v-model="formData.status" required>
              <option value="">Select status</option>
              <option value="PLANNED">Planned</option>
              <option value="ONGOING">Ongoing</option>
              <option value="COMPLETED">Completed</option>
            </select>
          </div>
        </div>
      </section>

      <!-- Beverages Section -->
      <section class="form-section">
        <h2 class="section-title">Select Beverages *</h2>
        <div class="beverages-grid">
          <div
            v-for="beverage in availableBeverages"
            :key="beverage.id"
            class="checkbox-item"
          >
            <label>
              <input
                type="checkbox"
                :value="beverage.id"
                v-model="selectedBeverageIds"
              />
              <span>{{ beverage.name }} (€{{ beverage.price }})</span>
            </label>
          </div>
        </div>
        <p v-if="selectedBeverageIds.length === 0" class="warning-text">
          Please select at least one beverage
        </p>
      </section>

      <!-- Bars Section -->
      <section class="form-section">
        <div class="section-header">
          <h2 class="section-title">Bars Configuration</h2>
          <button type="button" @click="addBar" class="btn-add">
            + Add Bar
          </button>
        </div>

        <p v-if="formData.bars.length === 0" class="warning-text">
          Add at least one bar for the event
        </p>

        <div v-for="(bar, index) in formData.bars" :key="index" class="bar-block">
          <div class="block-header">
            <h3>Bar {{ index + 1 }}</h3>
            <button type="button" @click="removeBar(index)" class="btn-remove">
              × Remove
            </button>
          </div>

          <div class="form-grid">
            <div class="form-group">
              <label :for="'barName-' + index">Bar Name *</label>
              <input
                :id="'barName-' + index"
                v-model="bar.barName"
                type="text"
                required
                placeholder="e.g., Main Bar"
              />
            </div>

            <div class="form-group">
              <label :for="'barLocation-' + index">Location *</label>
              <input
                :id="'barLocation-' + index"
                v-model="bar.location"
                type="text"
                required
                placeholder="e.g., North Side"
              />
            </div>

            <div class="form-group">
              <label :for="'barCapacity-' + index">Total Capacity *</label>
              <input
                :id="'barCapacity-' + index"
                v-model.number="bar.totalCapacity"
                type="number"
                required
                min="0"
                placeholder="e.g., 500"
              />
            </div>
          </div>

          <!-- Beverage Stock for this bar -->
          <div v-if="selectedBeverageIds.length > 0" class="beverage-stock">
            <h4>Beverage Stock</h4>
            <div class="stock-grid">
              <div
                v-for="bevId in selectedBeverageIds"
                :key="bevId"
                class="stock-item"
              >
                <label>{{ getBeverageName(bevId) }}</label>
                <input
                  v-model.number="bar.beverageStock[bevId]"
                  type="number"
                  min="0"
                  placeholder="Quantity"
                />
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Drop Points Section -->
      <section class="form-section">
        <div class="section-header">
          <h2 class="section-title">Drop Points</h2>
          <button type="button" @click="addDropPoint" class="btn-add">
            + Add Drop Point
          </button>
        </div>

        <p v-if="formData.dropPoints.length === 0" class="warning-text">
          Add at least one drop point for the event
        </p>

        <div
          v-for="(dropPoint, index) in formData.dropPoints"
          :key="index"
          class="droppoint-block"
        >
          <div class="block-header">
            <h3>Drop Point {{ index + 1 }}</h3>
            <button type="button" @click="removeDropPoint(index)" class="btn-remove">
              × Remove
            </button>
          </div>

          <div class="form-grid">
            <div class="form-group">
              <label :for="'dpLocation-' + index">Location *</label>
              <input
                :id="'dpLocation-' + index"
                v-model="dropPoint.location"
                type="text"
                required
                placeholder="e.g., Entrance Gate"
              />
            </div>

            <div class="form-group">
              <label :for="'dpCapacity-' + index">Capacity *</label>
              <input
                :id="'dpCapacity-' + index"
                v-model.number="dropPoint.capacity"
                type="number"
                required
                min="0"
                placeholder="e.g., 100"
              />
            </div>
          </div>
        </div>
      </section>

      <!-- Form Actions -->
      <div class="form-actions">
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? 'Saving...' : isEdit ? 'Update Event' : 'Create Event' }}
        </button>
        <button type="button" @click="goBack" class="btn-secondary">
          Cancel
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useEventStore } from '@/stores/eventStore';
import { EventBar, EventDropPoint } from '@/api/eventApi';
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';

const router = useRouter();
const route = useRoute();
const eventStore = useEventStore();

// State
const isEdit = computed(() => !!route.params.id);
const selectedBeverageIds = ref<number[]>([]);
const formData = ref({
  name: '',
  date: '',
  location: '',
  duration: 1,
  status: '' as 'PLANNED' | 'ONGOING' | 'COMPLETED' | '',
  bars: [] as EventBar[],
  dropPoints: [] as EventDropPoint[],
});

// Computed
const availableBeverages = computed(() => eventStore.beverages);
const loading = computed(() => eventStore.loading);

// Methods
const getBeverageName = (id: number) => {
  const beverage = availableBeverages.value.find(b => b.id === id);
  return beverage ? beverage.name : `Beverage ${id}`;
};

const addBar = () => {
  const beverageStock: { [key: number]: number } = {};
  selectedBeverageIds.value.forEach(id => {
    beverageStock[id] = 0;
  });

  formData.value.bars.push({
    barName: '',
    location: '',
    totalCapacity: 0,
    beverageStock,
  });
};

const removeBar = (index: number) => {
  formData.value.bars.splice(index, 1);
};

const addDropPoint = () => {
  formData.value.dropPoints.push({
    location: '',
    capacity: 0,
  });
};

const removeDropPoint = (index: number) => {
  formData.value.dropPoints.splice(index, 1);
};

const handleSubmit = async () => {
  if (selectedBeverageIds.value.length === 0) {
    toast.error('Please select at least one beverage');
    return;
  }

  if (formData.value.bars.length === 0) {
    toast.error('Please add at least one bar');
    return;
  }

  if (formData.value.dropPoints.length === 0) {
    toast.error('Please add at least one drop point');
    return;
  }

  const beverages = selectedBeverageIds.value.map(id =>
    availableBeverages.value.find(b => b.id === id)!
  );

  const payload = {
    name: formData.value.name,
    date: formData.value.date,
    location: formData.value.location,
    duration: formData.value.duration,
    status: formData.value.status as 'PLANNED' | 'ONGOING' | 'COMPLETED',
    beverages,
    bars: formData.value.bars,
    dropPoints: formData.value.dropPoints,
  };

  try {
    if (isEdit.value) {
      await eventStore.updateEvent(Number(route.params.id), payload);
      toast.success('Event updated successfully');
    } else {
      await eventStore.createEvent(payload);
      toast.success('Event created successfully');
    }
    router.push('/events');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to save event';
    toast.error(errorMessage);
  }
};

const goBack = () => {
  router.push('/events');
};

// Watch for beverage selection changes to update bar stocks
watch(selectedBeverageIds, (newIds, oldIds) => {
  formData.value.bars.forEach(bar => {
    // Add new beverages with 0 quantity
    newIds.forEach(id => {
      if (!(id in bar.beverageStock)) {
        bar.beverageStock[id] = 0;
      }
    });

    // Remove deselected beverages
    oldIds?.forEach(id => {
      if (!newIds.includes(id)) {
        delete bar.beverageStock[id];
      }
    });
  });
});

// Lifecycle
onMounted(async () => {
  await eventStore.fetchBeverages();

  if (isEdit.value) {
    try {
      const event = await eventStore.fetchEvent(Number(route.params.id));
      formData.value = {
        name: event.name,
        date: event.date,
        location: event.location,
        duration: event.duration,
        status: event.status,
        bars: event.bars || [],
        dropPoints: event.dropPoints || [],
      };
      selectedBeverageIds.value = event.beverages?.map(b => b.id) || [];
    } catch (err) {
      toast.error('Failed to load event');
      router.push('/events');
    }
  }
});
</script>

<style scoped>
.event-form-container {
  padding: 2rem;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.event-form {
  background: white;
  border-radius: 0.75rem;
  padding: 2rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.form-section {
  margin-bottom: 2.5rem;
  padding-bottom: 2rem;
  border-bottom: 2px solid #e5e7eb;
}

.form-section:last-of-type {
  border-bottom: none;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 1.5rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group select {
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

.beverages-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.checkbox-item label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.checkbox-item input[type="checkbox"] {
  width: 1.25rem;
  height: 1.25rem;
  cursor: pointer;
}

.warning-text {
  color: #dc2626;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

.bar-block,
.droppoint-block {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.block-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
}

.beverage-stock {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #d1d5db;
}

.beverage-stock h4 {
  font-weight: 600;
  margin-bottom: 1rem;
  color: #374151;
}

.stock-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
}

.stock-item {
  display: flex;
  flex-direction: column;
}

.stock-item label {
  font-size: 0.875rem;
  margin-bottom: 0.25rem;
  color: #6b7280;
}

.stock-item input {
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
}

.btn-primary,
.btn-secondary,
.btn-add,
.btn-remove {
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  border: none;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-primary {
  background-color: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #2563eb;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #e5e7eb;
  color: #374151;
}

.btn-secondary:hover {
  background-color: #d1d5db;
}

.btn-add {
  background-color: #10b981;
  color: white;
  padding: 0.5rem 1rem;
}

.btn-add:hover {
  background-color: #059669;
}

.btn-remove {
  background-color: #ef4444;
  color: white;
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
}

.btn-remove:hover {
  background-color: #dc2626;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  margin-top: 2rem;
}
</style>
