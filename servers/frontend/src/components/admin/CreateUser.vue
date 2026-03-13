<template>
  <div class="p-6">
    <!-- Header -->
    <div class="flex items-center gap-3 mb-6">
      <div class="w-10 h-10 rounded-xl bg-primary-100 flex items-center justify-center">
        <i class="fas fa-user-plus text-primary-600"></i>
      </div>
      <div>
        <h2 class="text-lg font-semibold text-slate-900">Create New User</h2>
        <p class="text-sm text-slate-500">Add a new user to the system</p>
      </div>
    </div>

    <!-- Step Indicator -->
    <div class="flex items-center gap-2 mb-6">
      <button
        v-for="(s, idx) in steps"
        :key="idx"
        @click="goToStep(idx)"
        class="flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all"
        :class="currentStep === idx
          ? 'bg-primary-100 text-primary-700'
          : currentStep > idx
            ? 'bg-emerald-100 text-emerald-700'
            : 'bg-slate-100 text-slate-400'"
      >
        <span
          class="w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold"
          :class="currentStep === idx
            ? 'bg-primary-600 text-white'
            : currentStep > idx
              ? 'bg-emerald-500 text-white'
              : 'bg-slate-300 text-white'"
        >
          <i v-if="currentStep > idx" class="fas fa-check"></i>
          <span v-else>{{ idx + 1 }}</span>
        </span>
        {{ s.label }}
      </button>
    </div>

    <!-- Error -->
    <Transition name="fade">
      <div
        v-if="error"
        class="mb-6 px-4 py-3 rounded-xl bg-red-50 border border-red-100 flex items-center gap-3 text-red-700"
      >
        <i class="fas fa-exclamation-circle"></i>
        <span class="text-sm">{{ error }}</span>
      </div>
    </Transition>

    <form @submit.prevent="handleSubmit">
      <!-- Step 1: Credentials -->
      <div v-show="currentStep === 0" class="space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label for="firstName" class="label">First Name</label>
            <input
              v-model="form.firstName"
              id="firstName"
              type="text"
              class="input"
              placeholder="Enter first name"
              required
            />
          </div>
          <div>
            <label for="lastName" class="label">Last Name</label>
            <input
              v-model="form.lastName"
              id="lastName"
              type="text"
              class="input"
              placeholder="Enter last name"
              required
            />
          </div>
          <div>
            <label for="username" class="label">Username</label>
            <input
              v-model="form.username"
              id="username"
              type="text"
              class="input"
              placeholder="Enter username"
              required
            />
          </div>
          <div>
            <label for="email" class="label">Email</label>
            <input
              v-model="form.email"
              id="email"
              type="email"
              class="input"
              placeholder="user@example.com"
              required
            />
          </div>
        </div>

        <div>
          <label for="password" class="label">Password</label>
          <div class="relative">
            <input
              v-model="form.password"
              id="password"
              :type="showPassword ? 'text' : 'password'"
              class="input pr-11"
              placeholder="Create a password"
              required
              minlength="6"
            />
            <button
              type="button"
              @click="showPassword = !showPassword"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
              tabindex="-1"
            >
              <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
          <!-- Strength -->
          <div class="mt-2" v-if="form.password">
            <div class="flex gap-1 mb-1">
              <div
                v-for="i in 4"
                :key="i"
                class="h-1.5 flex-1 rounded-full transition-all duration-300"
                :class="i <= passwordStrength.level ? passwordStrength.color : 'bg-slate-200'"
              ></div>
            </div>
            <p class="text-xs font-medium" :class="passwordStrength.textColor">
              {{ passwordStrength.label }}
            </p>
          </div>
        </div>

        <div class="flex justify-end pt-2">
          <button
            type="button"
            class="btn-primary"
            @click="nextStep"
            :disabled="!step1Valid"
          >
            Next
            <i class="fas fa-arrow-right ml-1"></i>
          </button>
        </div>
      </div>

      <!-- Step 2: Role & Service -->
      <div v-show="currentStep === 1" class="space-y-5">
        <div>
          <label class="label">
            <i class="fas fa-user-tag mr-1 text-slate-400"></i>
            Role
          </label>
          <div class="grid grid-cols-3 gap-3">
            <button
              v-for="role in availableRoles"
              :key="role.value"
              type="button"
              @click="form.role = role.value"
              class="p-3 rounded-xl border-2 transition-all text-center"
              :class="form.role === role.value
                ? 'border-primary-500 bg-primary-50 text-primary-700'
                : 'border-slate-200 hover:border-slate-300 text-slate-600'"
            >
              <i :class="role.icon" class="text-lg mb-1"></i>
              <p class="text-sm font-semibold">{{ role.label }}</p>
            </button>
          </div>
        </div>

        <div v-if="form.role && form.role !== 'ADMIN'">
          <label class="label">
            <i class="fas fa-cogs mr-1 text-slate-400"></i>
            Service
          </label>
          <div class="grid grid-cols-2 gap-3">
            <button
              v-for="service in availableServices"
              :key="service.value"
              type="button"
              @click="form.service = service.value"
              class="p-3 rounded-xl border-2 transition-all flex items-center gap-3"
              :class="form.service === service.value
                ? 'border-primary-500 bg-primary-50 text-primary-700'
                : 'border-slate-200 hover:border-slate-300 text-slate-600'"
            >
              <i :class="service.icon" class="text-lg"></i>
              <span class="text-sm font-semibold">{{ service.label }}</span>
            </button>
          </div>
        </div>

        <div v-if="form.role && form.role !== 'ADMIN' && form.service === 'BAR'">
          <label for="assignedTo" class="label">Assign To Bar (Optional)</label>
          <select v-model="form.assignedTo" id="assignedTo" class="input">
            <option value="">No resource</option>
            <option v-for="resource in availableResources" :key="resource.value" :value="resource.value">
              {{ resource.label }}
            </option>
          </select>
          <p class="text-xs text-slate-500 mt-1.5">Leave empty to allow access to all bars</p>
        </div>

        <div class="flex justify-between pt-2">
          <button type="button" class="btn-secondary" @click="currentStep = 0">
            <i class="fas fa-arrow-left mr-1"></i>
            Back
          </button>
          <button
            type="button"
            class="btn-primary"
            @click="nextStep"
            :disabled="!step2Valid"
          >
            Review
            <i class="fas fa-arrow-right ml-1"></i>
          </button>
        </div>
      </div>

      <!-- Step 3: Review -->
      <div v-show="currentStep === 2" class="space-y-4">
        <div class="p-5 rounded-xl bg-slate-50 border border-slate-200 space-y-3">
          <h4 class="text-sm font-bold text-slate-700 uppercase tracking-wide">Review Details</h4>
          <div class="grid grid-cols-2 gap-3 text-sm">
            <div>
              <p class="text-slate-500 text-xs mb-0.5">Username</p>
              <p class="font-semibold text-slate-900">{{ form.username }}</p>
            </div>
            <div>
              <p class="text-slate-500 text-xs mb-0.5">Email</p>
              <p class="font-semibold text-slate-900">{{ form.email }}</p>
            </div>
            <div>
              <p class="text-slate-500 text-xs mb-0.5">First Name</p>
              <p class="font-semibold text-slate-900">{{ form.firstName || '—' }}</p>
            </div>
            <div>
              <p class="text-slate-500 text-xs mb-0.5">Last Name</p>
              <p class="font-semibold text-slate-900">{{ form.lastName || '—' }}</p>
            </div>
            <div>
              <p class="text-slate-500 text-xs mb-0.5">Role</p>
              <p class="font-semibold text-slate-900">{{ form.role || '—' }}</p>
            </div>
            <div>
              <p class="text-slate-500 text-xs mb-0.5">Service</p>
              <p class="font-semibold text-slate-900">{{ form.role === 'ADMIN' ? 'ALL' : (form.service || '—') }}</p>
            </div>
            <div v-if="form.assignedTo" class="col-span-2">
              <p class="text-slate-500 text-xs mb-0.5">Assigned Resource</p>
              <p class="font-semibold text-slate-900">{{ getResourceLabel(form.assignedTo) }}</p>
            </div>
          </div>
        </div>

        <div class="flex gap-3 pt-2">
          <button type="button" class="btn-secondary flex-1" @click="currentStep = 1">
            <i class="fas fa-arrow-left mr-1"></i>
            Back
          </button>
          <button type="submit" class="btn-primary flex-1" :disabled="loading">
            <template v-if="loading">
              <i class="fas fa-spinner fa-spin"></i>
              <span>Creating...</span>
            </template>
            <template v-else>
              <i class="fas fa-user-plus"></i>
              <span>Create User</span>
            </template>
          </button>
        </div>
      </div>
    </form>

    <!-- Cancel -->
    <div class="mt-4 pt-4 border-t border-slate-100 text-center">
      <button type="button" class="text-sm text-slate-500 hover:text-slate-700 font-medium" @click="emit('cancel')">
        Cancel
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { authApi, barApi } from '@/api';
import { usePasswordStrength } from '@/composables/usePasswordStrength';
import { availableRoles, availableServices } from '@/composables/useRoleStyling';

const emit = defineEmits(['created', 'cancel']);

const loading = ref(false);
const error = ref('');
const showPassword = ref(false);
const currentStep = ref(0);

const steps = [
  { label: 'Credentials' },
  { label: 'Role & Service' },
  { label: 'Review' },
];

const form = reactive({
  username: '',
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  role: '',
  service: '',
  assignedTo: '',
});

const bars = ref([]);

const _password = computed(() => form.password);
const { strength: passwordStrength } = usePasswordStrength(_password);

const availableResources = computed(() =>
  (bars.value || []).map((bar) => ({ value: bar.id, label: bar.name }))
);

const step1Valid = computed(() =>
  form.username.trim() && form.firstName.trim() && form.lastName.trim() && form.email.trim() && form.password.length >= 6
);

const step2Valid = computed(() => {
  if (!form.role) return false;
  if (form.role === 'ADMIN') return true;
  return !!form.service;
});

function goToStep(idx) {
  if (idx < currentStep.value) currentStep.value = idx;
}

function nextStep() {
  if (currentStep.value < steps.length - 1) currentStep.value++;
}

function getResourceLabel(id) {
  const r = availableResources.value.find(r => r.value === id);
  return r ? r.label : id;
}

async function loadResources() {
  try {
    const [barsResponse] = await Promise.all([barApi.getBars()]);
    bars.value = barsResponse.data || [];
  } catch (e) {
    console.error('Failed to load resources:', e);
  }
}

async function handleSubmit() {
  loading.value = true;
  error.value = '';

  try {
    const resourceId =
      form.role !== 'ADMIN' && form.service === 'BAR'
        ? (form.assignedTo || null)
        : null;

    const createResponse = await authApi.createUser({
      username: form.username,
      firstName: form.firstName,
      lastName: form.lastName,
      email: form.email,
      password: form.password,
      roleName: form.role,
      serviceCode: form.service,
      resourceId,
    });

    const userId = createResponse?.data?.id;
    if (userId) {
      const assignments = form.role === 'ADMIN'
        ? [{ roleName: 'ADMIN', serviceCode: null, resourceId: null }]
        : [{ roleName: form.role, serviceCode: form.service, resourceId }];

      await authApi.setUserAssignments(userId, assignments);
    }

    emit('created');
  } catch (e) {
    error.value = e?.message ?? 'Failed to create user';
    currentStep.value = 0;
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadResources();
});
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
