<template>
  <div class="dialog-hero">
    <div class="dialog-title">{{ title }}</div>
    <div v-if="description" class="dialog-subtitle">{{ description }}</div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  description?: string
  title: string
}>()
</script>
