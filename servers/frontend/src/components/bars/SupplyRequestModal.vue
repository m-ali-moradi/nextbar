<script setup>
import { ref, watch } from 'vue';
import { useBarStore } from '../../stores/barStore';

const props = defineProps({
  isOpen: Boolean,
  item: Object,
  barId: String
});

const emit = defineEmits(['close', 'supply-requested']);

const quantity = ref(10);
const barStore = useBarStore();

function validateQuantity() {
  if (quantity.value < 1) {
    quantity.value = 0;
  } else if (quantity.value > 50) {
    alert("You can only request up to 50 items at a time.");
    quantity.value = 50;
  }
}

async function requestSupply() {
  if (quantity.value < 1 || quantity.value > 50) {
    alert("Invalid quantity. Please enter a value between 1 and 50.");
    return;
  }
  try {
    await barStore.createSupplyRequest(props.barId, [
      { productId: props.item.productId, quantity: quantity.value }
    ]);
    emit('close');
    emit('supply-requested', { productId: props.item.productId, quantity: quantity.value });
    quantity.value = 10;
  } catch (error) {
    console.error("Error during supply request:", error);
    alert("Failed to request supply. Please try again later.");
  }
}
</script>
