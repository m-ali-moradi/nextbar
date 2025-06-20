<template>
  <div class="manage-item">
    <header class="header">
      <div class="logo">Drop Points Service</div>
      <nav class="nav">
        <a href="/" class="active">Droppoint</a>

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
          <form @submit.prevent="isEditing ? updateDropPoint() : createDropPoint()">
            <label for="location">Location:</label>
            <input type="text" id="location" v-model="newDropPoint.location" required />
            <label for="capacity">Capacity:</label>
            <input type="number" id="capacity" v-model="newDropPoint.capacity" required />
            <label v-if="newDropPoint.status === 'FULL' || 'EMPTY'" for="current_empties">Current Empties:</label>
            <input v-if="newDropPoint.status === 'FULL' || 'EMPTY'" type="number" id="current_empties" v-model="newDropPoint.current_empties" required />
            <label v-if="newDropPoint.status === 'FULL' || 'EMPTY'" for="status">Status:</label>
            <input v-if="newDropPoint.status === 'FULL' || 'EMPTY'" type="text" id="status" v-model="newDropPoint.status" required />

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
              <button @click="openModal(item)" class="edit-btn">Edit</button>
              <button @click="deleteDropPoint(item.id)" class="delete-btn">Delete</button>
              <button v-if="item.status === 'EMPTY'" @click="addEmpty(item.id)" class="add-btn">Add Empties</button>
              <button v-if="item.status === 'FULL'" @click="notifyWarehouse(item.id)" class="notify-btn">Notify Warehouse</button>
              <button v-if="item.status === 'FULL_AND_NOTIFIED_TO_WAREHOUSE'" @click="removeEmpties(item.id)" class="remove-btn">Transfer to Warehouse</button>
            </td>
          </tr>
        </tbody>
      </table>
    </main>
  </div>
</template>

<script>
import { toast } from 'vue3-toastify';
import "vue3-toastify/dist/index.css";
import { useDroppointStore } from '@/stores/droppoint.js'

export default {
  name: 'ManageDroppoint',
  data() {
    return {
      newDropPoint: {
        id: '',
        location: '',
        capacity: 100,
        current_empties: 0,
        status: 'EMPTY',
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
        capacity: 100,
        current_empties: 0,
        status: 'EMPTY',
      }
      this.isEditing = false
      this.editIndex = null
    },
    createDropPoint() {
      this.dropPointStore.createDropPoint({ ...this.newDropPoint})
      toast(`Drop point ${this.newDropPoint.location} was created`, {
        autoClose: 3000,
        theme: "auto",
        hideProgressBar: false,
        type: toast.TYPE.SUCCESS
      });
      this.closeModal()
    },
    updateDropPoint() {
      if (this.editIndex !== null) {
        this.dropPointStore.updateDropPoint(this.editIndex + 1, { ...this.newDropPoint})
        toast(`Drop Point of id ${this.editIndex + 1} was updated`, {
          autoClose: 3000,
          theme: "auto",
          hideProgressBar: false,
          type: toast.TYPE.SUCCESS
        });
      }
      this.closeModal()
    },
    deleteDropPoint(id) {
      if (confirm('Are you sure you want to delete this drop point?')) {
        this.dropPointStore.deleteDropPoint(id)
        toast(`Drop Point ${id} was added`, {
          autoClose: 3000,
          theme: "auto",
          hideProgressBar: false,
          type: toast.TYPE.WARNING
        });

      }
    },
    addEmpty(id) {
      this.dropPointStore.addEmpties(id)
      toast(`An empty was added to drop point id ${id}`, {
        autoClose: 3000,
        theme: "auto",
        hideProgressBar: false,
        type: toast.TYPE.SUCCESS

      });
    },
    notifyWarehouse(id) {
      if (confirm('Are you sure you want to notify warehouse to remove empties?')) {
        this.dropPointStore.notifyWarehouse(id)
        toast(`Warehouse notified`, {
          autoClose: 3000,
          theme: "auto",
          hideProgressBar: false,
          type: toast.TYPE.INFO
        });
      }
    },

    removeEmpties(id) {
      if (confirm('Are you sure you want to transfer empties to warehouse?')) {
        this.dropPointStore.removeEmpties(id)
        toast(`Empties from Drop point ID ${id} were transferred to the Warehouse`, {
          autoClose: 3000,
          theme: "auto",
          hideProgressBar: false,
          type: toast.TYPE.SUCCESS
        });
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
