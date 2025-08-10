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

    <!-- 视频区域 -->
    <div class="video-grid">
      <div class="video-item">
        <div class="video-placeholder">
          <span class="video-avatar">👤</span>
          <p>参与者 1</p>
        </div>
      </div>

      <div class="video-item">
        <div class="video-placeholder">
          <span class="video-avatar">{{ userStore.currentUser?.avatar }}</span>
          <p>{{ userStore.currentUser?.name }} (您)</p>
        </div>
      </div>
    </div>

    <!-- 控制栏 -->
    <div class="control-bar">
      <el-button
          :type="roomStore.isMicOn ? 'default' : 'danger'"
          :icon="roomStore.isMicOn ? Microphone : Mute"
          circle
          size="large"
          @click="roomStore.toggleMic()"
      />

      <el-button
          :type="roomStore.isVideoOn ? 'default' : 'danger'"
          :icon="roomStore.isVideoOn ? VideoCamera : VideoCameraFilled"
          circle
          size="large"
          @click="roomStore.toggleVideo()"
      />

      <el-button
          type="danger"
          :icon="PhoneFilled"
          circle
          size="large"
          @click="leaveRoom"
      />
    </div>

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
import { ref } from 'vue'
import {
  ArrowLeft,
  Microphone,
  Mute,
  VideoCamera,
  VideoCameraFilled,
  PhoneFilled,
  Promotion
} from '@element-plus/icons-vue'
import { useRoomStore } from '@/stores/room'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const roomStore = useRoomStore()
const userStore = useUserStore()

const chatMessage = ref('')

const leaveRoom = () => {
  roomStore.leaveRoom()
  ElMessage.info('已离开房间')
}

const sendChatMessage = () => {
  if (chatMessage.value.trim()) {
    // 这里可以实现发送消息的逻辑
    ElMessage.success('消息已发送')
    chatMessage.value = ''
  }
}
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