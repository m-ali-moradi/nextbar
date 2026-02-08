<template>
  <div class="flex min-h-screen bg-slate-50">
    <Sidebar />
    
    <div class="flex-1 ml-72">
      <Navbar />

      <main class="p-6 lg:p-8 max-w-4xl mx-auto">
        <!-- Page Header -->
        <div class="flex items-center gap-4 mb-8">
          <button 
            @click="goBack"
            class="p-3 rounded-xl bg-white border border-slate-200 text-slate-600 hover:bg-slate-50 transition-colors"
          >
            <i class="fas fa-arrow-left"></i>
          </button>
          <div>
            <h1 class="text-3xl font-bold text-slate-900">
              {{ isEdit ? 'Edit Event' : 'Create Event' }}
            </h1>
            <p class="text-slate-500 mt-1">
              {{ isEdit ? 'Update event details and settings' : 'Fill in the details to create a new event' }}
            </p>
          </div>
        </div>

        <!-- Progress Steps -->
        <div class="mb-8">
          <div class="flex items-center justify-between">
            <button
              v-for="(step, i) in steps"
              :key="step.id"
              @click="goToStep(i)"
              class="flex items-center gap-3 group"
              :class="i < steps.length - 1 && 'flex-1'"
            >
              <div 
                class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold transition-colors"
                :class="getStepClass(i)"
              >
                <i v-if="i < currentStep" class="fas fa-check"></i>
                <span v-else>{{ i + 1 }}</span>
              </div>
              <span 
                class="hidden sm:block text-sm font-semibold transition-colors"
                :class="i === currentStep ? 'text-event-600' : 'text-slate-500'"
              >
                {{ step.title }}
              </span>
              <div 
                v-if="i < steps.length - 1"
                class="hidden lg:block flex-1 h-0.5 mx-4"
                :class="i < currentStep ? 'bg-event-500' : 'bg-slate-200'"
              ></div>
            </button>
          </div>
        </div>

        <!-- Form Card -->
        <div class="card p-6 lg:p-8">
          <form @submit.prevent="handleSubmit">
            <!-- Step 1: Basic Details -->
            <div v-show="currentStep === 0" class="space-y-6 animate-fade-in">
              <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <!-- Event Name -->
                <div class="lg:col-span-2">
                  <label class="label" for="name">Event Name *</label>
                  <input
                    id="name"
                    v-model="form.name"
                    type="text"
                    class="input"
                    :class="errors.name && 'border-red-500 focus:ring-red-500'"
                    placeholder="e.g., Summer Music Festival 2025"
                    required
                  />
                  <p v-if="errors.name" class="text-sm text-red-500 mt-1">{{ errors.name }}</p>
                </div>

                <!-- Date -->
                <div>
                  <label class="label" for="date">Event Date *</label>
                  <input
                    id="date"
                    v-model="form.date"
                    type="date"
                    class="input"
                    :class="errors.date && 'border-red-500 focus:ring-red-500'"
                    :min="minDate"
                    required
                  />
                  <p v-if="errors.date" class="text-sm text-red-500 mt-1">{{ errors.date }}</p>
                </div>

                <!-- Location -->
                <div>
                  <label class="label" for="location">Location</label>
                  <input
                    id="location"
                    v-model="form.location"
                    type="text"
                    class="input"
                    placeholder="e.g., Central Park, Convention Center"
                  />
                </div>

                <!-- Description -->
                <div class="lg:col-span-2">
                  <label class="label" for="description">Description</label>
                  <textarea
                    id="description"
                    v-model="form.description"
                    rows="4"
                    class="input resize-none"
                    placeholder="Describe what this event is about..."
                  ></textarea>
                </div>
              </div>
            </div>

            <!-- Step 2: Organizer & Capacity -->
            <div v-show="currentStep === 1" class="space-y-6 animate-fade-in">
              <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <!-- Organizer Name -->
                <div>
                  <label class="label" for="organizerName">Organizer Name</label>
                  <input
                    id="organizerName"
                    v-model="form.organizerName"
                    type="text"
                    class="input"
                    placeholder="e.g., John Smith"
                  />
                </div>

                <!-- Organizer Email -->
                <div>
                  <label class="label" for="organizerEmail">Organizer Email</label>
                  <input
                    id="organizerEmail"
                    v-model="form.organizerEmail"
                    type="email"
                    class="input"
                    :class="errors.organizerEmail && 'border-red-500 focus:ring-red-500'"
                    placeholder="e.g., organizer@example.com"
                  />
                  <p v-if="errors.organizerEmail" class="text-sm text-red-500 mt-1">{{ errors.organizerEmail }}</p>
                </div>

                <!-- Organizer Phone -->
                <div>
                  <label class="label" for="organizerPhone">Organizer Phone</label>
                  <input
                    id="organizerPhone"
                    v-model="form.organizerPhone"
                    type="tel"
                    class="input"
                    placeholder="e.g., +1 (555) 123-4567"
                  />
                </div>

                <!-- Expected Attendees -->
                <div>
                  <label class="label" for="expectedAttendees">Expected Attendees</label>
                  <input
                    id="expectedAttendees"
                    v-model.number="form.expectedAttendees"
                    type="number"
                    min="0"
                    class="input"
                    placeholder="e.g., 500"
                  />
                </div>

                <!-- Max Capacity -->
                <div>
                  <label class="label" for="maxCapacity">Max Capacity</label>
                  <input
                    id="maxCapacity"
                    v-model.number="form.maxCapacity"
                    type="number"
                    min="0"
                    class="input"
                    placeholder="e.g., 1000"
                  />
                </div>
              </div>
            </div>

            <!-- Step 3: Settings -->
            <div v-show="currentStep === 2" class="space-y-6 animate-fade-in">
              <!-- Visibility Toggle -->
              <div class="p-6 bg-slate-50 rounded-xl border border-slate-200">
                <div class="flex items-center justify-between">
                  <div>
                    <h4 class="font-semibold text-slate-900">Event Visibility</h4>
                    <p class="text-sm text-slate-500 mt-1">
                      {{ form.isPublic 
                        ? 'This event is visible to everyone' 
                        : 'This event is private and only visible to invited attendees' 
                      }}
                    </p>
                  </div>
                  <button
                    type="button"
                    @click="form.isPublic = !form.isPublic"
                    class="relative inline-flex h-7 w-14 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-event-500 focus:ring-offset-2"
                    :class="form.isPublic ? 'bg-event-600' : 'bg-slate-300'"
                  >
                    <span
                      class="inline-block h-5 w-5 transform rounded-full bg-white shadow-sm transition-transform"
                      :class="form.isPublic ? 'translate-x-8' : 'translate-x-1'"
                    ></span>
                  </button>
                </div>
              </div>

              <!-- Summary -->
              <div class="p-6 bg-event-50 rounded-xl border border-event-200">
                <h4 class="font-semibold text-event-900 mb-4 flex items-center gap-2">
                  <i class="fas fa-clipboard-check"></i>
                  Event Summary
                </h4>
                <div class="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span class="text-event-600">Name:</span>
                    <span class="font-medium text-event-900 ml-2">{{ form.name || 'Not set' }}</span>
                  </div>
                  <div>
                    <span class="text-event-600">Date:</span>
                    <span class="font-medium text-event-900 ml-2">{{ form.date ? formatDate(form.date) : 'Not set' }}</span>
                  </div>
                  <div>
                    <span class="text-event-600">Location:</span>
                    <span class="font-medium text-event-900 ml-2">{{ form.location || 'Not set' }}</span>
                  </div>
                  <div>
                    <span class="text-event-600">Organizer:</span>
                    <span class="font-medium text-event-900 ml-2">{{ form.organizerName || 'Not set' }}</span>
                  </div>
                  <div>
                    <span class="text-event-600">Expected:</span>
                    <span class="font-medium text-event-900 ml-2">{{ form.expectedAttendees || 'N/A' }} attendees</span>
                  </div>
                  <div>
                    <span class="text-event-600">Visibility:</span>
                    <span class="font-medium text-event-900 ml-2">{{ form.isPublic ? 'Public' : 'Private' }}</span>
                  </div>
                </div>
              </div>

              <p class="text-slate-500 text-sm">
                <i class="fas fa-info-circle mr-1"></i>
                After creating the event, you can add bars and drop points from the event details page.
              </p>
            </div>

            <!-- Navigation Buttons -->
            <div class="flex items-center justify-between mt-8 pt-6 border-t border-slate-200">
              <button
                v-if="currentStep > 0"
                type="button"
                @click="prevStep"
                class="btn-secondary"
              >
                <i class="fas fa-arrow-left"></i>
                <span>Back</span>
              </button>
              <div v-else></div>

              <div class="flex gap-3">
                <button
                  type="button"
                  @click="goBack"
                  class="btn-ghost"
                >
                  Cancel
                </button>

                <button
                  v-if="currentStep < steps.length - 1"
                  type="button"
                  @click="nextStep"
                  class="btn-primary"
                >
                  <span>Continue</span>
                  <i class="fas fa-arrow-right"></i>
                </button>

                <button
                  v-else
                  type="submit"
                  class="btn-success"
                  :disabled="loading"
                >
                  <span v-if="loading" class="flex items-center gap-2">
                    <i class="fas fa-spinner animate-spin"></i>
                    {{ isEdit ? 'Saving...' : 'Creating...' }}
                  </span>
                  <span v-else>
                    <i class="fas fa-check mr-1.5"></i>
                    {{ isEdit ? 'Save Changes' : 'Create Event' }}
                  </span>
                </button>
              </div>
            </div>
          </form>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useEventStore } from '@/stores/eventStore';
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';
import Sidebar from '@/components/common/Sidebar.vue';
import Navbar from '@/components/common/Navbar.vue';

const route = useRoute();
const router = useRouter();
const eventStore = useEventStore();

// ============ Steps Configuration ============
const steps = [
  { id: 'basic', title: 'Basic Details' },
  { id: 'organizer', title: 'Organizer & Capacity' },
  { id: 'settings', title: 'Settings' },
];

// ============ State ============
const currentStep = ref(0);
const loading = ref(false);
const form = ref({
  name: '',
  date: '',
  location: '',
  description: '',
  organizerName: '',
  organizerEmail: '',
  organizerPhone: '',
  expectedAttendees: undefined as number | undefined,
  maxCapacity: undefined as number | undefined,
  isPublic: true,
});
const errors = ref<Record<string, string>>({});

// ============ Computed ============
const isEdit = computed(() => route.path.includes('/edit'));
const eventId = computed(() => route.params.id ? Number(route.params.id) : null);
const minDate = computed(() => new Date().toISOString().split('T')[0]);

// ============ Methods ============
const getStepClass = (index: number) => {
  if (index < currentStep.value) {
    return 'bg-event-500 text-white';
  } else if (index === currentStep.value) {
    return 'bg-event-100 text-event-600 border-2 border-event-500';
  }
  return 'bg-slate-200 text-slate-500';
};

const goToStep = (index: number) => {
  if (index < currentStep.value) {
    currentStep.value = index;
  }
};

const validateStep = (): boolean => {
  errors.value = {};

  if (currentStep.value === 0) {
    if (!form.value.name.trim()) {
      errors.value.name = 'Event name is required';
      return false;
    }
    if (!form.value.date) {
      errors.value.date = 'Event date is required';
      return false;
    }
    if (new Date(form.value.date) < new Date(minDate.value)) {
      errors.value.date = 'Event date cannot be in the past';
      return false;
    }
  }

  if (currentStep.value === 1) {
    if (form.value.organizerEmail && !isValidEmail(form.value.organizerEmail)) {
      errors.value.organizerEmail = 'Please enter a valid email address';
      return false;
    }
  }

  return true;
};

const isValidEmail = (email: string) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

const nextStep = () => {
  if (validateStep()) {
    currentStep.value++;
  }
};

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--;
  }
};

const goBack = () => {
  router.push('/events');
};

const formatDate = (date: string) => {
  if (!date) return 'N/A';
  return new Date(date).toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

const handleSubmit = async () => {
  if (!validateStep()) return;

  loading.value = true;
  try {
    const payload = {
      name: form.value.name.trim(),
      date: form.value.date,
      location: form.value.location.trim() || undefined,
      description: form.value.description.trim() || undefined,
      organizerName: form.value.organizerName.trim() || undefined,
      organizerEmail: form.value.organizerEmail.trim() || undefined,
      organizerPhone: form.value.organizerPhone.trim() || undefined,
      expectedAttendees: form.value.expectedAttendees || undefined,
      maxCapacity: form.value.maxCapacity || undefined,
      isPublic: form.value.isPublic,
    };

    if (isEdit.value && eventId.value) {
      await eventStore.updateEvent(eventId.value, payload);
      toast.success('Event updated successfully!');
    } else {
      await eventStore.createEvent(payload);
      toast.success('Event created successfully!');
    }
    router.push('/events');
  } catch (err) {
    const errorMessage = err instanceof Error ? err.message : 'Failed to save event';
    toast.error(errorMessage);
  } finally {
    loading.value = false;
  }
};

// ============ Lifecycle ============
onMounted(async () => {
  if (isEdit.value && eventId.value) {
    try {
      const event = await eventStore.fetchEvent(eventId.value);
      if (event) {
        form.value = {
          name: event.name || '',
          date: event.date || '',
          location: event.location || '',
          description: event.description || '',
          organizerName: event.organizerName || '',
          organizerEmail: event.organizerEmail || '',
          organizerPhone: event.organizerPhone || '',
          expectedAttendees: event.expectedAttendees,
          maxCapacity: event.maxCapacity,
          isPublic: event.isPublic ?? true,
        };
      }
    } catch (err) {
      toast.error('Failed to load event');
      router.push('/events');
    }
  }
});
</script>
