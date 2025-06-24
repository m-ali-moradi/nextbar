<template>
  <div id="app">
    <!-- optional nav -->
    <nav>
      <router-link to="/events">All Events</router-link> |
      <router-link to="/event/step1">Create Event</router-link>
    </nav>

    <!-- this is where your routed components appear -->
    <router-view />
  </div>
</template>

<script setup>
// no imports needed here unless you want global mixins, composables, etc.
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  margin: 20px;
  color: #2c3e50;
}

nav {
  margin-bottom: 20px;
}

nav a {
  text-decoration: none;
  margin: 0 10px;
  color: #42b983;
}

nav a.router-link-exact-active {
  font-weight: bold;
}
</style>
