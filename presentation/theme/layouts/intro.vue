<template>
  <default class="intro" :style="style">
    <slot />
  </default>
</template>

<script setup lang="ts">
import { defineComponent } from 'vue'
import Default from '../layouts/default.vue'
import { computed } from 'vue'
import { handleBackground } from '../layoutHelper'

const props = defineProps({
  background: {
  },
  dark: {
  },
  dim: {
    default: true,
  },
})

const style = computed(() => handleBackground(props.background, props.dim, props.dark))

const components = defineComponent({ Default })
</script>
