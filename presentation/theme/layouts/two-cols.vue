<script setup lang="ts">
import Default from '../layouts/default.vue'
import {defineComponent} from "vue";

const components = defineComponent({ Default })

const props = defineProps({
  class: {
    type: String,
  },
  layoutClass: {
    type: String,
  },
})
</script>

<template>
  <default class="slidev-layout">
    <slot />
    <div class="two-columns w-full h-full grid grid-cols-2" :class="props.layoutClass">
      <div class="col-left pr-1" :class="props.class">
        <slot name="left"/>
      </div>
      <div class="col-right pl-1" :class="props.class">
        <slot name="right" />
      </div>
    </div>
  </default>
</template>