<template>
  <div class="main-container">
    <VideoRoom v-if="roomStore.selectedRoom" />
    <template v-else>
      <SideBar />
      <ChatArea />
    </template>
  </div>
</template>

<script setup lang="ts">
import SideBar from '@/components/Main/SideBar.vue'
import ChatArea from '@/components/Main/ChatArea.vue'
import VideoRoom from '@/components/Main/VideoRoom.vue'
import { useRoomStore } from '@/stores/room'

const roomStore = useRoomStore()
</script>

<style scoped>
.main-container {
  display: flex;
  height: 100vh;
  background: #f5f7fa;
}
</style>