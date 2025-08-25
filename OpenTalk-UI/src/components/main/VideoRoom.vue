<template>
  <div class="video-room">
    <!-- 房间头部 -->
    <div class="room-header">
      <div class="room-info">
        <el-button
            :icon="ArrowLeft"
            @click="leaveRoom"
            text
        >
          返回
        </el-button>
        <h2>{{ roomStore.selectedRoom?.name }}</h2>
        <el-tag>{{ roomStore.selectedRoom?.participants }} 位参与者</el-tag>
      </div>
    </div>

    <!-- 视频网格 -->
    <VideoGrid
        :local-stream="roomStore.localStream"
        :participants="roomStore.participants"
        :screen-share-stream="roomStore.screenShareStream"
        :screen-share-user="screenShareUser"
        :current-user="userStore.currentUser"
        :is-mic-on="roomStore.isMicOn"
        :is-camera-on="roomStore.isVideoOn"
        @toggle-camera="roomStore.toggleVideo"
    />

    <!-- 控制栏 -->
    <VideoControls
        :is-mic-on="roomStore.isMicOn"
        :is-camera-on="roomStore.isVideoOn"
        :is-screen-sharing="roomStore.isScreenSharing"
        :is-chat-visible="isChatVisible"
        :call-duration="callDuration"
        @toggle-mic="roomStore.toggleMic"
        @toggle-camera="roomStore.toggleVideo"
        @toggle-screen-share="roomStore.toggleScreenShare"
        @toggle-chat="toggleChat"
        @end-call="leaveRoom"
        @open-device-settings="openDeviceSettings"
        @open-audio-settings="openAudioSettings"
        @open-general-settings="openGeneralSettings"
    />

    <!-- 侧边聊天栏 -->
    <div class="room-chat">
      <div class="chat-header">
        <h3>房间聊天</h3>
      </div>

      <div class="chat-messages">
        <div class="system-message">
          <span class="message-tag">系统</span>
          欢迎加入房间
        </div>
      </div>

      <div class="chat-input">
        <el-input
            v-model="chatMessage"
            placeholder="输入消息..."
            @keyup.enter="sendChatMessage"
        />
        <el-button
            type="primary"
            :icon="Promotion"
            @click="sendChatMessage"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useRoomStore } from '@/stores/room'
import { useUserStore } from '@/stores/user'
import { ArrowLeft, ChatLineRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import VideoGrid from '@/components/video/VideoGrid.vue'
import VideoControls from '@/components/video/VideoControls.vue'

const router = useRouter()
const roomStore = useRoomStore()
const userStore = useUserStore()

const isChatVisible = ref(false)
const callDuration = ref(0)
const callTimer = ref<number>()
const chatMessage = ref('')

const currentRoom = computed(() => roomStore.currentRoom)

const screenShareUser = computed(() => {
  // 找到正在共享屏幕的用户
  return roomStore.participants.find(p => p.isScreenSharing) || 
         (roomStore.isScreenSharing ? userStore.currentUser : null)
})

const leaveRoom = async () => {
  try {
    await roomStore.leaveRoom()
    ElMessage.success('已离开房间')
    router.push('/main')
  } catch (error) {
    console.error('离开房间失败:', error)
    ElMessage.error('离开房间失败')
  }
}

const toggleChat = () => {
  isChatVisible.value = !isChatVisible.value
}

const goBack = () => {
  router.push('/main')
}

const openDeviceSettings = () => {
  // TODO: 打开设备设置
  ElMessage.info('设备设置功能开发中...')
}

const openAudioSettings = () => {
  // TODO: 打开音频设置
  ElMessage.info('音频设置功能开发中...')
}

const openGeneralSettings = () => {
  // TODO: 打开通用设置
  ElMessage.info('通用设置功能开发中...')
}

const sendChatMessage = () => {
  if (chatMessage.value.trim()) {
    // 这里可以实现发送消息的逻辑
    ElMessage.success('消息已发送')
    chatMessage.value = ''
  }
}

// 开始计时
const startCallTimer = () => {
  callTimer.value = setInterval(() => {
    callDuration.value++
  }, 1000)
}

// 停止计时
const stopCallTimer = () => {
  if (callTimer.value) {
    clearInterval(callTimer.value)
    callTimer.value = undefined
  }
}

onMounted(async () => {
  try {
    await roomStore.initializeMedia()
    startCallTimer()
  } catch (error) {
    console.error('初始化媒体失败:', error)
    ElMessage.error('无法访问摄像头或麦克风')
  }
})

onUnmounted(() => {
  stopCallTimer()
  // 清理资源
  roomStore.cleanup()
})
</script>

<style scoped>
.video-room {
  position: fixed;
  inset: 0;
  background: #1a1a1a;
  display: grid;
  grid-template-columns: 1fr 320px;
  grid-template-rows: auto 1fr auto;
  grid-template-areas:
    "header header"
    "video chat"
    "controls chat";
}

.room-header {
  grid-area: header;
  padding: 20px;
  background: #2a2a2a;
  border-bottom: 1px solid #3a3a3a;
}

.room-info {
  display: flex;
  align-items: center;
  gap: 20px;
  color: white;
}

.room-info h2 {
  font-size: 20px;
  font-weight: 500;
}

.video-grid {
  grid-area: video;
  padding: 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  align-content: center;
}

.video-item {
  aspect-ratio: 16/9;
  background: #2a2a2a;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-placeholder {
  text-align: center;
  color: white;
}

.video-avatar {
  font-size: 64px;
  display: block;
  margin-bottom: 10px;
}

.control-bar {
  grid-area: controls;
  padding: 20px;
  display: flex;
  justify-content: center;
  gap: 20px;
  background: #2a2a2a;
  border-top: 1px solid #3a3a3a;
}

.room-chat {
  grid-area: chat;
  background: #2a2a2a;
  border-left: 1px solid #3a3a3a;
  display: flex;
  flex-direction: column;
}

.room-chat .chat-header {
  padding: 20px;
  border-bottom: 1px solid #3a3a3a;
}

.room-chat .chat-header h3 {
  color: white;
  font-size: 16px;
}

.room-chat .chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.system-message {
  color: #909399;
  font-size: 14px;
}

.message-tag {
  color: #409eff;
  margin-right: 8px;
}

.room-chat .chat-input {
  padding: 20px;
  border-top: 1px solid #3a3a3a;
  display: flex;
  gap: 10px;
}

.room-chat .chat-input .el-input {
  flex: 1;
}
</style>