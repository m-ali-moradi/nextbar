<template>
  <!-- Transition effect for sliding in and out of the modal -->
  <transition name="fade-slide">
    <!-- Modal container, visible only when 'visible' is true -->
    <div
      v-if="visible"
      class="fixed top-4 right-4 bg-yellow-200 shadow-lg rounded-lg p-4 w-80 z-50"
      @mouseenter="pauseTimer"
      @mouseleave="resumeTimer"
    >
      <!-- Header section with title and close button -->
      <div class="flex justify-between items-center mb-2">
        <h3 class="text-lg font-semibold text-gray-800">
          Low Stock: {{ item.productName }}
        </h3>
        <!-- Close button, triggers cancel on click -->
        <button @click="cancel" class="text-gray-500 hover:text-red-500">
          ✕
        </button>
      </div>
      <!-- Status message showing current quantity and countdown -->
      <p class="text-gray-600 mb-2">
        Current quantity: {{ item.quantity }}. Requesting supply in
        {{ countdown }}s...
      </p>
      <!-- Quantity input section -->
      <div class="flex items-center mb-2">
        <label for="quantity" class="mr-2 text-gray-700">Quantity:</label>
        <!-- Input for user to set requested quantity, with validation -->
        <input
          id="quantity"
          type="number"
          v-model.number="quantity"
          min="1"
          max="50"
          class="border rounded px-2 py-1 w-20"
        />
      </div>
      <!-- Action button section -->
      <div class="flex justify-end">
        <!-- Confirm button to submit the supply request -->
        <button
          @click="confirm"
          class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          OK
        </button>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

const props = defineProps<{
  item: { productName: string; quantity: number };
  duration?: number;
}>();

const emit = defineEmits<{
  cancel: [];
  confirm: [payload: { productName: string; quantity: number }];
}>();

const effectiveDuration = props.duration ?? 10;

const visible = ref(true);
const countdown = ref(effectiveDuration);
const quantity = ref(20);
const paused = ref(false);
let timer: ReturnType<typeof setInterval> | null = null;

function startTimer() {
  timer = setInterval(() => {
    if (!paused.value) {
      countdown.value--;
      if (countdown.value <= 0) {
        confirm();
      }
    }
  }, 1000);
}

function pauseTimer() {
  paused.value = true;
}

function resumeTimer() {
  paused.value = false;
}

function cancel() {
  if (timer) clearInterval(timer);
  visible.value = false;
  emit('cancel');
}

function confirm() {
  if (timer) clearInterval(timer);
  visible.value = false;
  emit('confirm', {
    productName: props.item.productName,
    quantity: quantity.value,
  });
}

onMounted(() => {
  startTimer();
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});
</script>

<style scoped>
/* Define transition effects for the modal slide animation */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.5s ease; /* Smooth transition over 0.5 seconds */
}

/* Define starting and ending states for the slide animation */
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0; /* Fully transparent at start/end */
  transform: translateX(100%); /* Slide in from the right */
}
</style>
