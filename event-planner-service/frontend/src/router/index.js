import { createRouter, createWebHistory } from 'vue-router'
import EventList from '@/components/EventList.vue'
import EventForm from '@/components/EventForm.vue'
import EventDetails from '@/components/EventDetails.vue'

const routes = [
  // List all events
  { path: '/events', component: EventList },

  { path: '/events/new', component: EventForm },

  // Edit an existing event – same form, with props to load data
  {
    path: '/events/:id/edit',
    component: EventForm,
    props: route => ({ id: route.params.id, edit: true })
  },

  // View event details
  { path: '/events/:id', component: EventDetails, props: true },

  // Fallback
  { path: '/:pathMatch(.*)*', redirect: '/events' }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
