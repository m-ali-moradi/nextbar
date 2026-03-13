<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-content max-w-lg animate-scale-in">
        <!-- Header -->
        <div class="modal-header">
          <h3 class="text-xl font-bold text-slate-900">{{ isEditMode ? 'Edit Bar' : 'Add New Bar' }}</h3>
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
                :class="form.resourceMode === 'NEW' && 'ring-2 ring-bar-500 border-bar-500 text-bar-700'"
                @click="setResourceMode('NEW')"
              >
                Create New
              </button>
              <button
                type="button"
                class="btn-secondary"
                :class="form.resourceMode === 'EXISTING' && 'ring-2 ring-bar-500 border-bar-500 text-bar-700'"
                @click="setResourceMode('EXISTING')"
              >
                Reuse Existing
              </button>
            </div>
          </div>

          <div v-if="!isEditMode && form.resourceMode === 'EXISTING'">
            <label class="label" for="existingBar">Select Existing Bar *</label>
            <select
              id="existingBar"
              v-model="form.existingResourceId"
              class="input"
              :class="errors.existingResourceId && 'border-red-500 focus:ring-red-500'"
              required
            >
              <option value="">Select existing bar</option>
              <option v-for="bar in reusableBars" :key="bar.id" :value="String(bar.id)">
                {{ bar.name }}{{ bar.location ? ` (${bar.location})` : '' }}
              </option>
            </select>
            <p v-if="errors.existingResourceId" class="text-sm text-red-500 mt-1">{{ errors.existingResourceId }}</p>
          </div>

          <!-- Bar Name -->
          <div v-if="isEditMode || form.resourceMode === 'NEW'">
            <label class="label" for="barName">Bar Name *</label>
            <input
              id="barName"
              v-model="form.name"
              type="text"
              class="input"
              :class="errors.name && 'border-red-500 focus:ring-red-500'"
              placeholder="e.g., Main Bar, VIP Bar"
              required
            />
            <p v-if="errors.name" class="text-sm text-red-500 mt-1">{{ errors.name }}</p>
          </div>

          <!-- Location -->
          <div v-if="isEditMode || form.resourceMode === 'NEW'">
            <label class="label" for="barLocation">Location</label>
            <input
              id="barLocation"
              v-model="form.location"
              type="text"
              class="input"
              placeholder="e.g., North Entrance, Center Stage"
            />
          </div>

          <!-- Capacity -->
          <div v-if="isEditMode || form.resourceMode === 'NEW'">
            <label class="label" for="barCapacity">Capacity</label>
            <input
              id="barCapacity"
              v-model.number="form.capacity"
              type="number"
              min="1"
              class="input"
              placeholder="e.g., 100"
            />
            <p class="text-sm text-slate-500 mt-1">Maximum number of guests this bar can serve.</p>
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
                v-for="staff in barAssignableStaff"
                :key="staff.id"
                :value="staff.username"
                :disabled="isAssignedToBar(staff)"
                :class="isAssignedToBar(staff) ? 'text-slate-400 italic' : ''"
              >
                {{
                  (staff.fullName || staff.username) + (
                    isAssignedToBar(staff)
                      ? ` (${staff.assignments[0].role}) assigned to another BAR)`
                      : ((staff.assignments && staff.assignments.length && staff.assignments[0].role)
                          ? ` (${staff.assignments[0].role})`
                          : (Array.isArray(staff.roleAssignments) && staff.roleAssignments.length ? ` (${staff.roleAssignments[0]})` : ''))
                  )
                }}
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
              <i class="fas" :class="isEditMode ? 'fa-save mr-1.5' : 'fa-plus mr-1.5'"></i>
              {{ isEditMode ? 'Save Changes' : 'Add Bar' }}
            </span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue';
import { toast } from 'vue3-toastify';
import { eventApi } from '@/api/eventApi';
import type { EventBar, ResourceMode } from '@/api/eventApi';

interface Props {
  eventId: number;
  bar?: EventBar | null;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  close: [];
  saved: [];
}>();

const isEditMode = computed(() => Boolean(props.bar?.id));

// ============ State ============
const loading = ref(false);
const form = ref({
  name: '',
  location: '',
  capacity: undefined as number | undefined,
  assignedStaff: '',
  resourceMode: 'NEW' as ResourceMode,
  existingResourceId: '',
});
const errors = ref<Record<string, string>>({});
// for staff assignment, we need to know which staff are already assigned to a BAR resource
const availableStaff = ref<Array<{
  id: string;
  username: string;
  fullName?: string;
  assignments?: Array<{ service?: string; role?: string; resourceId?: string }>;
  roleAssignments?: string[];
}>>([]);

const reusableBars = ref<EventBar[]>([]);

// ============ Methods ============
const validate = (): boolean => {
  errors.value = {};

  if (!isEditMode.value && form.value.resourceMode === 'EXISTING') {
    if (!form.value.existingResourceId) {
      errors.value.existingResourceId = 'Please select an existing bar';
      return false;
    }
    return true;
  }
  
  if (!form.value.name.trim()) {
    errors.value.name = 'Bar name is required';
    return false;
  }

  if (form.value.name.length > 100) {
    errors.value.name = 'Bar name must be less than 100 characters';
    return false;
  }

  return true;
};

const setResourceMode = (mode: ResourceMode) => {
  form.value.resourceMode = mode;
  if (mode === 'EXISTING') {
    form.value.name = '';
    form.value.location = '';
    form.value.capacity = undefined;
  } else {
    form.value.existingResourceId = '';
  }
  errors.value = {};
};

const handleSubmit = async () => {
  if (!validate()) return;

  loading.value = true;
  try {
    const assignedStaffId = form.value.assignedStaff
      ? availableStaff.value.find(s => s.username === form.value.assignedStaff)?.id
      : undefined;
    const assignedStaff = assignedStaffId ? [assignedStaffId] : undefined;

    if (props.bar?.id) {
      await eventApi.updateBar(props.bar.id, {
        name: form.value.name.trim(),
        location: form.value.location.trim() || undefined,
        capacity: form.value.capacity || undefined,
        assignedStaff,
      });
    } else {
      await eventApi.createBar({
        name: form.value.resourceMode === 'NEW' ? form.value.name.trim() : undefined,
        location: form.value.resourceMode === 'NEW' ? (form.value.location.trim() || undefined) : undefined,
        eventId: props.eventId,
        capacity: form.value.resourceMode === 'NEW' ? (form.value.capacity || undefined) : undefined,
        assignedStaff,
        resourceMode: form.value.resourceMode,
        existingResourceId: form.value.resourceMode === 'EXISTING' ? form.value.existingResourceId : undefined,
      });
    }
    emit('saved');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to save bar';
    toast.error(errorMessage);
  } finally {
    loading.value = false;
  }
};

const fillFormFromBar = () => {
  if (!props.bar) {
    form.value = {
      name: '',
      location: '',
      capacity: undefined,
      assignedStaff: '',
      resourceMode: 'NEW',
      existingResourceId: '',
    };
    return;
  }

  form.value = {
    name: props.bar.name || '',
    location: props.bar.location || '',
    capacity: props.bar.capacity,
    assignedStaff: props.bar.assignedStaff || '',
    resourceMode: 'NEW',
    existingResourceId: '',
  };
};

// Helper: detect if a user already has a BAR assignment with a resourceId
const isAssignedToBar = (staff: any): boolean => {
  if (!staff) return false;
  const assignments = staff.assignments || [];
  return assignments.some((a: any) => {
    if (!a) return false;
    const svc = a.service || a.serviceName || '';
    const res = a.resourceId;
    return typeof svc === 'string' && svc.startsWith('BAR') && res != null && String(res).toUpperCase() !== 'NONE';
  });
};

// Only show users who are BAR staff (non-admin)
const barAssignableStaff = computed(() => {
  return availableStaff.value.filter((s: any) => {
    const assignments = s?.assignments;
    if (!Array.isArray(assignments) || !assignments.length) return false;

    const isAdmin = assignments.some((a: any) => {
      const role = (a?.role || '').toString().toUpperCase();
      return role === 'ADMIN';
    });
    if (isAdmin) return false;

    const hasBarService = assignments.some((a: any) => {
      const svc = (a?.service || a?.serviceName || '').toString().toUpperCase();
      return svc.startsWith('BAR');
    });
    if (!hasBarService) return false;

    // legacy check: roleAssignments may be an array of strings
    if (Array.isArray(s.roleAssignments) && s.roleAssignments.length) {
      const legacyRole = String(s.roleAssignments[0] || '').toUpperCase();
      if (legacyRole === 'ADMIN') return false;
    }

    return true;
  });
});

// ============ Lifecycle ============
onMounted(async () => {
  fillFormFromBar();
  try {
    const [staffResponse, allBarsResponse] = await Promise.all([
      eventApi.getAvailableBarStaff(),
      eventApi.getAllBars(),
    ]);
    availableStaff.value = staffResponse.data || [];
    reusableBars.value = (allBarsResponse.data || []).filter(bar => bar.eventId !== props.eventId);
  } catch (err) {
    console.error('Failed to fetch staff:', err);
  }
});

watch(() => props.bar, () => {
  fillFormFromBar();
}, { deep: true });
</script>
