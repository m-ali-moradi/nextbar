<template>
  <div class="manage-item">
    <header class="header">
      <div class="logo">Drop Points Service</div>
      <nav class="nav">
        <a href="/src/components/Droppoint" class="active">Droppoint</a>

        <a href="#" class="logout">Logout</a>
      </nav>
    </header>

    <main class="content">
      <h1>Manage Drop Points</h1>
      <button class="new-item" @click="openModal((item = null))">+ New Drop Point</button>

      <!-- Modal Form -->
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal">
          <span class="close" @click="closeModal">&times;</span>
          <h2>{{ isEditing ? 'Edit Drop Point' : 'New Drop Point' }}</h2>
          <form @submit.prevent="isEditing ? updateDropPoint() : addDropPoint()">
            <label for="location">Location:</label>
            <input type="text" id="location" v-model="newDropPoint.location" required />
            <label for="capacity">Capacity:</label>
            <input type="text" id="capacity" v-model="newDropPoint.capacity" required />
            <label for="current_empties">Current Empties:</label>
            <input type="text" id="current_empties" v-model="newDropPoint.current_empties" required />
            <label for="status">Status:</label>
            <input type="text" id="status" v-model="newDropPoint.status" required />

            <button type="submit" class="submit-btn">
              {{ isEditing ? 'Update Drop point' : 'Save Drop point' }}
            </button>
            <button type="button" @click="closeModal()" class="cancel-btn">Cancel</button>
          </form>
        </div>
      </div>

      <table class="item-table">
        <thead>
          <tr>
            <th>Drop Point ID</th>
            <th>Location</th>
            <th>Capacity</th>
            <th>Current Empties</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in droppoints" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.location }}</td>
            <td>{{ item.capacity }}</td>
            <td>{{ item.current_empties }}</td>
            <td>{{ item.status }}</td>
            <td>
              <button @click="openModal(item)">Edit</button>
              <button @click="deleteDropPoint(item.id)" class="delete-btn">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
    </main>
  </div>
</template>

<script>
import { useDroppointStore } from '@/stores/droppoint.js'

export default {
  name: 'ManageDroppoint',
  data() {
    return {
      newDropPoint: {
        id: '',
        location: '',
        capacity: '',
        current_empties: '',
        status: '',
      },
      showModal: false,
      isEditing: false,
      editIndex: null,
    }
  },
  computed: {
    droppoints() {
      return this.dropPointStore.droppoints
    },
  },
  methods: {
    openModal(item) {
      this.showModal = true
      if (item) {
        this.isEditing = true
        this.editIndex = this.droppoints.findIndex((u) => u.id === item.id)
        this.newDropPoint= { ...item }
      } else {
        this.isEditing = false
        this.editIndex = null
        this.newDropPoint = {

          location: '',
          capacity: 100,
          current_empties: 0,
          status: "EMPTY"
        }
      }
    },
    closeModal() {
      this.showModal = false
      this.newDropPoint = {
        id: '',
        location: '',
        capacity: '',
        current_empties: '',
        status: '',
      }
      this.isEditing = false
      this.editIndex = null
    },
    addDropPoint() {
      this.dropPointStore.addDropPoint({ ...this.newDropPoint})
      this.closeModal()
    },
    updateDropPoint() {
      if (this.editIndex !== null) {
        this.dropPointStore.updateDropPoint(this.editIndex + 1, { ...this.newDropPoint})
      }
      this.closeModal()
    },
    deleteDropPoint(id) {
      if (confirm('Are you sure you want to delete this inventory?')) {
        this.dropPointStore.deleteDropPoint(id)
      }
    },
  },
  setup() {
    const dropPointStore = useDroppointStore()
    // fetch inventories
    dropPointStore.fetchDropPoints()
    return { dropPointStore }
  },
  onMounted() {
    return this.dropPointStore.fetchDropPoints()
  },
}
</script>

<style scoped>
.manage-item {
  font-family: Arial, sans-serif;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ddd;
}
</style>
