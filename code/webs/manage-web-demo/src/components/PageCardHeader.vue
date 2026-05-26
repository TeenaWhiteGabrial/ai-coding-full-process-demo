<template>
  <div class="page-card-header">
    <div>
      <span class="page-kicker">{{ kicker }}</span>
      <h2>{{ title }}</h2>
      <p v-if="description" class="page-description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="page-card-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  description?: string
  kicker: string
  title: string
}>()
</script>
