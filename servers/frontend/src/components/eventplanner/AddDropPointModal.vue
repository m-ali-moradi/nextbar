<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-content max-w-lg animate-scale-in">
        <!-- Header -->
        <div class="modal-header">
          <h3 class="text-xl font-bold text-slate-900">Add Drop Point</h3>
          <button 
            @click="$emit('close')"
            class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <!-- Body -->
        <form @submit.prevent="handleSubmit" class="modal-body space-y-5">
          <!-- Drop Point Name -->
          <div>
            <label class="label" for="dpName">Drop Point Name *</label>
            <input
              id="dpName"
              v-model="form.name"
              type="text"
              class="input"
              :class="errors.name && 'border-red-500 focus:ring-red-500'"
              placeholder="e.g., Entrance Gate, Exit A"
              required
            />
            <p v-if="errors.name" class="text-sm text-red-500 mt-1">{{ errors.name }}</p>
          </div>

          <!-- Location -->
          <div>
            <label class="label" for="dpLocation">Location</label>
            <input
              id="dpLocation"
              v-model="form.location"
              type="text"
              class="input"
              placeholder="e.g., Near Main Stage, West Side"
            />
          </div>

          <!-- Staff Assignment -->
          <div>
            <label class="label" for="staffAssignment">Assign Staff (Optional)</label>
            <select
              id="staffAssignment"
              v-model="form.assignedStaff"
              class="input"
            >
              <option value="">Select staff member</option>
              <option v-for="staff in availableStaff" :key="staff.id" :value="staff.username">
                {{ staff.fullName || staff.username }}
              </option>
            </select>
            <p class="text-sm text-slate-500 mt-1">You can assign staff later from the event details.</p>
          </div>
        </form>
        
        <!-- Footer -->
        <div class="modal-footer justify-end">
          <button 
            type="button"
            @click="$emit('close')"
            class="btn-secondary"
            :disabled="loading"
          >
            Cancel
          </button>
          <button 
            @click="handleSubmit"
            class="btn-primary"
            :disabled="loading"
          >
            <span v-if="loading" class="flex items-center gap-2">
              <i class="fas fa-spinner animate-spin"></i>
              Adding...
            </span>
            <span v-else>
              <i class="fas fa-plus mr-1.5"></i>
              Add Drop Point
            </span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useEventStore } from '@/stores/eventStore';
import { toast } from 'vue3-toastify';

interface Props {
  eventId: number;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  close: [];
  saved: [];
}>();

const eventStore = useEventStore();

// ============ State ============
const loading = ref(false);
const form = ref({
  name: '',
  location: '',
  assignedStaff: '',
});
const errors = ref<Record<string, string>>({});
const availableStaff = ref<Array<{ id: string; username: string; fullName?: string }>>([]);

// ============ Methods ============
const validate = (): boolean => {
  errors.value = {};
  
  if (!form.value.name.trim()) {
    errors.value.name = 'Drop point name is required';
    return false;
  }

  if (form.value.name.length > 100) {
    errors.value.name = 'Drop point name must be less than 100 characters';
    return false;
  }

  return true;
};

const handleSubmit = async () => {
  if (!validate()) return;

  loading.value = true;
  try {
    await eventStore.createDropPoint({
      name: form.value.name.trim(),
      location: form.value.location.trim() || undefined,
      eventId: props.eventId,
      assignedStaff: form.value.assignedStaff || undefined,
    });
    emit('saved');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to add drop point';
    toast.error(errorMessage);
  } finally {
    loading.value = false;
  }
};

// ============ Lifecycle ============
onMounted(async () => {
  try {
    const staff = await eventStore.fetchAvailableStaff();
    availableStaff.value = staff;
  } catch (err) {
    console.error('Failed to fetch staff:', err);
  }
});
</script>
