<template>
  <div class="mono-avatar" :style="{ background: bg }">
    {{ letter }}
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  seed: { type: String, default: '?' },
  color: { type: String, default: '#0a84ff' }
});

const letter = computed(() => (props.seed || '?').slice(0,1).toUpperCase());
const bg = computed(() => props.color || '#0a84ff');
</script>

<style scoped>
.mono-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fff;
  font-weight: 700;
  font-size: 12px;
  border: 1px solid rgba(0,0,0,0.08);
  box-shadow: 0 2px 10px rgba(0,0,0,0.06) inset;
}
</style>