<template>
  <ErrorBoundary>
    <router-view v-slot="{ Component }">
      <KeepAlive>
        <component :is="Component" />
      </KeepAlive>
    </router-view>
  </ErrorBoundary>
  <Snackbar />
</template>

<script setup lang="ts">
import Snackbar from './components/Snackbar.vue'
import ErrorBoundary from './components/ErrorBoundary.vue'
</script>
