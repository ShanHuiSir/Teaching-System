<template>
  <svg
    :viewBox="def.viewBox || '0 0 24 24'"
    :fill="def.fill ?? 'none'"
    :stroke="def.stroke ?? 'currentColor'"
    :stroke-width="def.strokeWidth ?? '1.5'"
    stroke-linecap="round"
    stroke-linejoin="round"
  >
    <component
      v-for="(el, i) in elements"
      :key="i"
      :is="el.tag"
      v-bind="omitTag(el)"
    />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ICONS, type IconDef } from '../utils/icons'

const props = defineProps<{
  name: string
}>()

const def = computed<IconDef>(() => ICONS[props.name] ?? { elements: [] })

const elements = computed(() => def.value.elements)

function omitTag(el: { tag: string; [key: string]: string }) {
  const { tag: _, ...attrs } = el
  return attrs
}
</script>
