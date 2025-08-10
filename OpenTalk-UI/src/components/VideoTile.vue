<template>
  <div class="video-card" style="position:relative">
    <span class="video-label">{{ label }}</span>
    <video ref="el" class="peer" playsinline autoplay :muted="muted" style="width:100%;height:180px;object-fit:cover;background:#000"></video>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';

const props = defineProps({
  stream: { type: [Object, null], default: null },
  label: { type: String, default: '' },
  muted: { type: Boolean, default: false }
});

const el = ref(null);

function apply() {
  if (el.value) {
    el.value.srcObject = props.stream || null;
    el.value.play?.().catch(()=>{});
  }
}

onMounted(apply);
watch(() => props.stream, apply);
</script>
