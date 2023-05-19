<script setup lang="ts">
import Default from '../layouts/default.vue'
import { defineComponent } from 'vue'
import { computed } from 'vue'
import { handleBackground } from '../layoutHelper'

const props = defineProps({
  background: {
    // random image from a curated Unsplash collection by Anthony
    default: 'https://source.unsplash.com/collection/94734566/1920x1080',
  },
  dim: {
    default: true,
  },
})

const style = computed(() => handleBackground(props.background, props.dim))
const components = defineComponent({ Default })
</script>

<template>
  <default class="cover h-full grid place-content-center" :style="style">
    <slot />
  </default>
</template>

