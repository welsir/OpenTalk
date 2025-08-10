<template>
  <div class="chat-area">
    <template v-if="chatStore.selectedChat">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="chat-user-info">
          <span class="chat-avatar">{{ chatStore.selectedChat.avatar }}</span>
          <div class="chat-user-details">
            <div class="chat-user-name">{{ chatStore.selectedChat.name }}</div>
            <div class="chat-user-status">
              {{ chatStore.selectedChat.status === 'online' ? '在线' : '离线' }}
            </div>
          </div>
        </div>

        <div class="chat-actions">
          <el-button :icon="Phone" circle />
          <el-button :icon="VideoCamera" circle />
        </div>
      </div>

      <!-- 消息区域 -->
      <div class="chat-messages" ref="messagesContainer">
        <div
            v-for="message in messages"
            :key="message.id"
            class="message-item"
            :class="{ 'message-own': isOwnMessage(message) }"
        >
          <div class="message-bubble">
            <div class="message-content">{{ message.content }}</div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <el-input
            v-model="inputMessage"
            placeholder="输入消息..."
            @keyup.enter="sendMessage"
            class="input-field"
        />
        <el-button
            type="primary"
            :icon="Promotion"
            @click="sendMessage"
            :disabled="!inputMessage.trim()"
        >
          发送
        </el-button>
      </div>
    </template>

    <!-- 空状态 -->
    <div v-else class="empty-chat">
      <el-empty description="选择一个好友开始聊天">
        <template #image>
          <el-icon :size="100" color="#c0c4cc">
            <ChatLineRound />
          </el-icon>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue'
import { Phone, VideoCamera, Promotion, ChatLineRound } from '@element-plus/icons-vue'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import type { Message } from '@/types'

const chatStore = useChatStore()
const userStore = useUserStore()

const inputMessage = ref('')
const messagesContainer = ref<HTMLElement>()

const messages = computed(() => {
  if (!chatStore.selectedChat) return []
  return chatStore.messages[chatStore.selectedChat.id] || []
})

const isOwnMessage = (message: Message) => {
  return message.senderName === userStore.currentUser?.name
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || !chatStore.selectedChat) return

  chatStore.sendMessage(
      chatStore.selectedChat.id,
      userStore.currentUser!.name,
      inputMessage.value
  )

  inputMessage.value = ''

  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

watch(() => chatStore.selectedChat, () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
})
</script>

<style scoped>
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
}

.chat-user-info {
  display: flex;
  align-items: center;
}

.chat-avatar {
  font-size: 32px;
  margin-right: 12px;
}

.chat-user-details {
  display: flex;
  flex-direction: column;
}

.chat-user-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.chat-user-status {
  font-size: 12px;
  color: #909399;
}

.chat-actions {
  display: flex;
  gap: 10px;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
}

.message-item.message-own {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 60%;
  padding: 12px 16px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-own .message-bubble {
  background: #409eff;
  color: white;
}

.message-content {
  margin-bottom: 4px;
  word-wrap: break-word;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
}

.message-own .message-time {
  color: rgba(255, 255, 255, 0.9);
}

.chat-input {
  display: flex;
  gap: 10px;
  padding: 20px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

.input-field {
  flex: 1;
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>