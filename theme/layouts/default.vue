<template>
  <div class="bg-main slidev-layout">
    <factor10-logo></factor10-logo>
    <div class="my-auto">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { defineComponent } from 'vue'
import Factor10Logo from '../components/Factor10Logo.vue'

const components = defineComponent({
  Factor10Logo,
})
</script>