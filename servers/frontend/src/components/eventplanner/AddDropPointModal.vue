<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-content max-w-lg animate-scale-in">
        <!-- Header -->
        <div class="modal-header">
          <h3 class="text-xl font-bold text-slate-900">{{ isEditMode ? 'Edit Drop Point' : 'Add Drop Point' }}</h3>
          <button 
            @click="$emit('close')"
            class="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <!-- Body -->
        <form @submit.prevent="handleSubmit" class="modal-body space-y-5">
          <div v-if="!isEditMode" class="space-y-2">
            <label class="label">Resource Mode *</label>
            <div class="grid grid-cols-2 gap-2">
              <button
                type="button"
                class="btn-secondary"
                :class="form.resourceMode === 'NEW' && 'ring-2 ring-droppoint-500 border-droppoint-500 text-droppoint-700'"
                @click="setResourceMode('NEW')"
              >
                Create New
              </button>
              <button
                type="button"
                class="btn-secondary"
                :class="form.resourceMode === 'EXISTING' && 'ring-2 ring-droppoint-500 border-droppoint-500 text-droppoint-700'"
                @click="setResourceMode('EXISTING')"
              >
                Reuse Existing
              </button>
            </div>
          </div>

          <div v-if="!isEditMode && form.resourceMode === 'EXISTING'">
            <label class="label" for="existingDropPoint">Select Existing Drop Point *</label>
            <select
              id="existingDropPoint"
              v-model="form.existingResourceId"
              class="input"
              :class="errors.existingResourceId && 'border-red-500 focus:ring-red-500'"
              required
            >
              <option value="">Select existing drop point</option>
              <option v-for="dp in reusableDropPoints" :key="dp.id" :value="String(dp.id)">
                {{ dp.name }}{{ dp.location ? ` (${dp.location})` : '' }}
              </option>
            </select>
            <p v-if="errors.existingResourceId" class="text-sm text-red-500 mt-1">{{ errors.existingResourceId }}</p>
          </div>

          <!-- Drop Point Name -->
          <div v-if="isEditMode || form.resourceMode === 'NEW'">
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
          <div v-if="isEditMode || form.resourceMode === 'NEW'">
            <label class="label" for="dpLocation">Location</label>
            <input
              id="dpLocation"
              v-model="form.location"
              type="text"
              class="input"
              placeholder="e.g., Near Main Stage, West Side"
            />
          </div>

          <div v-if="isEditMode || form.resourceMode === 'NEW'">
            <label class="label" for="dpCapacity">Capacity *</label>
            <input
              id="dpCapacity"
              v-model.number="form.capacity"
              type="number"
              min="1"
              class="input"
              :class="errors.capacity && 'border-red-500 focus:ring-red-500'"
              placeholder="e.g., 150"
              required
            />
            <p v-if="errors.capacity" class="text-sm text-red-500 mt-1">{{ errors.capacity }}</p>
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
              <option
                v-for="staff in dropPointAssignableStaff"
                :key="staff.id"
                :value="staff.username"
                :disabled="isAssignedToDropPoint(staff)"
              >
                {{ (staff.fullName || staff.username) + (getFirstRole(staff) ? ' (' + getFirstRole(staff) + ')' : '') + (isAssignedToDropPoint(staff) ? ' — assigned' : '') }}
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
              {{ isEditMode ? 'Saving...' : 'Adding...' }}
            </span>
            <span v-else>
              <i :class="isEditMode ? 'fas fa-save mr-1.5' : 'fas fa-plus mr-1.5'"></i>
              {{ isEditMode ? 'Save Changes' : 'Add Drop Point' }}
            </span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { toast } from 'vue3-toastify';
import { eventApi } from '@/api/eventApi';
import { authApi } from '@/api/authApi';
import type { EventDropPoint, ResourceMode } from '@/api/eventApi';

interface Props {
  eventId: number;
  dropPoint?: EventDropPoint | null;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  close: [];
  saved: [];
}>();

const isEditMode = computed(() => !!props.dropPoint?.id);

// ============ State ============
const loading = ref(false);
const form = ref({
  name: '',
  location: '',
  assignedStaff: '',
  capacity: 100,
  resourceMode: 'NEW' as ResourceMode,
  existingResourceId: '',
});
const errors = ref<Record<string, string>>({});
type Assignment = { assignmentId?: string; service?: string; role?: string; resourceId?: string };
const availableStaff = ref<Array<{ id: string; username: string; fullName?: string; assignments?: Assignment[]; roleAssignments?: string[] }>>([]);
const reusableDropPoints = ref<EventDropPoint[]>([]);

const getFirstRole = (staff: { assignments?: Assignment[]; roleAssignments?: string[] }) => {
  return staff.assignments?.[0]?.role || (staff.roleAssignments && staff.roleAssignments[0]) || '';
};

// detect if a user already has any resource assignment (resourceId present)
// Only resourceId is considered here; service/role filtering is handled on the backend.
const isAssignedToDropPoint = (staff: { assignments?: Assignment[] }) => {
  const assignments = staff.assignments || [];
  return assignments.some(a => a.resourceId && String(a.resourceId).toUpperCase() !== 'NONE');
};

// Only show users who are DROP_POINT staff (non-admin)
const dropPointAssignableStaff = computed(() => {
  return (availableStaff.value || []).filter((s: any) => {
    const assignments = s?.assignments;
    if (!Array.isArray(assignments) || !assignments.length) return false;

    const isAdmin = assignments.some((a: any) => {
      const role = (a?.role || '').toString().toUpperCase();
      return role === 'ADMIN';
    });
    if (isAdmin) return false;

    const hasDropPointService = assignments.some((a: any) => {
      const svc = (a?.service || a?.serviceName || '').toString().toUpperCase();
      return svc.startsWith('DROP') || svc.includes('DROP_POINT');
    });
    if (!hasDropPointService) return false;

    if (Array.isArray(s.roleAssignments) && s.roleAssignments.length) {
      const legacyRole = String(s.roleAssignments[0] || '').toUpperCase();
      if (legacyRole === 'ADMIN') return false;
    }

    return true;
  });
});

// ============ Methods ============
const validate = (): boolean => {
  errors.value = {};

  if (!isEditMode.value && form.value.resourceMode === 'EXISTING') {
    if (!form.value.existingResourceId) {
      errors.value.existingResourceId = 'Please select an existing drop point';
      return false;
    }
    return true;
  }
  
  if (!form.value.name.trim()) {
    errors.value.name = 'Drop point name is required';
    return false;
  }

  if (form.value.name.length > 100) {
    errors.value.name = 'Drop point name must be less than 100 characters';
    return false;
  }

  if (!Number.isInteger(form.value.capacity) || form.value.capacity <= 0) {
    errors.value.capacity = 'Capacity must be a whole number greater than 0';
    return false;
  }

  return true;
};

const setResourceMode = (mode: ResourceMode) => {
  form.value.resourceMode = mode;
  if (mode === 'EXISTING') {
    form.value.name = '';
    form.value.location = '';
    form.value.capacity = 100;
  } else {
    form.value.existingResourceId = '';
  }
  errors.value = {};
};

const handleSubmit = async () => {
  if (!validate()) return;

  loading.value = true;
  try {
    if (isEditMode.value && props.dropPoint?.id) {
      await eventApi.updateDropPoint(props.dropPoint.id, {
        name: form.value.name.trim(),
        location: form.value.location.trim() || undefined,
        assignedStaff: form.value.assignedStaff || undefined,
        capacity: form.value.capacity,
      });
    } else {
      await eventApi.createDropPoint({
        name: form.value.resourceMode === 'NEW' ? form.value.name.trim() : undefined,
        location: form.value.resourceMode === 'NEW' ? (form.value.location.trim() || undefined) : undefined,
        eventId: props.eventId,
        assignedStaff: form.value.assignedStaff || undefined,
        capacity: form.value.resourceMode === 'NEW' ? form.value.capacity : undefined,
        resourceMode: form.value.resourceMode,
        existingResourceId: form.value.resourceMode === 'EXISTING' ? form.value.existingResourceId : undefined,
      });
    }
    emit('saved');
  } catch (err) {
    const errorMessage = err instanceof Error
      ? err.message
      : (isEditMode.value ? 'Failed to update drop point' : 'Failed to add drop point');
    toast.error(errorMessage);
  } finally {
    loading.value = false;
  }
};

// ============ Lifecycle ============
onMounted(async () => {
  if (props.dropPoint) {
    form.value.name = props.dropPoint.name || '';
    form.value.location = props.dropPoint.location || '';
    form.value.assignedStaff = props.dropPoint.assignedStaff || '';
    form.value.capacity = props.dropPoint.capacity ?? 100;
    form.value.resourceMode = 'NEW';
    form.value.existingResourceId = '';
  }

  try {
    const [staffResponse, allDropPointsResponse] = await Promise.all([
      // Fetch all users for drop point assignment (backend will handle filtering)
      authApi.getUsers(),
      eventApi.getAllDropPoints(),
    ]);
    availableStaff.value = staffResponse.data || [];
    reusableDropPoints.value = (allDropPointsResponse.data || []).filter(dp => dp.eventId !== props.eventId);
  } catch (err) {
    console.error('Failed to fetch staff:', err);
  }
});
</script>
